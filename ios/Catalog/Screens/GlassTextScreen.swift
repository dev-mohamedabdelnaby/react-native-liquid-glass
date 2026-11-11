//
//  GlassTextScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates text on Liquid Glass: titles, paragraphs, tags, text fields,
/// vibrant label hierarchy, clear glass legibility, and glass-filled letterforms.
struct GlassTextScreen: View {
    @State private var message = ""
    @State private var username = ""

    var body: some View {
        ScrollView {
            VStack(spacing: 32) {
                section("Glass letterforms") {
                    GlassLettering(text: "Liquid", font: .system(size: 88, weight: .black, design: .rounded))
                    GlassLettering(text: "GLASS", font: .system(size: 64, weight: .heavy))
                }

                section("Titles") {
                    Text("Large Title on Glass")
                        .font(.largeTitle.bold())
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .appGlassEffect()

                    Text("Tinted headline")
                        .font(.title2.weight(.semibold))
                        .padding(.horizontal, 20)
                        .padding(.vertical, 10)
                        .glassEffect(.regular.tint(.purple))
                }

                section("Paragraph card") {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Liquid Glass")
                            .font(.headline)
                        // Hierarchical styles stay vibrant and legible on glass.
                        Text("A dynamic material that blurs content behind it and reflects surrounding color and light.")
                            .font(.body)
                            .foregroundStyle(.secondary)
                        Text("Updated just now")
                            .font(.caption)
                            .foregroundStyle(.tertiary)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(20)
                    .appGlassEffect(in: .rect(cornerRadius: 24))
                }

                section("Tags") {
                    GlassEffectContainer(spacing: 8) {
                        HStack(spacing: 8) {
                            ForEach(["SwiftUI", "iOS 26", "Design"], id: \.self) { tag in
                                Text(tag)
                                    .font(.subheadline.weight(.medium))
                                    .padding(.horizontal, 14)
                                    .padding(.vertical, 8)
                                    .appGlassEffect(.regular.interactive())
                            }
                        }
                    }
                }

                section("Clear glass with dimming") {
                    // Clear glass needs a dimming layer underneath to keep text legible.
                    Label("Readable over rich backgrounds", systemImage: "eye")
                        .font(.headline)
                        .foregroundStyle(.white)
                        .padding()
                        .background(.black.opacity(0.35), in: .capsule)
                        .appGlassEffect(.clear)
                }

                section("Text fields") {
                    TextField("Username", text: $username)
                        .textFieldStyle(.plain)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 14)
                        .appGlassEffect(.regular.interactive())

                    HStack(spacing: 8) {
                        TextField("Message", text: $message, axis: .vertical)
                            .textFieldStyle(.plain)
                            .lineLimit(1...4)
                            .padding(.horizontal, 20)
                            .padding(.vertical, 14)
                            .appGlassEffect(.regular.interactive(), in: .rect(cornerRadius: 24))

                        Button("Send", systemImage: "arrow.up") {
                            message = ""
                        }
                        .labelStyle(.iconOnly)
                        .font(.headline)
                        .buttonStyle(.glassProminent)
                        .buttonBorderShape(.circle)
                        .controlSize(.large)
                        .disabled(message.isEmpty)
                    }
                }
            }
            .padding()
        }
        .scrollDismissesKeyboard(.interactively)
        .glassBackdrop()
        .navigationTitle("Text")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func section<Content: View>(_ title: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(spacing: 12) {
            Text(title)
                .font(.headline)
                .foregroundStyle(.primary)
            content()
        }
        .frame(maxWidth: .infinity)
    }
}

/// Renders text whose letterforms are filled with Liquid Glass.
///
/// `glassEffect` only accepts shapes, so this renders a glass rectangle sized to the
/// text and masks it with the text itself, leaving glass only inside the letters.
private struct GlassLettering: View {
    let text: String
    let font: Font

    var body: some View {
        Text(text)
            .font(font)
            .hidden()
            .overlay {
                Color.clear
                    .appGlassEffect(.regular, in: .rect)
                    .mask {
                        Text(text)
                            .font(font)
                    }
            }
            // A subtle stroke-like shadow helps the glass letters stand out.
            .shadow(color: .black.opacity(0.15), radius: 6, y: 3)
            .accessibilityLabel(text)
    }
}

#Preview("Light") {
    NavigationStack { GlassTextScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassTextScreen() }
        .preferredColorScheme(.dark)
}
