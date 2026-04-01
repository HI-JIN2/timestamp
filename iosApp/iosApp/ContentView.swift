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
            }
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

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            selectedImageBase64: selectedImageBase64,
            metadataTimestampLabel: metadataTimestampLabel,
            exportMessage: exportMessage,
            onPickPhoto: onPickPhotoRequest,
            onExport: onExportRequest
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
    let size = image.size
    let format = UIGraphicsImageRendererFormat.default()
    format.scale = image.scale
    let renderer = UIGraphicsImageRenderer(size: size, format: format)

    return renderer.image { _ in
        image.draw(in: CGRect(origin: .zero, size: size))

        let timestampFont = UIFont.monospacedDigitSystemFont(ofSize: max(size.width * 0.052, 26), weight: .bold)
        let locationFont = UIFont.monospacedSystemFont(ofSize: max(size.width * 0.024, 13), weight: .regular)

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
        let bottomPadding = max(size.height * 0.05, 18)
        let timestampSize = (request.timestamp as NSString).size(withAttributes: timestampAttributes)
        let locationSize = (request.location as NSString).size(withAttributes: locationAttributes)
        let contentWidth = max(timestampSize.width, locationSize.width)

        let startX: CGFloat = request.alignmentKey == "bottom_end"
            ? size.width - horizontalPadding - contentWidth
            : horizontalPadding
        let locationY = size.height - bottomPadding - locationSize.height
        let timestampY = locationY - timestampSize.height - max(size.height * 0.008, 6)

        (request.timestamp as NSString).draw(
            at: CGPoint(x: startX, y: timestampY),
            withAttributes: timestampAttributes
        )
        (request.location as NSString).draw(
            at: CGPoint(x: startX, y: locationY),
            withAttributes: locationAttributes
        )
    }
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
