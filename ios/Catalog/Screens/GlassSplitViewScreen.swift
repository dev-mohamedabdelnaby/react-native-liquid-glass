//
//  GlassSplitViewScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates a glass sidebar and inspector in `NavigationSplitView`,
/// with `backgroundExtensionEffect()` extending artwork underneath them.
struct GlassSplitViewScreen: View {
    @State private var isPresented = false

    var body: some View {
        VStack(spacing: 24) {
            Text("The sidebar and inspector float on Liquid Glass. The header artwork mirrors and blurs beneath them with the background extension effect.")
                .multilineTextAlignment(.center)
                .padding()
                .appGlassEffect(in: .rect(cornerRadius: 24))

            Button("Open Split View", glassImage: "sidebar.leading") {
                isPresented = true
            }
            .buttonStyle(.glassProminent)
            .controlSize(.large)

            Text("Best viewed on iPad or in landscape.")
                .glassFont(.footnote)
                .foregroundStyle(.secondary)
        }
        .padding()
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .glassBackdrop()
        .navigationTitle("Split View")
        .navigationBarTitleDisplayMode(.inline)
        .fullScreenCover(isPresented: $isPresented) {
            SplitViewDemo()
        }
    }
}

/// The full-screen split view presented by `GlassSplitViewScreen`.
private struct SplitViewDemo: View {
    @Environment(\.dismiss) private var dismiss
    @State private var selection: String? = "Mountains"
    @State private var isInspectorPresented = true
    @State private var isExtensionEnabled = true

    private let places = ["Mountains", "Ocean", "Desert", "Forest"]

    var body: some View {
        NavigationSplitView {
            List(places, id: \.self, selection: $selection) { place in
                Label(place, glassImage: symbol(for: place))
            }
            .navigationTitle("Places")
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Close", glassImage: "xmark", uiKit: true) { dismiss() }
                }
            }
        } detail: {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    header
                    Text(selection ?? "Select a place")
                        .glassFont(.largeTitle, weight: .bold)
                    Text("Toggle the background extension effect from the inspector to compare how the artwork fills the space under the sidebar and inspector.")
                        .foregroundStyle(.secondary)
                }
                .padding(.bottom)
            }
            .ignoresSafeArea(edges: .top)
            .toolbar {
                ToolbarItem {
                    Button("Inspector", glassImage: "info", uiKit: true) {
                        isInspectorPresented.toggle()
                    }
                }
            }
            .inspector(isPresented: $isInspectorPresented) {
                Form {
                    Toggle("Background extension", isOn: $isExtensionEnabled)
                }
                .navigationTitle("Inspector")
            }
        }
    }

    /// Colorful header artwork that extends under the sidebar and inspector.
    private var header: some View {
        LinearGradient(
            colors: [.orange, .pink, .purple, .indigo],
            startPoint: .leading,
            endPoint: .trailing
        )
        .overlay {
            GlassImage(symbol(for: selection ?? ""))
                .font(.system(size: 80))
                .foregroundStyle(.white)
        }
        .frame(height: 300)
        .backgroundExtensionEffect(isEnabled: isExtensionEnabled)
    }

    private func symbol(for place: String) -> String {
        switch place {
        case "Mountains": "mountain.2.fill"
        case "Ocean": "water.waves"
        case "Desert": "sun.max.fill"
        case "Forest": "tree.fill"
        default: "map.fill"
        }
    }
}

#Preview("Light") {
    NavigationStack { GlassSplitViewScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassSplitViewScreen() }
        .preferredColorScheme(.dark)
}
