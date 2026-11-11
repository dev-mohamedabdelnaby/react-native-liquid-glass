//
//  GlassBackdrop.swift
//  liquidGlass
//

import SwiftUI

/// A colorful background so Liquid Glass has rich content to blur and reflect.
/// Uses soft pastel colors in light mode and deep saturated colors in dark mode.
struct GlassBackdrop: View {
    @Environment(\.colorScheme) private var colorScheme

    private var isDark: Bool { colorScheme == .dark }

    var body: some View {
        ZStack {
            LinearGradient(
                colors: isDark
                    ? [.black, .indigo, .purple.opacity(0.8)]
                    : [.cyan.opacity(0.35), .pink.opacity(0.35), .orange.opacity(0.35)],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )

            Circle()
                .fill(isDark ? Color.blue.opacity(0.6) : Color.mint.opacity(0.6))
                .frame(width: 260, height: 260)
                .blur(radius: 30)
                .offset(x: -120, y: -220)

            Circle()
                .fill(isDark ? Color.pink.opacity(0.5) : Color.yellow.opacity(0.6))
                .frame(width: 200, height: 200)
                .blur(radius: 30)
                .offset(x: 140, y: 240)

            Image(systemName: "sparkles")
                .font(.system(size: 160))
                .foregroundStyle(.primary.opacity(0.15))
        }
        .ignoresSafeArea()
    }
}

extension View {
    /// Places the view on top of the shared colorful backdrop.
    func glassBackdrop() -> some View {
        background { GlassBackdrop() }
    }
}

#Preview("Light") {
    GlassBackdrop()
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    GlassBackdrop()
        .preferredColorScheme(.dark)
}
