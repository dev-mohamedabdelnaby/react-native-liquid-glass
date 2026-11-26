//
//  GlassContainerScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates how `GlassEffectContainer` spacing blends nearby glass shapes together.
struct GlassContainerScreen: View {
    @State private var containerSpacing: CGFloat = 40
    @State private var offset: CGFloat = 0
    @Environment(\.layoutDirection) private var layoutDirection

    var body: some View {
        VStack(spacing: 40) {
            Spacer()

            GlassEffectContainer(spacing: containerSpacing) {
                HStack(spacing: 40) {
                    GlassImage("scribble.variable")
                        .frame(width: 80, height: 80)
                        .font(.system(size: 36))
                        .appGlassEffect()

                    GlassImage("eraser.fill")
                        .frame(width: 80, height: 80)
                        .font(.system(size: 36))
                        .appGlassEffect()
                        // Moving the shape closer makes the effects blend.
                        // Offsets aren't mirrored automatically, so flip the direction for RTL.
                        .offset(x: layoutDirection == .rightToLeft ? offset : -offset)
                }
            }

            Spacer()

            VStack(alignment: .leading, spacing: 12) {
                Text("Container spacing: \(Int(containerSpacing))")
                Slider(value: $containerSpacing, in: 0...80)

                Text("Pull together: \(Int(offset))")
                Slider(value: $offset, in: 0...80)
            }
            .padding()
            .appGlassEffect(in: .rect(cornerRadius: 24))
        }
        .padding()
        .glassBackdrop()
        .navigationTitle("Container")
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview("Light") {
    NavigationStack { GlassContainerScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassContainerScreen() }
        .preferredColorScheme(.dark)
}
