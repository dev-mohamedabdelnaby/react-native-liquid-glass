//
//  GlassSearchScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates the Liquid Glass search field in the bottom toolbar, including the minimized search button.
struct GlassSearchScreen: View {
    @State private var searchText = ""
    @State private var isMinimized = true

    private let fruits = [
        "Apple", "Apricot", "Banana", "Blueberry", "Cherry", "Coconut", "Date", "Fig",
        "Grape", "Kiwi", "Lemon", "Lime", "Mango", "Melon", "Orange", "Papaya",
        "Peach", "Pear", "Pineapple", "Plum", "Raspberry", "Strawberry", "Watermelon",
    ]

    private var results: [String] {
        searchText.isEmpty ? fruits : fruits.filter { $0.localizedStandardContains(searchText) }
    }

    var body: some View {
        List {
            Section {
                Toggle("Minimize search field", isOn: $isMinimized)
            } footer: {
                Text("When minimized, search appears as a glass button until tapped.").glassFontOnly(.footnote)
            }

            Section {
                ForEach(results, id: \.self) { fruit in
                    Text(fruit)
                }
            } header: {
                Text("Fruits").glassFontOnly(.headline)
            }
        }
        .navigationTitle("Search")
        .navigationBarTitleDisplayMode(.inline)
        .searchable(text: $searchText, prompt: "Search fruits")
        .searchToolbarBehavior(isMinimized ? .minimize : .automatic)
        .toolbar {
            ToolbarItem(placement: .bottomBar) {
                Button("Filter", glassImage: "line.3.horizontal.decrease", uiKit: true) {}
            }

            ToolbarSpacer(.flexible, placement: .bottomBar)

            // Places the system search field in the bottom glass toolbar.
            DefaultToolbarItem(kind: .search, placement: .bottomBar)

            ToolbarSpacer(.flexible, placement: .bottomBar)

            ToolbarItem(placement: .bottomBar) {
                Button("Add", glassImage: "plus", uiKit: true) {}
            }
        }
    }
}

#Preview("Light") {
    NavigationStack { GlassSearchScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassSearchScreen() }
        .preferredColorScheme(.dark)
}
