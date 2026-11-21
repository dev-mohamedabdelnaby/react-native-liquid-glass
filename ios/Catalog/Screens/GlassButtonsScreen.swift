//
//  GlassButtonsScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates the `.glass`, `.glassProminent` and `.glass(_:)` button styles.
struct GlassButtonsScreen: View {
    @State private var lastAction = "None"

    var body: some View {
        VStack(spacing: 24) {
            Text("Last action: \(lastAction)")
                .glassFont(.headline)
                .foregroundStyle(.primary)

            Button("Glass") { lastAction = "Glass" }
                .buttonStyle(.glass)

            Button("Glass Prominent") { lastAction = "Prominent" }
                .buttonStyle(.glassProminent)

            Button("Clear Glass") { lastAction = "Clear" }
                .buttonStyle(.glass(.clear))

            Button("Tinted Prominent") { lastAction = "Tinted" }
                .buttonStyle(.glassProminent)
                .tint(.orange)

            Button("Large", systemImage: "bolt.fill") { lastAction = "Large" }
                .buttonStyle(.glass)
                .controlSize(.large)

            HStack(spacing: 16) {
                ForEach(["play.fill", "pause.fill", "forward.fill"], id: \.self) { symbol in
                    Button {
                        lastAction = symbol
                    } label: {
                        Image(systemName: symbol)
                            .frame(width: 32, height: 32)
                    }
                    .buttonStyle(.glass)
                    .buttonBorderShape(.circle)
                }
            }

            Button("Disabled") {}
                .buttonStyle(.glass)
                .disabled(true)
        }
        .padding()
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .glassBackdrop()
        .navigationTitle("Buttons")
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview("Light") {
    NavigationStack { GlassButtonsScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassButtonsScreen() }
        .preferredColorScheme(.dark)
}
