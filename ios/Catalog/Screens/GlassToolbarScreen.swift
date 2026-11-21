//
//  GlassToolbarScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates system toolbars, which adopt Liquid Glass automatically,
/// plus a compose button whose actions can morph out of it.
struct GlassToolbarScreen: View {
    @State private var isFavorite = false
    @State private var isMorphEnabled = true
    @State private var isGroupGlassVisible = true
    @State private var isComposeExpanded = false
    @State private var lastAction = "None"
    @Namespace private var namespace

    private let composeActions: [(title: String, symbol: String)] = [
        ("New Note", "note.text"),
        ("New Photo", "camera"),
        ("New Folder", "folder.badge.plus"),
    ]

    var body: some View {
        List {
            Section {
                LabeledContent("Last action", value: lastAction)
                Toggle("Morph compose button", isOn: $isMorphEnabled)
                Toggle("Glass behind Share & More", isOn: $isGroupGlassVisible.animation())
            } footer: {
                Text("These options are also in the ••• menu.").glassFontOnly(.footnote)
            }

            Section {
                ForEach(1...40, id: \.self) { index in
                    Label("Row \(index)", systemImage: "photo")
                }
            } header: {
                Text("Rows").glassFontOnly(.headline)
            }
        }
        .navigationTitle("Toolbar")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button("Favorite", systemImage: isFavorite ? "heart.fill" : "heart") {
                    isFavorite.toggle()
                    lastAction = isFavorite ? "Favorited" : "Unfavorited"
                }
            }

            ToolbarSpacer(.fixed, placement: .topBarTrailing)

            ToolbarItemGroup(placement: .topBarTrailing) {
                ShareLink(item: URL(string: "https://developer.apple.com/documentation/swiftui/applying-liquid-glass-to-custom-views")!)

                Menu("More", systemImage: "ellipsis") {
                    Toggle("Morph Compose Button", systemImage: "wand.and.stars", isOn: $isMorphEnabled)
                    Toggle("Glass Behind Group", systemImage: "drop", isOn: $isGroupGlassVisible.animation())
                    Divider()
                    Button("Select Rows", systemImage: "checkmark.circle") { lastAction = "Select rows" }
                    Button("Sort", systemImage: "arrow.up.arrow.down") { lastAction = "Sort" }
                }
            }
            // Hiding the shared background removes the glass capsule behind Share and More.
            .sharedBackgroundVisibility(isGroupGlassVisible ? .visible : .hidden)

            ToolbarItemGroup(placement: .bottomBar) {
                // Backward/forward symbols flip automatically in right-to-left layouts.
                Button("Back", systemImage: "chevron.backward") { lastAction = "Back" }
                Button("Forward", systemImage: "chevron.forward") { lastAction = "Forward" }
            }

            ToolbarSpacer(.flexible, placement: .bottomBar)
        }
        .overlay(alignment: .bottomTrailing) {
            composeButton
                .padding()
        }
    }

    /// A compose button whose actions morph out of it when morphing is enabled,
    /// or simply fade in as separate glass shapes when it's disabled.
    private var composeButton: some View {
        // Container spacing below the stack spacing keeps the shapes apart at rest.
        GlassEffectContainer(spacing: isMorphEnabled ? 10 : 0) {
            VStack(alignment: .trailing, spacing: 12) {
                if isComposeExpanded {
                    ForEach(composeActions, id: \.title) { action in
                        Button {
                            lastAction = action.title
                            withAnimation(.bouncy) { isComposeExpanded = false }
                        } label: {
                            Label(action.title, systemImage: action.symbol)
                                .glassFont(.headline)
                                .padding(.horizontal, 18)
                                .padding(.vertical, 12)
                        }
                        .buttonStyle(.plain)
                        .appGlassEffect(.regular.interactive())
                        .glassEffectID(isMorphEnabled ? action.title : nil, in: namespace)
                        .glassEffectTransition(isMorphEnabled ? .matchedGeometry : .materialize)
                    }
                }

                Button {
                    withAnimation(.bouncy) { isComposeExpanded.toggle() }
                } label: {
                    Image(systemName: isComposeExpanded ? "xmark" : "square.and.pencil")
                        .font(.title2.weight(.semibold))
                        .foregroundStyle(.white)
                        .contentTransition(.symbolEffect(.replace))
                        .frame(width: 60, height: 60)
                }
                .buttonStyle(.plain)
                .glassEffect(.regular.tint(.blue).interactive(), in: .circle)
                .glassEffectID(isMorphEnabled ? "compose" : nil, in: namespace)
                .accessibilityLabel(isComposeExpanded ? "Close" : "Compose")
            }
        }
    }
}

#Preview("Light") {
    NavigationStack { GlassToolbarScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassToolbarScreen() }
        .preferredColorScheme(.dark)
}
