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

            Button("Show Alert", glassImage: "exclamationmark.triangle") {
                isAlertPresented = true
            }
            .buttonStyle(.glassProminent)

            Button("Show Confirmation Dialog", glassImage: "trash") {
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

            GlassImage("photo.artframe")
                .font(.system(size: 60))
                .frame(width: 160, height: 160)
                .appGlassEffect(.regular.interactive(), in: .rect(cornerRadius: 32))
                .contextMenu {
                    Button("Copy", glassImage: "doc.on.doc", uiKit: true) { lastAction = "Copied" }
                    Button("Share", glassImage: "square.and.arrow.up", uiKit: true) { lastAction = "Shared" }
                    Divider()
                    Button("Delete", glassImage: "trash", uiKit: true, role: .destructive) { lastAction = "Deleted" }
                } preview: {
                    GlassImage("photo.artframe")
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
