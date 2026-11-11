//
//  LiquidGlassView.swift
//  liquidGlass
//

import SwiftUI

/// The shapes a `LiquidGlassView` can take.
enum LiquidGlassShape: String, CaseIterable, Identifiable {
    case capsule = "Capsule"
    case roundedRect = "Rounded"
    case circle = "Circle"
    case rect = "Rect"

    var id: Self { self }
}

/// A reusable container that places any content on a Liquid Glass surface.
///
/// ```swift
/// LiquidGlassView(shape: .roundedRect, tint: .blue, isInteractive: true) {
///     Label("Hello", systemImage: "drop.fill")
/// }
/// ```
struct LiquidGlassView<Content: View>: View {
    var isClear = false
    var shape: LiquidGlassShape = .roundedRect
    var cornerRadius: CGFloat = 24
    var tint: Color?
    var isInteractive = false
    var padding: CGFloat = 20
    @ViewBuilder var content: Content

    /// The `Glass` configuration built from the view's properties.
    private var glass: Glass {
        var glass: Glass = isClear ? .clear : .regular
        if let tint {
            glass = glass.tint(tint)
        }
        return glass.interactive(isInteractive)
    }

    var body: some View {
        let padded = content.padding(padding)
        switch shape {
        case .capsule:
            padded.appGlassEffect(glass, in: .capsule)
        case .roundedRect:
            padded.appGlassEffect(glass, in: .rect(cornerRadius: cornerRadius))
        case .circle:
            padded.appGlassEffect(glass, in: .circle)
        case .rect:
            padded.appGlassEffect(glass, in: .rect)
        }
    }
}

#Preview("Light") {
    VStack(spacing: 20) {
        LiquidGlassView {
            Text("Regular glass view")
        }
        LiquidGlassView(shape: .capsule, tint: .orange, isInteractive: true) {
            Label("Tinted & interactive", systemImage: "hand.tap")
        }
        LiquidGlassView(shape: .circle) {
            Image(systemName: "star.fill").font(.largeTitle)
        }
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity)
    .glassBackdrop()
    .preferredColorScheme(.light)
}

#Preview("Dark") {
    LiquidGlassView(isClear: true) {
        Text("Clear glass view")
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity)
    .glassBackdrop()
    .preferredColorScheme(.dark)
}
