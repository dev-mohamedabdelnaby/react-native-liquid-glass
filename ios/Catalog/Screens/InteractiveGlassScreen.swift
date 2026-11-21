//
//  InteractiveGlassScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates `Glass.interactive()`, which makes custom glass react to touch.
struct InteractiveGlassScreen: View {
    @State private var tapCount = 0

    var body: some View {
        VStack(spacing: 32) {
            Text("Press and hold the shapes")
                .glassFont(.headline)
                .foregroundStyle(.primary)

            Image(systemName: "hand.tap.fill")
                .font(.system(size: 44))
                .frame(width: 120, height: 120)
                .appGlassEffect(.regular.interactive(), in: .circle)
                .onTapGesture { tapCount += 1 }

            Text("Tapped \(tapCount) times")
                .glassFont(.title3).monospacedDigit()
                .padding()
                .glassEffect(.regular.tint(.purple).interactive())
                .onTapGesture { tapCount += 1 }

            HStack(spacing: 20) {
                Text("Static")
                    .padding()
                    .appGlassEffect()

                Text("Interactive")
                    .padding()
                    .appGlassEffect(.regular.interactive())
            }
        }
        .padding()
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .glassBackdrop()
        .navigationTitle("Interactive")
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview("Light") {
    NavigationStack { InteractiveGlassScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { InteractiveGlassScreen() }
        .preferredColorScheme(.dark)
}
