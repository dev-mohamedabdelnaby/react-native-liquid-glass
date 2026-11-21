//
//  GlassUnionScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates combining separate views into one glass shape with `glassEffectUnion(id:namespace:)`.
/// Each symbol opens a glass menu with actions for that forecast.
struct GlassUnionScreen: View {
    /// A forecast shown as one symbol in a union group.
    struct Forecast: Identifiable, Hashable {
        let symbol: String
        let name: String
        let detail: String
        let group: String

        var id: String { symbol }
    }

    @Namespace private var namespace
    @State private var selected: Forecast?
    @State private var alerts: Set<String> = []

    private let forecasts = [
        Forecast(symbol: "cloud.bolt.rain.fill", name: "Thunderstorm", detail: "18° · 80% rain · Afternoon", group: "day"),
        Forecast(symbol: "sun.rain.fill", name: "Sun Showers", detail: "22° · 40% rain · Morning", group: "day"),
        Forecast(symbol: "moon.stars.fill", name: "Clear Night", detail: "14° · Starry skies · Evening", group: "night"),
        Forecast(symbol: "moon.fill", name: "Calm Night", detail: "12° · Light wind · Late night", group: "night"),
    ]

    var body: some View {
        VStack(spacing: 32) {
            Text("Day and night symbols are grouped into two unions. Tap a symbol for its menu.")
                .glassFont(.headline)
                .multilineTextAlignment(.center)
                .foregroundStyle(.primary)

            GlassEffectContainer(spacing: 20) {
                HStack(spacing: 20) {
                    ForEach(forecasts) { forecast in
                        Menu {
                            menuContent(for: forecast)
                        } label: {
                            Image(systemName: forecast.symbol)
                                .symbolRenderingMode(.multicolor)
                                .font(.system(size: 30))
                                .frame(width: 64, height: 64)
                                .overlay(alignment: .topTrailing) {
                                    if alerts.contains(forecast.id) {
                                        Image(systemName: "bell.fill")
                                            .font(.caption2)
                                            .foregroundStyle(.orange)
                                            .padding(6)
                                    }
                                }
                        }
                        .buttonStyle(.plain)
                        .appGlassEffect(.regular.interactive())
                        .glassEffectUnion(id: forecast.group, namespace: namespace)
                    }
                }
            }

            if let selected {
                VStack(spacing: 8) {
                    Image(systemName: selected.symbol)
                        .symbolRenderingMode(.multicolor)
                        .font(.system(size: 44))
                    Text(selected.name)
                        .glassFont(.title2, weight: .bold)
                    Text(selected.detail)
                        .foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity)
                .padding(24)
                .appGlassEffect(in: .rect(cornerRadius: 28))
                .transition(.blurReplace)
                .id(selected)
            }
        }
        .padding()
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .glassBackdrop()
        .navigationTitle("Union")
        .navigationBarTitleDisplayMode(.inline)
    }

    @ViewBuilder
    private func menuContent(for forecast: Forecast) -> some View {
        Section("\(forecast.name) · \(forecast.detail)") {
            Button("Show Details", systemImage: "info.circle") {
                withAnimation(.smooth) { selected = forecast }
            }

            Button(
                alerts.contains(forecast.id) ? "Remove Alert" : "Set Alert",
                systemImage: alerts.contains(forecast.id) ? "bell.slash" : "bell"
            ) {
                withAnimation {
                    if alerts.contains(forecast.id) {
                        alerts.remove(forecast.id)
                    } else {
                        alerts.insert(forecast.id)
                    }
                }
            }

            ShareLink(item: "\(forecast.name): \(forecast.detail)")
        }

        if selected == forecast {
            Button("Hide Details", systemImage: "xmark", role: .destructive) {
                withAnimation(.smooth) { selected = nil }
            }
        }
    }
}

#Preview("Light") {
    NavigationStack { GlassUnionScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassUnionScreen() }
        .preferredColorScheme(.dark)
}
