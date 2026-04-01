import Photos
import PhotosUI
import SwiftUI
import ComposeApp

struct ContentView: View {
    @State private var isPhotoPickerPresented = false
    @State private var selectedPhotoItem: PhotosPickerItem?
    @State private var selectedImageBase64: String?
    @State private var metadataTimestampLabel: String?

    var body: some View {
        ComposeView(
            selectedImageBase64: selectedImageBase64,
            metadataTimestampLabel: metadataTimestampLabel,
            onPickPhotoRequest: { isPhotoPickerPresented = true }
        )
            .id(selectedImageBase64 ?? "empty")
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
                }
            }
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    let selectedImageBase64: String?
    let metadataTimestampLabel: String?
    let onPickPhotoRequest: () -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            selectedImageBase64: selectedImageBase64,
            metadataTimestampLabel: metadataTimestampLabel,
            onPickPhoto: onPickPhotoRequest
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
