//
//  GlassTransitionScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates `glassEffectTransition(_:)` with the `.matchedGeometry`, `.materialize` and `.identity` transitions.
struct GlassTransitionScreen: View {
    /// The available glass effect transitions, wrapped so they can drive a picker.
    enum TransitionKind: String, CaseIterable, Identifiable {
        case matchedGeometry = "Matched"
        case materialize = "Materialize"
        case identity = "Identity"

        var id: Self { self }

        var transition: GlassEffectTransition {
            switch self {
            case .matchedGeometry: .matchedGeometry
            case .materialize: .materialize
            case .identity: .identity
            }
        }
    }

    @State private var kind: TransitionKind = .matchedGeometry
    @State private var isShowingBadges = false
    @Namespace private var namespace

    private let badges = ["star.fill", "heart.fill", "bolt.fill"]

    var body: some View {
        VStack(spacing: 32) {
            Picker("Transition", selection: $kind) {
                ForEach(TransitionKind.allCases) { kind in
                    Text(kind.rawValue).tag(kind)
                }
            }
            .pickerStyle(.segmented)

            Spacer()

            GlassEffectContainer(spacing: 24) {
                HStack(spacing: 24) {
                    GlassImage("person.fill")
                        .font(.title)
                        .frame(width: 72, height: 72)
                        .appGlassEffect()
                        .glassEffectID("avatar", in: namespace)

                    if isShowingBadges {
                        ForEach(badges, id: \.self) { badge in
                            GlassImage(badge)
                                .font(.title2)
                                .frame(width: 56, height: 56)
                                .glassEffect(.regular.tint(.accentColor.opacity(0.4)))
                                .glassEffectID(badge, in: namespace)
                                .glassEffectTransition(kind.transition)
                        }
                    }
                }
            }

            Spacer()

            Text(description)
                .glassFont(.footnote)
                .multilineTextAlignment(.center)
                .padding()
                .appGlassEffect(in: .rect(cornerRadius: 20))

            Button(isShowingBadges ? "Hide Badges" : "Show Badges") {
                withAnimation(.bouncy) {
                    isShowingBadges.toggle()
                }
            }
            .buttonStyle(.glassProminent)
            .controlSize(.large)
        }
        .padding()
        .glassBackdrop()
        .navigationTitle("Transitions")
        .navigationBarTitleDisplayMode(.inline)
    }

    private var description: String {
        switch kind {
        case .matchedGeometry:
            "Badges grow out of the nearby avatar shape, since they're within the container's spacing."
        case .materialize:
            "Badges fade and materialize in place — best for shapes far from each other."
        case .identity:
            "No glass animation: badges appear and disappear instantly."
        }
    }
}

#Preview("Light") {
    NavigationStack { GlassTransitionScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassTransitionScreen() }
        .preferredColorScheme(.dark)
}
