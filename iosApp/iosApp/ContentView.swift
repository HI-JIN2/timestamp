import Photos
import PhotosUI
import SwiftUI
import UIKit
import ComposeApp

struct ContentView: View {
    @State private var isPhotoPickerPresented = false
    @State private var selectedPhotoItem: PhotosPickerItem?
    @State private var selectedImageBase64: String?
    @State private var metadataTimestampLabel: String?
    @State private var exportMessage: String?

    var body: some View {
        ComposeView(
            selectedImageBase64: selectedImageBase64,
            metadataTimestampLabel: metadataTimestampLabel,
            exportMessage: exportMessage,
            onPickPhotoRequest: { isPhotoPickerPresented = true },
            onExportRequest: { request in
                exportMessage = exportTimestampedImage(request)
            },
            onExportMessageConsumed: { exportMessage = nil }
        )
            .id("\(selectedImageBase64 ?? "empty")-\(exportMessage ?? "")")
            .ignoresSafeArea(.keyboard)
            .photosPicker(
                isPresented: $isPhotoPickerPresented,
                selection: $selectedPhotoItem,
                matching: .images
            )
            .task(id: selectedPhotoItem) {
                guard let selectedPhotoItem else { return }
                if let data = try? await selectedPhotoItem.loadTransferable(type: Data.self) {
                    selectedImageBase64 = data.base64EncodedString()
                    metadataTimestampLabel = selectedPhotoItem.itemIdentifier
                        .flatMap(fetchAssetDateLabel(for:))
                    exportMessage = nil
                }
            }
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    let selectedImageBase64: String?
    let metadataTimestampLabel: String?
    let exportMessage: String?
    let onPickPhotoRequest: () -> Void
    let onExportRequest: (TimestampExportRequest) -> Void
    let onExportMessageConsumed: () -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            selectedImageBase64: selectedImageBase64,
            metadataTimestampLabel: metadataTimestampLabel,
            exportMessage: exportMessage,
            onPickPhoto: onPickPhotoRequest,
            onExport: onExportRequest,
            onExportMessageConsumed: onExportMessageConsumed
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

private func fetchAssetDateLabel(for itemIdentifier: String) -> String? {
    let assets = PHAsset.fetchAssets(withLocalIdentifiers: [itemIdentifier], options: nil)
    guard let date = assets.firstObject?.creationDate else { return nil }

    let formatter = DateFormatter()
    formatter.dateFormat = "MM.dd.yy  HH:mm"
    return formatter.string(from: date)
}

private func exportTimestampedImage(_ request: TimestampExportRequest) -> String {
    guard
        let imageData = Data(base64Encoded: request.imageBase64),
        let image = UIImage(data: imageData),
        let renderedImage = renderTimestampedImage(image: image, request: request)
    else {
        return "이미지를 준비하지 못했습니다."
    }

    let activityController = UIActivityViewController(
        activityItems: [renderedImage],
        applicationActivities: nil
    )

    guard let rootViewController = currentRootViewController() else {
        return "공유 시트를 열지 못했습니다."
    }

    if let popover = activityController.popoverPresentationController {
        popover.sourceView = rootViewController.view
        popover.sourceRect = CGRect(
            x: rootViewController.view.bounds.midX,
            y: rootViewController.view.bounds.maxY - 40,
            width: 1,
            height: 1
        )
    }

    rootViewController.present(activityController, animated: true)
    return "공유 시트를 열었습니다. '이미지 저장'으로 사진 앱에 저장할 수 있습니다."
}

private func renderTimestampedImage(
    image: UIImage,
    request: TimestampExportRequest
) -> UIImage? {
    guard
        let cgImage = image.cgImage,
        let cropPreset = iosCropPreset(
            aspectRatioKey: request.aspectRatioKey,
            cropScale: CGFloat(request.cropScale),
            offsetXRatio: CGFloat(request.cropOffsetXRatio),
            offsetYRatio: CGFloat(request.cropOffsetYRatio),
            width: CGFloat(cgImage.width),
            height: CGFloat(cgImage.height)
        ),
        let croppedCgImage = cgImage.cropping(to: cropPreset.sourceRect)
    else {
        return nil
    }

    let croppedImage = UIImage(cgImage: croppedCgImage, scale: image.scale, orientation: image.imageOrientation)
    let size = croppedImage.size
    let format = UIGraphicsImageRendererFormat.default()
    format.scale = croppedImage.scale
    let renderer = UIGraphicsImageRenderer(size: size, format: format)

    return renderer.image { _ in
        croppedImage.draw(in: CGRect(origin: .zero, size: size))

        let stylePreset = iosOverlayStylePreset(
            scaleKey: request.scaleKey,
            insetKey: request.insetKey,
            safeAreaKey: request.safeAreaKey
        )
        let timestampFont = UIFont.monospacedDigitSystemFont(
            ofSize: max(size.width * stylePreset.timestampRatio, 22),
            weight: .bold
        )
        let locationFont = UIFont.monospacedSystemFont(
            ofSize: max(size.width * stylePreset.locationRatio, 11),
            weight: .regular
        )

        let shadow = NSShadow()
        shadow.shadowColor = colorFromHex(request.shadowColorHex)
        shadow.shadowOffset = CGSize(width: 0, height: max(size.width * 0.006, 2))
        shadow.shadowBlurRadius = max(size.width * 0.008, 5)

        let timestampAttributes: [NSAttributedString.Key: Any] = [
            .font: timestampFont,
            .foregroundColor: colorFromHex(request.timestampColorHex),
            .shadow: shadow
        ]
        let locationAttributes: [NSAttributedString.Key: Any] = [
            .font: locationFont,
            .foregroundColor: colorFromHex(request.locationColorHex),
            .shadow: shadow
        ]

        let horizontalPadding = max(size.width * 0.04, 18)
        let bottomPadding = max(size.height * (stylePreset.bottomInsetRatio + stylePreset.safeAreaExtraRatio), 18)
        let timestampSize = (request.timestamp as NSString).size(withAttributes: timestampAttributes)
        let locationSize = (request.location as NSString).size(withAttributes: locationAttributes)
        let contentWidth = max(timestampSize.width, locationSize.width)

        let startX: CGFloat = request.alignmentKey == "bottom_end"
            ? size.width - horizontalPadding - contentWidth
            : horizontalPadding
        let adjustedX = startX + (size.width * 0.018 * CGFloat(request.offsetXStep))
        let locationY = size.height - bottomPadding - locationSize.height - (size.height * 0.016 * CGFloat(request.offsetYStep))
        let timestampY = locationY - timestampSize.height - max(size.height * 0.008, 6)

        (request.timestamp as NSString).draw(
            at: CGPoint(x: adjustedX, y: timestampY),
            withAttributes: timestampAttributes
        )
        (request.location as NSString).draw(
            at: CGPoint(x: adjustedX, y: locationY),
            withAttributes: locationAttributes
        )
    }
}

private func iosCropPreset(
    aspectRatioKey: String,
    cropScale: CGFloat,
    offsetXRatio: CGFloat,
    offsetYRatio: CGFloat,
    width: CGFloat,
    height: CGFloat
) -> (sourceRect: CGRect)? {
    let aspectRatio: CGFloat = aspectRatioKey == "16_9" ? (16 / 9) : (4 / 3)
    let zoom = min(max(cropScale, 1), 4)

    let baseCropWidth: CGFloat
    let baseCropHeight: CGFloat
    if width / height > aspectRatio {
        baseCropHeight = height
        baseCropWidth = baseCropHeight * aspectRatio
    } else {
        baseCropWidth = width
        baseCropHeight = baseCropWidth / aspectRatio
    }

    let cropWidth = baseCropWidth / zoom
    let cropHeight = baseCropHeight / zoom
    let maxShiftX = max((width - cropWidth) / 2, 0)
    let maxShiftY = max((height - cropHeight) / 2, 0)
    let centerX = width / 2 + min(max(offsetXRatio, -1), 1) * maxShiftX
    let centerY = height / 2 + min(max(offsetYRatio, -1), 1) * maxShiftY
    let left = min(max(centerX - cropWidth / 2, 0), width - cropWidth)
    let top = min(max(centerY - cropHeight / 2, 0), height - cropHeight)

    return (sourceRect: CGRect(x: left, y: top, width: cropWidth, height: cropHeight).integral)
}

private func iosOverlayStylePreset(scaleKey: String, insetKey: String, safeAreaKey: String) -> (timestampRatio: CGFloat, locationRatio: CGFloat, bottomInsetRatio: CGFloat, safeAreaExtraRatio: CGFloat) {
    let timestampRatio: CGFloat
    let locationRatio: CGFloat
    switch scaleKey {
    case "small":
        timestampRatio = 0.052
        locationRatio = 0.024
    case "large":
        timestampRatio = 0.074
        locationRatio = 0.034
    default:
        timestampRatio = 0.064
        locationRatio = 0.029
    }

    let bottomInsetRatio: CGFloat
    switch insetKey {
    case "tight":
        bottomInsetRatio = 0.055
    case "spacious":
        bottomInsetRatio = 0.11
    default:
        bottomInsetRatio = 0.08
    }

    let safeAreaExtraRatio: CGFloat
    switch safeAreaKey {
    case "standard":
        safeAreaExtraRatio = 0.025
    case "strong":
        safeAreaExtraRatio = 0.05
    default:
        safeAreaExtraRatio = 0
    }

    return (timestampRatio, locationRatio, bottomInsetRatio, safeAreaExtraRatio)
}

private func colorFromHex(_ hex: String) -> UIColor {
    let sanitized = hex.replacingOccurrences(of: "#", with: "")
    var value: UInt64 = 0
    Scanner(string: sanitized).scanHexInt64(&value)

    switch sanitized.count {
    case 8:
        return UIColor(
            red: CGFloat((value & 0x00FF0000) >> 16) / 255,
            green: CGFloat((value & 0x0000FF00) >> 8) / 255,
            blue: CGFloat(value & 0x000000FF) / 255,
            alpha: CGFloat((value & 0xFF000000) >> 24) / 255
        )
    default:
        return UIColor(
            red: CGFloat((value & 0xFF0000) >> 16) / 255,
            green: CGFloat((value & 0x00FF00) >> 8) / 255,
            blue: CGFloat(value & 0x0000FF) / 255,
            alpha: 1
        )
    }
}

private func currentRootViewController() -> UIViewController? {
    UIApplication.shared.connectedScenes
        .compactMap { $0 as? UIWindowScene }
        .flatMap(\.windows)
        .first(where: \.isKeyWindow)?
        .rootViewController
}
