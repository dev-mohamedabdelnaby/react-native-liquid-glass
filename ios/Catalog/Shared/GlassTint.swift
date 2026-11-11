//
//  GlassTint.swift
//  liquidGlass
//

import SwiftUI

/// The app-wide glass tint picked on the home screen; `.none` keeps glass untinted.
enum GlassTintChoice: String, CaseIterable, Identifiable {
    case none, blue, purple, pink, orange, green, teal

    var id: Self { self }

    var title: String { self == .none ? "None" : rawValue.capitalized }

    var color: Color? {
        switch self {
        case .none: nil
        case .blue: .blue
        case .purple: .purple
        case .pink: .pink
        case .orange: .orange
        case .green: .green
        case .teal: .teal
        }
    }
}

extension EnvironmentValues {
    /// The tint every untinted glass in the app takes; `nil` for none.
    @Entry var glassTint: Color? = nil
}

extension View {
    /// `glassEffect` that takes the app-wide tint. Use it for glass without a tint of its own;
    /// glass with its own tint keeps calling `glassEffect` so its color wins.
    func appGlassEffect(_ glass: Glass = .regular, in shape: some Shape = DefaultGlassEffectShape()) -> some View {
        modifier(AppGlassEffect(glass: glass, shape: shape))
    }
}

private struct AppGlassEffect<S: Shape>: ViewModifier {
    let glass: Glass
    let shape: S
    @Environment(\.glassTint) private var tint

    func body(content: Content) -> some View {
        // A light tint, so the glass keeps showing what's behind it.
        content.glassEffect(tint.map { glass.tint($0.opacity(0.35)) } ?? glass, in: shape)
    }
}
