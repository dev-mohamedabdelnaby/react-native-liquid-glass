//
//  GlassFloatingHeaderScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates a header with no bar background: content scrolls edge-to-edge under
/// floating glass toolbar buttons, with only a soft edge behind the status bar.
struct GlassFloatingHeaderScreen: View {
    /// How the top edge is treated while content scrolls beneath it.
    enum TopEdge: String, CaseIterable, Identifiable {
        case statusBar = "Status Bar"
        case soft = "Soft"
        case none = "None"

        var id: Self { self }
    }

    @State private var topEdge: TopEdge = .statusBar
    @State private var isFavorite = false

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                header

                LazyVStack(alignment: .leading, spacing: 16) {
                    Picker("Top edge", selection: $topEdge) {
                        ForEach(TopEdge.allCases) { edge in
                            Text(edge.rawValue).tag(edge)
                        }
                    }
                    .pickerStyle(.segmented)

                    Text(description)
                        .glassFont(.footnote)
                        .foregroundStyle(.secondary)

                    ForEach(1...30, id: \.self) { index in
                        HStack(spacing: 12) {
                            Image(systemName: "photo")
                                .font(.title2)
                                .foregroundStyle(.tint)
                            Text("Row \(index)")
                            Spacer()
                        }
                        .padding()
                        .background(.background.secondary, in: .rect(cornerRadius: 16))
                    }
                }
                .padding()
            }
        }
        // Let the header run under the status bar and navigation bar.
        .ignoresSafeArea(edges: .top)
        // Remove the navigation bar background so only the glass buttons remain.
        .toolbarBackgroundVisibility(.hidden, for: .navigationBar)
        .scrollEdgeEffectStyle(.soft, for: .top)
        .scrollEdgeEffectHidden(topEdge != .soft, for: .top)
        .overlay {
            if topEdge == .statusBar {
                StatusBarEdge()
            }
        }
        .navigationTitle("")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button("Favorite", systemImage: isFavorite ? "heart.fill" : "heart") {
                    isFavorite.toggle()
                }
            }

            ToolbarSpacer(.fixed, placement: .topBarTrailing)

            ToolbarItemGroup(placement: .topBarTrailing) {
                Button("Share", systemImage: "square.and.arrow.up") {}
                Button("More", systemImage: "ellipsis") {}
            }
        }
    }

    /// Full-bleed artwork that sits under the status bar and floating toolbar.
    /// When pulled down past the top, it stretches so no background shows above it.
    private var header: some View {
        GeometryReader { proxy in
            // Positive when the scroll view is overscrolled at the top.
            let overscroll = max(0, proxy.frame(in: .scrollView).minY)

            LinearGradient(
                colors: [.indigo, .purple, .pink, .orange],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .frame(width: proxy.size.width, height: proxy.size.height + overscroll)
            .offset(y: -overscroll)
        }
        .frame(height: 320)
        .overlay(alignment: .bottomLeading) {
            VStack(alignment: .leading, spacing: 4) {
                Text("Floating Header")
                    .glassFont(.largeTitle, weight: .bold)
                Text("No bar background — just glass buttons")
                    .glassFont(.subheadline)
            }
            .foregroundStyle(.white)
            .padding()
        }
    }

    private var description: String {
        switch topEdge {
        case .statusBar:
            "Only the status bar area gets a fade, so the clock and battery stay legible."
        case .soft:
            "The system soft scroll edge effect fades content under the whole toolbar."
        case .none:
            "No edge effect at all: content scrolls fully visible under the glass buttons."
        }
    }
}

/// A progressive blur limited to the status bar's height.
private struct StatusBarEdge: View {
    /// The window's top inset covers only the status bar / Dynamic Island area,
    /// unlike the view's safe area, which also includes the navigation bar.
    private var statusBarHeight: CGFloat {
        let window = UIApplication.shared.connectedScenes
            .compactMap { ($0 as? UIWindowScene)?.keyWindow }
            .first
        return window?.safeAreaInsets.top ?? 54
    }

    var body: some View {
        // The material fades out toward the bottom, ending at the status bar's edge.
        Rectangle()
            .fill(.ultraThinMaterial)
            .mask {
                LinearGradient(
                    stops: [.init(color: .black, location: 0.4), .init(color: .clear, location: 1)],
                    startPoint: .top,
                    endPoint: .bottom
                )
            }
            .frame(height: statusBarHeight)
            // Pin to the very top of the screen, ignoring the navigation bar's safe area.
            .frame(maxHeight: .infinity, alignment: .top)
            .ignoresSafeArea()
            .allowsHitTesting(false)
    }
}

#Preview("Light") {
    NavigationStack { GlassFloatingHeaderScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassFloatingHeaderScreen() }
        .preferredColorScheme(.dark)
}
