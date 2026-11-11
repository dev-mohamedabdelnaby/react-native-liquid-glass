//
//  GlassControlsScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates standard controls that take on a Liquid Glass look while being interacted with.
struct GlassControlsScreen: View {
    @State private var volume = 0.5
    @State private var isOn = true
    @State private var segment = 0
    @State private var stepperValue = 3
    @State private var date = Date()
    @State private var color = Color.blue
    @State private var flavor = "Vanilla"
    @State private var progress = 0.6

    var body: some View {
        Form {
            Section("Slider") {
                Slider(value: $volume) {
                    Text("Volume")
                } minimumValueLabel: {
                    Image(systemName: "speaker")
                } maximumValueLabel: {
                    Image(systemName: "speaker.wave.3")
                }
            }

            Section("Toggle") {
                Toggle("Notifications", isOn: $isOn)
            }

            Section("Segmented Picker") {
                Picker("Mode", selection: $segment) {
                    Text("Day").tag(0)
                    Text("Week").tag(1)
                    Text("Month").tag(2)
                }
                .pickerStyle(.segmented)
            }

            Section {
                Stepper("Cups: \(stepperValue)", value: $stepperValue, in: 0...10)

                HStack {
                    Text("Order total")
                    Spacer()
                    Text(Double(stepperValue) * 3.5, format: .currency(code: "USD"))
                        .monospacedDigit()
                        .contentTransition(.numericText())
                }

                HStack {
                    Text("Quick pick")
                    Spacer()
                    GlassStepper(value: $stepperValue, range: 0...10)
                }
            } header: {
                Text("Stepper")
            } footer: {
                Text("Tap the number in the glass stepper for a menu of presets.")
            }

            Section("Menu Picker") {
                // Menu pickers present their options in a glass menu.
                Picker("Flavor", selection: $flavor) {
                    ForEach(["Vanilla", "Chocolate", "Strawberry"], id: \.self) { flavor in
                        Text(flavor).tag(flavor)
                    }
                }
                .pickerStyle(.menu)
            }

            Section("Date & Color Pickers") {
                DatePicker("Date", selection: $date, displayedComponents: .date)
                DatePicker("Time", selection: $date, displayedComponents: .hourAndMinute)
                ColorPicker("Accent", selection: $color)
            }

            Section("Progress") {
                ProgressView(value: progress)
                    .tint(color)
                Slider(value: $progress)
                    .tint(color)
            }
        }
        .navigationTitle("Controls")
        .navigationBarTitleDisplayMode(.inline)
    }
}

/// A custom − / value / + stepper on Liquid Glass; tapping the value opens a menu of presets.
private struct GlassStepper: View {
    @Binding var value: Int
    let range: ClosedRange<Int>

    var body: some View {
        HStack(spacing: 0) {
            Button("Decrease", systemImage: "minus") {
                withAnimation { value = max(range.lowerBound, value - 1) }
            }
            .disabled(value <= range.lowerBound)

            Divider().frame(height: 20)

            Menu {
                Section("Presets") {
                    ForEach([1, 2, 5, 10], id: \.self) { preset in
                        Button("\(preset) cups") {
                            withAnimation { value = preset }
                        }
                    }
                }
                Button("Reset", systemImage: "arrow.counterclockwise", role: .destructive) {
                    withAnimation { value = range.lowerBound }
                }
            } label: {
                Text("\(value)")
                    .font(.headline.monospacedDigit())
                    .contentTransition(.numericText())
                    .frame(minWidth: 36)
            }

            Divider().frame(height: 20)

            Button("Increase", systemImage: "plus") {
                withAnimation { value = min(range.upperBound, value + 1) }
            }
            .disabled(value >= range.upperBound)
        }
        .labelStyle(.iconOnly)
        .buttonStyle(.plain)
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .appGlassEffect(.regular.interactive())
    }
}

#Preview("Light") {
    NavigationStack { GlassControlsScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassControlsScreen() }
        .preferredColorScheme(.dark)
}
