//
//  GlassScrollEdgeScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates scroll edge effects behind glass bars, and a custom glass bar using `safeAreaBar(edge:)`.
struct GlassScrollEdgeScreen: View {
    /// The scroll edge effect options, wrapped so they can drive a picker.
    enum EdgeStyle: String, CaseIterable, Identifiable {
        case automatic = "Auto"
        case soft = "Soft"
        case hard = "Hard"
        case hidden = "Hidden"

        var id: Self { self }

        var style: ScrollEdgeEffectStyle {
            switch self {
            case .automatic, .hidden: .automatic
            case .soft: .soft
            case .hard: .hard
            }
        }
    }

    @State private var edgeStyle: EdgeStyle = .automatic
    @State private var category = 0

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(1...40, id: \.self) { index in
                    HStack {
                        Circle()
                            .fill(Color(hue: Double(index) / 40, saturation: 0.6, brightness: 0.9))
                            .frame(width: 40, height: 40)
                        Text("Item \(index)")
                        Spacer()
                    }
                    .padding()
                    .background(.background.secondary, in: .rect(cornerRadius: 16))
                }
            }
            .padding(.horizontal)
        }
        .scrollEdgeEffectStyle(edgeStyle.style, for: .all)
        .scrollEdgeEffectHidden(edgeStyle == .hidden, for: .all)
        // A custom bar that insets the safe area and extends the scroll edge effect like a system toolbar.
        .safeAreaBar(edge: .top) {
            Picker("Category", selection: $category) {
                Text("All").tag(0)
                Text("Recent").tag(1)
                Text("Shared").tag(2)
            }
            .pickerStyle(.segmented)
            .padding(.horizontal)
            .padding(.vertical, 8)
        }
        .safeAreaBar(edge: .bottom) {
            GlassEffectContainer {
                HStack(spacing: 8) {
                    ForEach(EdgeStyle.allCases) { style in
                        // The selected style uses the prominent glass button.
                        if edgeStyle == style {
                            Button(style.rawValue) {}
                                .buttonStyle(.glassProminent)
                        } else {
                            Button(style.rawValue) {
                                withAnimation { edgeStyle = style }
                            }
                            .buttonStyle(.glass)
                        }
                    }
                }
            }
            .padding()
        }
        .navigationTitle("Scroll Edge")
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview("Light") {
    NavigationStack { GlassScrollEdgeScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassScrollEdgeScreen() }
        .preferredColorScheme(.dark)
}
