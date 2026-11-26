//
//  GlassMorphingScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates morphing glass shapes in and out with `glassEffectID(_:in:)`.
struct GlassMorphingScreen: View {
    @State private var isExpanded = false
    @Namespace private var namespace

    private let tools = ["pencil", "eraser.fill", "paintbrush.fill", "lasso"]

    var body: some View {
        VStack(spacing: 40) {
            Spacer()

            GlassEffectContainer(spacing: 20) {
                VStack(spacing: 16) {
                    if isExpanded {
                        ForEach(tools, id: \.self) { tool in
                            GlassImage(tool)
                                .font(.title2)
                                .frame(width: 60, height: 60)
                                .appGlassEffect(.regular.interactive())
                                .glassEffectID(tool, in: namespace)
                        }
                    }

                    Button {
                        withAnimation(.bouncy) {
                            isExpanded.toggle()
                        }
                    } label: {
                        GlassImage(isExpanded ? "xmark" : "plus")
                            .font(.title2)
                            .frame(width: 60, height: 60)
                    }
                    .buttonStyle(.glass)
                    .buttonBorderShape(.circle)
                    .glassEffectID("toggle", in: namespace)
                }
            }

            Spacer()

            Text("Tap the button to morph the tools out of it")
                .glassFont(.footnote)
                .padding()
                .appGlassEffect()
        }
        .padding()
        .frame(maxWidth: .infinity)
        .glassBackdrop()
        .navigationTitle("Morphing")
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview("Light") {
    NavigationStack { GlassMorphingScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassMorphingScreen() }
        .preferredColorScheme(.dark)
}
