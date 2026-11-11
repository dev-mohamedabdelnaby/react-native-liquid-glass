//
//  GlassSheetScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates sheets, popovers and menus, which use Liquid Glass by default.
struct GlassSheetScreen: View {
    @State private var isSheetPresented = false
    @State private var isPopoverPresented = false
    @State private var selection = "None"

    var body: some View {
        VStack(spacing: 24) {
            Button("Show Sheet") { isSheetPresented = true }
                .buttonStyle(.glassProminent)

            Button("Show Popover") { isPopoverPresented = true }
                .buttonStyle(.glass)
                .popover(isPresented: $isPopoverPresented) {
                    Text("A glass popover")
                        .padding()
                        .presentationCompactAdaptation(.popover)
                }

            Menu("Menu: \(selection)") {
                Button("Option A") { selection = "A" }
                Button("Option B") { selection = "B" }
                Button("Option C") { selection = "C" }
            }
            .buttonStyle(.glass)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .glassBackdrop()
        .navigationTitle("Sheets & Menus")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $isSheetPresented) {
            VStack(spacing: 16) {
                Text("Partial-height sheets float on Liquid Glass")
                    .font(.headline)
                    .multilineTextAlignment(.center)
                Button("Done") { isSheetPresented = false }
                    .buttonStyle(.glass)
            }
            .padding()
            .presentationDetents([.medium, .large])
        }
    }
}

#Preview("Light") {
    NavigationStack { GlassSheetScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassSheetScreen() }
        .preferredColorScheme(.dark)
}
