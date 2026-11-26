//
//  GlassEditSearchScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates two glass buttons that morph into each other's tools:
/// Search expands into a full search field (hiding Edit), and Edit turns into
/// Delete + Done (hiding Search). Finishing either returns to the two buttons.
struct GlassEditSearchScreen: View {
    /// What the glass action bar is currently showing.
    enum Mode {
        case idle, searching, editing
    }

    @State private var mode: Mode = .idle
    @State private var searchText = ""
    @State private var selection: Set<String> = []
    @State private var items = [
        "Groceries", "Workout plan", "Trip to Tokyo", "Book list", "Recipes",
        "Gift ideas", "Meeting notes", "Budget", "Garden", "Movies to watch",
        "Packing list", "Side project", "Birthday party", "Reading notes",
    ]
    @FocusState private var isSearchFocused: Bool
    @Namespace private var namespace

    private var visibleItems: [String] {
        searchText.isEmpty ? items : items.filter { $0.localizedStandardContains(searchText) }
    }

    var body: some View {
        List(selection: $selection) {
            ForEach(visibleItems, id: \.self) { item in
                Label(item, glassImage: "note.text")
            }
            .onDelete { offsets in
                let removed = offsets.map { visibleItems[$0] }
                items.removeAll { removed.contains($0) }
            }
            .onMove { source, destination in
                items.move(fromOffsets: source, toOffset: destination)
            }
        }
        .environment(\.editMode, .constant(mode == .editing ? .active : .inactive))
        .overlay {
            if mode == .searching && visibleItems.isEmpty {
                ContentUnavailableView.search(text: searchText)
            }
        }
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
        .safeAreaBar(edge: .bottom) {
            actionBar
                .padding()
        }
    }

    private var title: String {
        switch mode {
        case .idle: "Notes"
        case .searching: "Search"
        case .editing: selection.isEmpty ? "Select Items" : "\(selection.count) Selected"
        }
    }

    /// The glass bar whose shapes morph between the idle, searching and editing states.
    private var actionBar: some View {
        GlassEffectContainer(spacing: 12) {
            HStack(spacing: 12) {
                switch mode {
                case .idle:
                    Spacer()
                    circleButton("Edit", systemImage: "pencil", id: "edit") {
                        setMode(.editing)
                    }
                    circleButton("Search", systemImage: "magnifyingglass", id: "search") {
                        setMode(.searching)
                    }

                case .searching:
                    searchField
                    circleButton("Close Search", systemImage: "xmark", id: "close") {
                        searchText = ""
                        setMode(.idle)
                    }

                case .editing:
                    if !selection.isEmpty {
                        Button(role: .destructive) {
                            withAnimation(.bouncy) {
                                items.removeAll { selection.contains($0) }
                                selection.removeAll()
                            }
                        } label: {
                            Label("Delete \(selection.count)", glassImage: "trash")
                                .glassFont(.headline)
                                .foregroundStyle(.white)
                                .padding(.horizontal, 20)
                                .frame(height: 52)
                        }
                        .buttonStyle(.plain)
                        .glassEffect(.regular.tint(.red).interactive())
                        .glassEffectID("delete", in: namespace)
                    }

                    Spacer()

                    // Done morphs out of the Edit button and returns to the two icons.
                    circleButton("Done", systemImage: "checkmark", id: "edit", tint: .blue) {
                        selection.removeAll()
                        setMode(.idle)
                    }
                }
            }
        }
    }

    /// The expanded search field, morphing out of the Search button.
    private var searchField: some View {
        HStack(spacing: 8) {
            GlassImage("magnifyingglass")
                .foregroundStyle(.secondary)
            TextField("Search notes", text: $searchText)
                .focused($isSearchFocused)
                .submitLabel(.search)
            if !searchText.isEmpty {
                Button("Clear", glassImage: "xmark.circle.fill") {
                    searchText = ""
                }
                .labelStyle(.iconOnly)
                .foregroundStyle(.secondary)
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 18)
        .frame(height: 52)
        .appGlassEffect(.regular.interactive())
        .glassEffectID("search", in: namespace)
    }

    private func circleButton(
        _ title: String,
        systemImage: String,
        id: String,
        tint: Color? = nil,
        action: @escaping () -> Void
    ) -> some View {
        GlassCircleButton(title: title, systemImage: systemImage, tint: tint, action: action)
            .glassEffectID(id, in: namespace)
    }

    private func setMode(_ newMode: Mode) {
        withAnimation(.bouncy) {
            mode = newMode
        }
        isSearchFocused = newMode == .searching
    }
}

/// A circular glass icon button, optionally tinted.
private struct GlassCircleButton: View {
    let title: String
    let systemImage: String
    var tint: Color?
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            GlassImage(systemImage)
                .font(.title3.weight(.semibold))
                .foregroundStyle(tint == nil ? Color.primary : Color.white)
                .frame(width: 52, height: 52)
        }
        .buttonStyle(.plain)
        .glassEffect(.regular.tint(tint).interactive(), in: .circle)
        .accessibilityLabel(title)
    }
}

#Preview("Light") {
    NavigationStack { GlassEditSearchScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassEditSearchScreen() }
        .preferredColorScheme(.dark)
}
