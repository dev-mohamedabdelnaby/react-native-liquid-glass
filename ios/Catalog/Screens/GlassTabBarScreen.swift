//
//  GlassTabBarScreen.swift
//  liquidGlass
//

import SwiftUI

/// Demonstrates Liquid Glass tab bar layouts: with a Now Playing accessory, search inside
/// the bar, search as a separate glass circle, and tabs without search.
///
/// A `TabView` must be the root of its screen, with one `NavigationStack` per tab.
/// Pushing it inside the catalog's `NavigationStack` nests stacks, which makes the
/// search tab pop back to the catalog, so each demo is presented full screen instead.
struct GlassTabBarScreen: View {
    @State private var presentedVariant: TabBarVariant?
    @State private var minimizesOnScroll = true

    var body: some View {
        VStack(spacing: 20) {
            Text("Floating glass tab bars. Pick a layout below.")
                .multilineTextAlignment(.center)
                .padding()
                .appGlassEffect(in: .rect(cornerRadius: 24))

            Toggle("Minimize on scroll", systemImage: "arrow.down.right.and.arrow.up.left", isOn: $minimizesOnScroll)
                .padding()
                .appGlassEffect(in: .rect(cornerRadius: 20))

            ForEach(TabBarVariant.allCases) { variant in
                Button {
                    presentedVariant = variant
                } label: {
                    Label(variant.title, systemImage: variant.systemImage)
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.glassProminent)
                .controlSize(.large)
            }
        }
        .padding()
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .glassBackdrop()
        .navigationTitle("Tab Bar")
        .navigationBarTitleDisplayMode(.inline)
        .fullScreenCover(item: $presentedVariant) { variant in
            TabBarDemo(variant: variant, minimizesOnScroll: minimizesOnScroll)
                // A fresh identity per variant so no state carries over between demos.
                .id(variant)
        }
    }
}

/// The tab bar layouts this screen can demo.
enum TabBarVariant: String, CaseIterable, Identifiable {
    case nowPlaying
    case searchInBar
    case searchSeparated
    case noSearch

    var id: Self { self }

    var title: String {
        switch self {
        case .nowPlaying: "With Now Playing Bar"
        case .searchInBar: "Search Inside Tabs"
        case .searchSeparated: "Search Separated"
        case .noSearch: "Tabs Without Search"
        }
    }

    var systemImage: String {
        switch self {
        case .nowPlaying: "music.note"
        case .searchInBar: "rectangle.and.text.magnifyingglass"
        case .searchSeparated: "magnifyingglass.circle"
        case .noSearch: "dock.rectangle"
        }
    }
}

/// The tabs shown in every demo.
private enum DemoTab: String, CaseIterable, Identifiable {
    case home, library, profile

    var id: Self { self }

    var title: String { rawValue.capitalized }

    var systemImage: String {
        switch self {
        case .home: "house"
        case .library: "books.vertical"
        case .profile: "person.crop.circle"
        }
    }

    /// Unread count shown as a badge, matching a typical app layout.
    var badge: Int { self == .library ? 3 : 0 }
}

// MARK: - Tab bar demos

/// A full-screen demo using the system `TabView` tab bar, which provides the Liquid Glass
/// capsule, the selection drop and sliding between tabs.
private struct TabBarDemo: View {
    let variant: TabBarVariant
    let minimizesOnScroll: Bool

    @Environment(\.dismiss) private var dismiss
    @State private var searchText = ""
    /// On its own, `.onScrollDown` only expands the bar back at the top. While the content scrolls
    /// up, the bar may not minimize, which expands it; scrolling down allows minimizing again.
    @State private var isScrollingUp = false

