//
//  GlassEffectScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates the basic `glassEffect(_:in:)` modifier with variants, tints and shapes.
/// Every item opens a glass menu that changes how it looks.
struct GlassEffectScreen: View {
    @State private var greeting = "Hello, Liquid Glass!"
    @State private var isGreetingClear = false
    @State private var isClearDimmed = false
    @State private var chipTints: [String: Color] = ["Orange": .orange, "Blue": .blue, "Green": .green]
    @State private var isStarFilled = true
    @State private var cornerRadius: CGFloat = 16
    @State private var isRectTinted = false

    private let palette: [(name: String, color: Color)] = [
        ("Orange", .orange), ("Blue", .blue), ("Green", .green),
        ("Pink", .pink), ("Purple", .purple), ("Teal", .teal),
    ]

    var body: some View {
        ScrollView {
            VStack(spacing: 28) {
                Text("Tap any glass item for its menu")
                    .glassFont(.footnote)
                    .foregroundStyle(.secondary)

                section("Regular (default capsule)") {
                    Menu {
                        Picker("Variant", selection: $isGreetingClear) {
                            Label("Regular", systemImage: "drop.fill").tag(false)
                            Label("Clear", systemImage: "drop").tag(true)
                        }
                        Section("Greeting") {
                            ForEach(["Hello, Liquid Glass!", "Hi there 👋", "Welcome!"], id: \.self) { text in
                                Button(text) { withAnimation { greeting = text } }
                            }
                        }
                        Button("Copy Text", systemImage: "doc.on.doc") {
                            UIPasteboard.general.string = greeting
                        }
                    } label: {
                        Text(greeting)
                            .glassFont(.title2)
                            .padding()
                            .contentTransition(.interpolate)
                    }
                    .buttonStyle(.plain)
                    .appGlassEffect(isGreetingClear ? .clear.interactive() : .regular.interactive())
                }

                section("Clear variant") {
                    Menu {
                        Toggle("Dimming Layer", systemImage: "circle.lefthalf.filled", isOn: $isClearDimmed.animation())
                    } label: {
                        Label("Clear glass", systemImage: "drop")
                            .glassFont(.title3)
                            .padding()
                    }
                    .buttonStyle(.plain)
                    // A dimming layer underneath keeps text legible on clear glass.
                    .background(isClearDimmed ? .black.opacity(0.35) : .clear, in: .capsule)
                    .appGlassEffect(.clear.interactive())
                }

                section("Tinted") {
                    HStack(spacing: 16) {
                        ForEach(["Orange", "Blue", "Green"], id: \.self) { chip in
                            tintedChip(chip)
                        }
                    }
                }

                section("Custom shapes") {
                    HStack(spacing: 16) {
                        Menu {
                            Button(isStarFilled ? "Unfavorite" : "Favorite",
                                   systemImage: isStarFilled ? "star.slash" : "star") {
                                isStarFilled.toggle()
                            }
                        } label: {
                            Image(systemName: isStarFilled ? "star.fill" : "star")
                                .font(.title)
                                .contentTransition(.symbolEffect(.replace))
                                .frame(width: 70, height: 70)
                        }
                        .buttonStyle(.plain)
                        .appGlassEffect(.regular.interactive(), in: .circle)

                        Menu {
                            Picker("Corner Radius", selection: $cornerRadius.animation(.smooth)) {
                                ForEach([4, 16, 32] as [CGFloat], id: \.self) { radius in
                                    Text("\(Int(radius)) pt").tag(radius)
                                }
                            }
                        } label: {
                            Text("Rounded")
                                .padding()
                        }
                        .buttonStyle(.plain)
                        .appGlassEffect(.regular.interactive(), in: .rect(cornerRadius: cornerRadius))

                        Menu {
                            Toggle("Tint", systemImage: "paintpalette", isOn: $isRectTinted.animation())
                        } label: {
                            Text("Rect")
                                .padding()
                        }
                        .buttonStyle(.plain)
                        .glassEffect(isRectTinted ? .regular.tint(.purple).interactive() : .regular.interactive(), in: .rect)
                    }
                }
            }
            .padding()
        }
        .glassBackdrop()
        .navigationTitle("Glass Effect")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func tintedChip(_ chip: String) -> some View {
        Menu {
            Section("Tint") {
                ForEach(palette, id: \.name) { option in
                    Button(option.name) {
                        withAnimation { chipTints[chip] = option.color }
                    }
                }
            }
            Button("Remove Tint", systemImage: "xmark.circle") {
                withAnimation { chipTints[chip] = .clear }
            }
        } label: {
            Text(chip)
                .padding()
        }
        .buttonStyle(.plain)
        .glassEffect(.regular.tint(chipTints[chip] ?? .clear).interactive())
    }

    private func section<Content: View>(_ title: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(spacing: 12) {
            Text(title)
                .glassFont(.headline)
                .foregroundStyle(.primary)
            content()
        }
        .frame(maxWidth: .infinity)
    }
}

#Preview("Light") {
    NavigationStack { GlassEffectScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassEffectScreen() }
        .preferredColorScheme(.dark)
}
