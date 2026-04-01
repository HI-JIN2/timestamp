import PhotosUI
import SwiftUI
import ComposeApp

struct ContentView: View {
    @State private var isPhotoPickerPresented = false
    @State private var selectedPhotoItem: PhotosPickerItem?
    @State private var selectedImageBase64: String?

    var body: some View {
        ComposeView(
            selectedImageBase64: selectedImageBase64,
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
                }
            }
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    let selectedImageBase64: String?
    let onPickPhotoRequest: () -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            selectedImageBase64: selectedImageBase64,
            onPickPhoto: onPickPhotoRequest
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
