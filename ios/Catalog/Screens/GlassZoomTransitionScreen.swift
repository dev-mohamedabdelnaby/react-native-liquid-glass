//
//  GlassZoomTransitionScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates sheets that zoom out of glass toolbar items and buttons with
/// `matchedTransitionSource(id:in:)`, plus toolbar badges and `sharedBackgroundVisibility(_:)`.
struct GlassZoomTransitionScreen: View {
    @State private var isComposePresented = false
    @State private var isInboxPresented = false
    @State private var unreadCount = 3
    @Namespace private var namespace

    var body: some View {
        VStack(spacing: 24) {
            Text("Tap the toolbar buttons — their sheets zoom out of the glass button that presented them.")
                .multilineTextAlignment(.center)
                .padding()
                .appGlassEffect(in: .rect(cornerRadius: 24))

            Button("New Message", systemImage: "square.and.pencil") {
                isComposePresented = true
            }
            .buttonStyle(.glassProminent)
            .controlSize(.large)
            .matchedTransitionSource(id: "compose", in: namespace)

            Stepper("Unread badge: \(unreadCount)", value: $unreadCount, in: 0...99)
                .padding()
                .appGlassEffect(in: .rect(cornerRadius: 20))
        }
        .padding()
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .glassBackdrop()
        .navigationTitle("Zoom Transitions")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button("Inbox", systemImage: "tray.fill") {
                    isInboxPresented = true
                }
                .badge(unreadCount)
            }
            .matchedTransitionSource(id: "inbox", in: namespace)

            // Hiding the shared background places this item outside the grouped glass capsule.
            ToolbarItem(placement: .principal) {
                Label("Online", systemImage: "circle.fill")
                    .labelStyle(.titleAndIcon)
                    .font(.caption.bold())
                    .foregroundStyle(.green)
            }
            .sharedBackgroundVisibility(.hidden)
        }
        .sheet(isPresented: $isInboxPresented) {
            NavigationStack {
                List(0..<max(unreadCount, 1), id: \.self) { index in
                    Label("Message \(index + 1)", systemImage: "envelope.badge")
                }
                .navigationTitle("Inbox")
                .toolbar {
                    Button("Done", systemImage: "checkmark") { isInboxPresented = false }
                }
            }
            .presentationDetents([.medium, .large])
            .navigationTransition(.zoom(sourceID: "inbox", in: namespace))
        }
        .sheet(isPresented: $isComposePresented) {
            NavigationStack {
                Form {
                    TextField("To", text: .constant(""))
                    TextField("Subject", text: .constant(""))
                }
                .navigationTitle("New Message")
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel", systemImage: "xmark") { isComposePresented = false }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Send", systemImage: "arrow.up") { isComposePresented = false }
                            .buttonStyle(.glassProminent)
                    }
                }
            }
            .navigationTransition(.zoom(sourceID: "compose", in: namespace))
        }
    }
}

#Preview("Light") {
    NavigationStack { GlassZoomTransitionScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassZoomTransitionScreen() }
        .preferredColorScheme(.dark)
}