    var body: some View {
        Group {
            switch variant {
            case .nowPlaying:
                TabView {
                    mainTabs
                    Tab(role: .search) {
                        DemoSearchList(searchText: $searchText, onClose: close)
                    }
                }
                .searchable(text: $searchText)
                .tabViewBottomAccessory {
                    Label("Now Playing", systemImage: "music.note")
                        .padding(.horizontal)
                }

            case .searchInBar:
                TabView {
                    mainTabs
                    // A regular tab: search sits inside the same glass capsule as the other tabs.
                    Tab("Search", systemImage: "magnifyingglass") {
                        DemoSearchList(searchText: $searchText, onClose: close)
                            // `.always` keeps the field visible instead of hiding it until you pull down.
                            .searchable(
                                text: $searchText,
                                placement: .navigationBarDrawer(displayMode: .always)
                            )
                    }
                }

            case .searchSeparated:
                TabView {
                    mainTabs
                    // The search role pins search as its own glass circle beside the tabs' capsule.
                    // Selecting it morphs the circle into a search field in the bar.
                    Tab(role: .search) {
                        DemoSearchList(searchText: $searchText, onClose: close)
                    }
                }
                .searchable(text: $searchText)
                // Tapping the circle opens the field right away; cancelling returns to the previous tab.
                .tabViewSearchActivation(.searchTabSelection)

            case .noSearch:
                TabView {
                    mainTabs
                    Tab("Settings", systemImage: "gearshape") {
                        DemoTabContent(title: "Settings", onClose: close) { isScrollingUp = !$0 }
                    }
                }
            }
        }
        .tabBarMinimizeBehavior(minimizesOnScroll && !isScrollingUp ? .onScrollDown : .never)
    }

    @TabContentBuilder<Never>
    private var mainTabs: some TabContent<Never> {
        ForEach(DemoTab.allCases) { tab in
            Tab(tab.title, systemImage: tab.systemImage) {
                DemoTabContent(title: tab.title, onClose: close) { isScrollingUp = !$0 }
            }
            .badge(tab.badge)
        }
    }

    private func close() {
        dismiss()
    }
}

// MARK: - Shared demo content

/// A scrolling list of items for one tab.
private struct DemoTabContent: View {
    let title: String
    let onClose: () -> Void
    /// Reports the scroll direction: `true` scrolling down, `false` scrolling up.
    var onScrollDirection: ((Bool) -> Void)?

    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(1...30, id: \.self) { index in
                        Text("\(title) item \(index)")
                            .frame(maxWidth: .infinity, minHeight: 60)
                            .background(.thinMaterial, in: .rect(cornerRadius: 12))
                    }
                }
                .padding()
            }
            .onScrollGeometryChange(for: CGFloat.self) { geometry in
                geometry.contentOffset.y + geometry.contentInsets.top
            } action: { oldOffset, newOffset in
                // Skip tiny movements and the top 20 pt (the bounce there). A small step down near
                // the top must not count as scrolling up: switching to `.never` mid-gesture keeps
                // the bar from minimizing.
                guard let onScrollDirection, abs(newOffset - oldOffset) > 4, newOffset > 20 else { return }
                onScrollDirection(newOffset > oldOffset)
            }
            .glassBackdrop()
            .navigationTitle(title)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Close", systemImage: "xmark", action: onClose)
                }
            }
        }
    }
}

/// Search results filtered by the shared search text.
private struct DemoSearchList: View {
    @Binding var searchText: String
    let onClose: () -> Void

    private var results: [String] {
        let all = (1...20).map { "Result \($0)" }
        return searchText.isEmpty ? all : all.filter { $0.localizedStandardContains(searchText) }
    }

    var body: some View {
        NavigationStack {
            List(results, id: \.self) { result in
                Text(result)
                    // Translucent rows like the tabs' cards, so the backdrop shows under the glass bar.
                    .listRowBackground(Rectangle().fill(.thinMaterial))
            }
            .overlay {
                if results.isEmpty {
                    ContentUnavailableView.search(text: searchText)
                }
            }
            // The same backdrop as the tabs, so the glass bar stays liquid while searching
            // instead of turning flat over the list's plain background.
            .scrollContentBackground(.hidden)
            .glassBackdrop()
            .navigationTitle("Search")
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Close", systemImage: "xmark", action: onClose)
                }
            }
        }
    }
}

#Preview("Light") {
    NavigationStack { GlassTabBarScreen() }
        .preferredColorScheme(.light)
}

#Preview("Dark") {
    NavigationStack { GlassTabBarScreen() }
        .preferredColorScheme(.dark)
}

#Preview("Search Separated – Light") {
    TabBarDemo(variant: .searchSeparated, minimizesOnScroll: true)
        .preferredColorScheme(.light)
}

#Preview("Search Separated – Dark") {
    TabBarDemo(variant: .searchSeparated, minimizesOnScroll: true)
        .preferredColorScheme(.dark)
}
