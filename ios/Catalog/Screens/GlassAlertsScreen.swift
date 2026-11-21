//
//  GlassAlertsScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates alerts, confirmation dialogs and context menus, which all render on Liquid Glass.
struct GlassAlertsScreen: View {
    @State private var isAlertPresented = false
    @State private var isDialogPresented = false
    @State private var lastAction = "None"

    var body: some View {
        VStack(spacing: 24) {
            Text("Last action: \(lastAction)")
                .glassFont(.headline)

            Button("Show Alert", systemImage: "exclamationmark.triangle") {
                isAlertPresented = true
            }
            .buttonStyle(.glassProminent)

            Button("Show Confirmation Dialog", systemImage: "trash") {
                isDialogPresented = true
            }
            .buttonStyle(.glass)
            .tint(.red)
            // On iOS, the dialog appears as a glass popover anchored to this button.
            .confirmationDialog("Delete this photo?", isPresented: $isDialogPresented, titleVisibility: .visible) {
                Button("Delete", role: .destructive) { lastAction = "Deleted" }
                Button("Archive") { lastAction = "Archived" }
                Button("Cancel", role: .cancel) { lastAction = "Cancelled" }
            }

            Image(systemName: "photo.artframe")
                .font(.system(size: 60))
                .frame(width: 160, height: 160)
                .appGlassEffect(.regular.interactive(), in: .rect(cornerRadius: 32))
                .contextMenu {
                    Button("Copy", systemImage: "doc.on.doc") { lastAction = "Copied" }
                    Button("Share", systemImage: "square.and.arrow.up") { lastAction = "Shared" }
                    Divider()
                    Button("Delete", systemImage: "trash", role: .destructive) { lastAction = "Deleted" }
                } preview: {
                    Image(systemName: "photo.artframe")
                        .font(.system(size: 120))
                        .padding(40)
                }

            Text("Long-press the artwork for a context menu")
                .glassFont(.footnote)
                .foregroundStyle(.secondary)
        }
        .padding()
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .glassBackdrop()
        .navigationTitle("Alerts & Dialogs")
        .navigationBarTitleDisplayMode(.inline)
        .alert("Enable Notifications?", isPresented: $isAlertPresented) {
            Button("Enable") { lastAction = "Enabled" }
            Button("Not Now", role: .cancel) { lastAction = "Not now" }
        } message: {
            Text("Alerts in iOS 26 and later use Liquid Glass.")
        }
    }
}

#Preview("Light") {
    NavigationStack { GlassAlertsScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassAlertsScreen() }
        .preferredColorScheme(.dark)
}
