//
//  GlassRecipesScreen.swift
//  liquidGlass
//

import SwiftUI

/// Real-world compositions built from Liquid Glass primitives:
/// a sliding segmented selector, map-style floating controls, a mini player and a floating action menu.
struct GlassRecipesScreen: View {
    var body: some View {
        ScrollView {
            VStack(spacing: 40) {
                recipe("Sliding selector") { GlassSegmentedSelector() }
                recipe("Map controls") { MapControls() }
                recipe("Mini player") { MiniPlayer() }
                recipe("Floating action menu") {
                    Text("The + button floats at the bottom-right corner. Its actions open upward without moving it.")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                }
            }
            .padding()
            // Leaves room so the last content can scroll above the floating button.
            .padding(.bottom, 80)
        }
        // Floats above the scroll view, pinned to the bottom-right corner.
        .overlay(alignment: .bottomTrailing) {
            FloatingActionMenu()
                .padding()
        }
        .glassBackdrop()
        .navigationTitle("Recipes")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func recipe<Content: View>(_ title: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(spacing: 16) {
            Text(title)
                .font(.headline)
            content()
        }
        .frame(maxWidth: .infinity)
    }
}

/// A segmented control whose tinted glass indicator morphs between options.
private struct GlassSegmentedSelector: View {
    @State private var selection = "Day"
    @Namespace private var namespace

    private let options = ["Day", "Week", "Month", "Year"]

    var body: some View {
        GlassEffectContainer(spacing: 0) {
            HStack(spacing: 0) {
                ForEach(options, id: \.self) { option in
                    Button {
                        withAnimation(.smooth) { selection = option }
                    } label: {
                        Text(option)
                            .font(.subheadline.weight(.semibold))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                            .background {
                                // The indicator matches geometry across options so it slides.
                                if selection == option {
                                    Capsule()
                                        .fill(.tint.opacity(0.35))
                                        .matchedGeometryEffect(id: "indicator", in: namespace)
                                }
                            }
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(4)
            .appGlassEffect(.regular.interactive())
        }
    }
}

/// Stacked floating controls like those in Maps, grouped with a glass union.
private struct MapControls: View {
    @State private var isTracking = false
    @Namespace private var namespace

    var body: some View {
        ZStack(alignment: .topTrailing) {
            RoundedRectangle(cornerRadius: 28)
                .fill(
                    LinearGradient(colors: [.green.opacity(0.6), .teal.opacity(0.6), .blue.opacity(0.5)],
                                   startPoint: .topLeading, endPoint: .bottomTrailing)
                )
                .overlay {
                    Image(systemName: "map.fill")
                        .font(.system(size: 80))
                        .foregroundStyle(.white.opacity(0.4))
                }
                .frame(height: 220)

            GlassEffectContainer(spacing: 0) {
                VStack(spacing: 0) {
                    controlButton("map", id: "layers") {}
                    Divider().frame(width: 28)
                    controlButton(isTracking ? "location.fill" : "location", id: "locate") {
                        isTracking.toggle()
                    }
                }
            }
            .padding(12)

            GlassEffectContainer {
                Button("Look Around", systemImage: "binoculars.fill") {}
                    .labelStyle(.iconOnly)
                    .font(.title3)
                    .frame(width: 48, height: 48)
                    .appGlassEffect(.regular.interactive(), in: .circle)
            }
            .padding(12)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomLeading)
        }
        .frame(height: 220)
    }

    private func controlButton(_ symbol: String, id: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: symbol)
                .font(.title3)
                .contentTransition(.symbolEffect(.replace))
                .frame(width: 48, height: 48)
        }
        .buttonStyle(.plain)
        .appGlassEffect(.regular.interactive(), in: .rect(cornerRadius: 16))
        .glassEffectUnion(id: "controls", namespace: namespace)
    }
}

/// A music mini player that expands into full controls, morphing its glass.
private struct MiniPlayer: View {
    @State private var isPlaying = false
    @State private var isExpanded = false
    @State private var progress = 0.35
    @Namespace private var namespace

    var body: some View {
        GlassEffectContainer(spacing: 16) {
            VStack(spacing: 12) {
                HStack(spacing: 12) {
                    RoundedRectangle(cornerRadius: 10)
                        .fill(LinearGradient(colors: [.pink, .orange], startPoint: .top, endPoint: .bottom))
                        .frame(width: 44, height: 44)
                        .overlay { Image(systemName: "music.note").foregroundStyle(.white) }

                    VStack(alignment: .leading) {
                        Text("Glass Animals")
                            .font(.headline)
                        Text("Heat Waves")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }

                    Spacer()

                    Button(isPlaying ? "Pause" : "Play", systemImage: isPlaying ? "pause.fill" : "play.fill") {
                        isPlaying.toggle()
                    }
                    .labelStyle(.iconOnly)
                    .font(.title2)
                    .contentTransition(.symbolEffect(.replace))
                    .buttonStyle(.plain)
                }
                .padding(12)
                .contentShape(.rect)
                .onTapGesture {
                    withAnimation(.bouncy) { isExpanded.toggle() }
                }
                .appGlassEffect(.regular.interactive(), in: .rect(cornerRadius: 24))
                .glassEffectID("player", in: namespace)

                if isExpanded {
                    VStack(spacing: 12) {
                        Slider(value: $progress)
                        HStack(spacing: 40) {
                            Button("Back", systemImage: "backward.fill") {}
                            Button("Shuffle", systemImage: "shuffle") {}
                            Button("Forward", systemImage: "forward.fill") {}
                        }
                        .labelStyle(.iconOnly)
                        .font(.title3)
                        .buttonStyle(.plain)
                    }
                    .padding()
                    .appGlassEffect(in: .rect(cornerRadius: 24))
                    .glassEffectID("controls", in: namespace)
                }
            }
        }
    }
}

/// A floating action button that fans out a vertical menu of glass actions.
private struct FloatingActionMenu: View {
    @State private var isOpen = false
    @Namespace private var namespace

    private let actions: [(title: String, symbol: String, color: Color)] = [
        ("Photo", "camera.fill", .blue),
        ("Note", "note.text", .yellow),
        ("Reminder", "checklist", .green),
    ]

    var body: some View {
        GlassEffectContainer(spacing: 16) {
            VStack(alignment: .trailing, spacing: 12) {
                if isOpen {
                    ForEach(actions, id: \.title) { action in
                        Label(action.title, systemImage: action.symbol)
                            .font(.headline)
                            .padding(.horizontal, 18)
                            .padding(.vertical, 12)
                            .glassEffect(.regular.tint(action.color.opacity(0.5)).interactive())
                            .glassEffectID(action.title, in: namespace)
                            .onTapGesture {
                                withAnimation(.bouncy) { isOpen = false }
                            }
                    }
                }

                Button {
                    withAnimation(.bouncy) { isOpen.toggle() }
                } label: {
                    Image(systemName: "plus")
                        .font(.title2.bold())
                        .rotationEffect(.degrees(isOpen ? 45 : 0))
                        .frame(width: 60, height: 60)
                }
                .buttonStyle(.glassProminent)
                .buttonBorderShape(.circle)
                .glassEffectID("fab", in: namespace)
                .accessibilityLabel(isOpen ? "Close" : "Add")
            }
        }
    }
}

#Preview("Light") {
    NavigationStack { GlassRecipesScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassRecipesScreen() }
        .preferredColorScheme(.dark)
}
