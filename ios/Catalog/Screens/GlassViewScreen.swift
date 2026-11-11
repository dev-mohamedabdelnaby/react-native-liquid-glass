//
//  GlassViewScreen.swift
//  liquidGlass
//

import SwiftUI
import UIKit

/// A playground for `LiquidGlassView`: tweak variant, shape, tint and interactivity live,
/// and compare it with the UIKit `UIGlassEffect` equivalent.
struct GlassViewScreen: View {
    @State private var isClear = false
    @State private var shape: LiquidGlassShape = .roundedRect
    @State private var cornerRadius: CGFloat = 24
    @State private var isTinted = false
    @State private var tint = Color.blue
    @State private var isInteractive = true

    var body: some View {
        ScrollView {
            VStack(spacing: 28) {
                LiquidGlassView(
                    isClear: isClear,
                    shape: shape,
                    cornerRadius: cornerRadius,
                    tint: isTinted ? tint.opacity(0.6) : nil,
                    isInteractive: isInteractive,
                    padding: shape == .circle ? 36 : 24
                ) {
                    preview
                }
                .frame(height: 220)
                .animation(.smooth, value: shape)

                controls

                VStack(spacing: 12) {
                    Text("UIKit · UIGlassEffect")
                        .font(.headline)
                    UIKitGlassView(isTinted: isTinted, tint: UIColor(tint), isInteractive: isInteractive)
                        .frame(height: 100)
                }
            }
            .padding()
        }
        .glassBackdrop()
        .navigationTitle("Glass View")
        .navigationBarTitleDisplayMode(.inline)
    }

    @ViewBuilder
    private var preview: some View {
        if shape == .circle {
            Image(systemName: "drop.fill")
                .font(.system(size: 48))
        } else {
            VStack(alignment: .leading, spacing: 8) {
                Label("Liquid Glass View", systemImage: "drop.fill")
                    .font(.title3.bold())
                Text("Any content can sit on a glass surface. Adjust the options below.")
                    .foregroundStyle(.secondary)
            }
        }
    }

    private var controls: some View {
        VStack(alignment: .leading, spacing: 16) {
            Picker("Variant", selection: $isClear) {
                Text("Regular").tag(false)
                Text("Clear").tag(true)
            }
            .pickerStyle(.segmented)

            Picker("Shape", selection: $shape) {
                ForEach(LiquidGlassShape.allCases) { shape in
                    Text(shape.rawValue).tag(shape)
                }
            }
            .pickerStyle(.segmented)

            if shape == .roundedRect {
                VStack(alignment: .leading) {
                    Text("Corner radius: \(Int(cornerRadius))")
                    Slider(value: $cornerRadius, in: 0...60)
                }
            }

            Toggle("Interactive", isOn: $isInteractive)
            Toggle("Tint", isOn: $isTinted.animation())
            if isTinted {
                ColorPicker("Tint color", selection: $tint, supportsOpacity: false)
            }
        }
        .padding(20)
        .appGlassEffect(in: .rect(cornerRadius: 28))
    }
}

/// Wraps a UIKit `UIVisualEffectView` configured with `UIGlassEffect`.
private struct UIKitGlassView: UIViewRepresentable {
    var isTinted: Bool
    var tint: UIColor
    var isInteractive: Bool

    func makeUIView(context: Context) -> UIVisualEffectView {
        let view = UIVisualEffectView()
        view.layer.cornerRadius = 28
        view.clipsToBounds = true

        let label = UILabel()
        label.text = "UIVisualEffectView + UIGlassEffect"
        label.font = .preferredFont(forTextStyle: .headline)
        label.adjustsFontForContentSizeCategory = true
        label.translatesAutoresizingMaskIntoConstraints = false
        view.contentView.addSubview(label)
        NSLayoutConstraint.activate([
            label.centerXAnchor.constraint(equalTo: view.contentView.centerXAnchor),
            label.centerYAnchor.constraint(equalTo: view.contentView.centerYAnchor),
        ])
        return view
    }

    func updateUIView(_ view: UIVisualEffectView, context: Context) {
        let effect = UIGlassEffect(style: .regular)
        effect.tintColor = isTinted ? tint.withAlphaComponent(0.6) : nil
        effect.isInteractive = isInteractive
        // Assigning the effect inside an animation block materializes the glass smoothly.
        UIView.animate(withDuration: 0.3) {
            view.effect = effect
        }
    }
}

#Preview("Light") {
    NavigationStack { GlassViewScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassViewScreen() }
        .preferredColorScheme(.dark)
}
