//
//  ContentView.swift
//  liquidGlass
//
//  Created by Mohamed AbdElNaby on 03/11/2026.
//

import SwiftUI

/// Each Liquid Glass component showcased in the catalog, with its own screen.
enum GlassComponent: String, CaseIterable, Identifiable {
    case glassEffect
    case glassView
    case interactive
    case buttons
    case container
    case morphing
    case union
    case toolbar
    case floatingHeader
    case tabBar
    case sheets
    case controls
    case text
    case transitions
    case search
    case editSearch
    case scrollEdge
    case splitView
    case zoomTransitions
    case alerts
    case recipes

    var id: Self { self }

    var title: String {
        switch self {
        case .glassEffect: "Glass Effect"
        case .glassView: "Glass View"
        case .interactive: "Interactive Glass"
        case .buttons: "Buttons"
        case .container: "Glass Container"
        case .morphing: "Morphing"
        case .union: "Union"
        case .toolbar: "Toolbar"
        case .floatingHeader: "Floating Header"
        case .tabBar: "Tab Bar"
        case .sheets: "Sheets & Menus"
        case .controls: "Controls"
        case .text: "Text"
        case .transitions: "Glass Transitions"
        case .search: "Search"
        case .editSearch: "Edit & Search Buttons"
        case .scrollEdge: "Scroll Edge & Bars"
        case .splitView: "Sidebar & Inspector"
        case .zoomTransitions: "Zoom Transitions"
        case .alerts: "Alerts & Dialogs"
        case .recipes: "Recipes"
        }
    }

    var systemImage: String {
        switch self {
        case .glassEffect: "drop.fill"
        case .glassView: "rectangle.on.rectangle"
        case .interactive: "hand.tap.fill"
        case .buttons: "button.horizontal.fill"
        case .container: "square.on.square"
        case .morphing: "wand.and.stars"
        case .union: "circle.grid.2x1.fill"
        case .toolbar: "menubar.rectangle"
        case .floatingHeader: "rectangle.portrait.topthird.inset.filled"
        case .tabBar: "dock.rectangle"
        case .sheets: "rectangle.bottomhalf.inset.filled"
        case .controls: "slider.horizontal.3"
        case .text: "textformat"
        case .transitions: "sparkles"
        case .search: "magnifyingglass"
        case .editSearch: "pencil.and.list.clipboard"
        case .scrollEdge: "rectangle.topthird.inset.filled"
        case .splitView: "sidebar.leading"
        case .zoomTransitions: "arrow.up.left.and.arrow.down.right"
        case .alerts: "exclamationmark.bubble"
        case .recipes: "square.grid.2x2.fill"
        }
    }

    @ViewBuilder
    var screen: some View {
        switch self {
        case .glassEffect: GlassEffectScreen()
        case .glassView: GlassViewScreen()
        case .interactive: InteractiveGlassScreen()
        case .buttons: GlassButtonsScreen()
        case .container: GlassContainerScreen()
        case .morphing: GlassMorphingScreen()
        case .union: GlassUnionScreen()
        case .toolbar: GlassToolbarScreen()
        case .floatingHeader: GlassFloatingHeaderScreen()
        case .tabBar: GlassTabBarScreen()
        case .sheets: GlassSheetScreen()
        case .controls: GlassControlsScreen()
        case .text: GlassTextScreen()
        case .transitions: GlassTransitionScreen()
        case .search: GlassSearchScreen()
        case .editSearch: GlassEditSearchScreen()
        case .scrollEdge: GlassScrollEdgeScreen()
        case .splitView: GlassSplitViewScreen()
        case .zoomTransitions: GlassZoomTransitionScreen()
        case .alerts: GlassAlertsScreen()
        case .recipes: GlassRecipesScreen()
        }
    }
}

/// The user's preferred appearance for the whole app.
enum AppearanceMode: String, CaseIterable, Identifiable {
    case system, light, dark

    var id: Self { self }

    var title: String {
        switch self {
        case .system: "System"
        case .light: "Light"
        case .dark: "Dark"
        }
    }

    /// `nil` follows the system setting.
    var colorScheme: ColorScheme? {
        switch self {
        case .system: nil
        case .light: .light
        case .dark: .dark
        }
    }
}

/// The user's preferred layout direction, to preview the app in left-to-right or right-to-left.
enum LayoutDirectionMode: String, CaseIterable, Identifiable {
    case system, leftToRight, rightToLeft

    var id: Self { self }

    var title: String {
        switch self {
        case .system: "System"
        case .leftToRight: "LTR"
        case .rightToLeft: "RTL"
        }
    }

    /// `nil` follows the system language's direction.
    var layoutDirection: LayoutDirection? {
        switch self {
        case .system: nil
        case .leftToRight: .leftToRight
        case .rightToLeft: .rightToLeft
        }
    }
}

struct ContentView: View {
    // react-native-liquid-glass: the settings come from the host (its props), and a row sends
    // `onOpen` instead of pushing.
    @Binding var appearance: AppearanceMode
    @Binding var directionMode: LayoutDirectionMode
    @Binding var glassTint: GlassTintChoice
    /// The direction from the system language, used when the mode is `.system`.
    var systemLayoutDirection: LayoutDirection
    var onOpen: (String) -> Void

    private var layoutDirection: LayoutDirection { directionMode.layoutDirection ?? systemLayoutDirection }

    var body: some View {
        NavigationStack {
            List {
                Section {
                    Picker("Appearance", selection: $appearance) {
                        ForEach(AppearanceMode.allCases) { mode in
                            Text(mode.title).tag(mode)
                        }
                    }
                    .pickerStyle(.segmented)

                    Picker("Layout direction", selection: $directionMode) {
                        ForEach(LayoutDirectionMode.allCases) { mode in
                            Text(mode.title).tag(mode)
                        }
                    }
                    .pickerStyle(.segmented)
                } footer: {
                    Text("RTL mirrors the whole app, including sheets and full-screen demos.").glassFontOnly(.footnote)
                }

                Section {
                    Picker("Glass tint", selection: $glassTint) {
                        ForEach(GlassTintChoice.allCases) { choice in
                            Label {
                                Text(choice.title)
                            } icon: {
                                Image(systemName: choice.color == nil ? "circle.dashed" : "circle.fill")
                                    .foregroundStyle(choice.color ?? .secondary)
                            }
                            .tag(choice)
                        }
                    }
                } footer: {
                    Text("Tints every glass in the catalog that has no tint of its own.").glassFontOnly(.footnote)
                }

                Section {
                    ForEach(GlassComponent.allCases) { component in
                        // A NavigationLink's look: primary title, tinted icon, disclosure chevron.
                        Button {
                            onOpen(component.rawValue)
                        } label: {
                            HStack {
                                Label {
                                    Text(component.title).foregroundStyle(Color.primary)
                                } icon: {
                                    Image(systemName: component.systemImage)
                                }
                                Spacer()
                                Image(systemName: "chevron.forward")
                                    .font(.body.weight(.semibold))
                                    .imageScale(.small)
                                    .foregroundStyle(Color(uiColor: .tertiaryLabel))
                                    // Where the system indicator sits (measured: 5 px at 3x from the edge).
                                    .padding(.trailing, 5.0 / 3.0)
                                    .accessibilityHidden(true)
                            }
                        }
                    }
                } header: {
                    Text("Components").glassFontOnly(.headline)
                }
            }
            // A list switched from RTL to LTR keeps UIKit's RTL layout and draws mirrored, so each
            // direction gets its own list (it opens at the top).
            .id(layoutDirection)
            .navigationTitle("Liquid Glass")
            .navigationDestination(for: GlassComponent.self) { component in
                component.screen
            }
        }
        .environment(\.glassTint, glassTint.color)
        .preferredColorScheme(appearance.colorScheme)
        .environment(\.layoutDirection, layoutDirection)
    }
}

#Preview("Light") {
    ContentView()
        .preferredColorScheme(.light)
}

#Preview("Right to Left") {
    ContentView()
        .environment(\.layoutDirection, .rightToLeft)
}

#Preview("Dark") {
    ContentView()
        .preferredColorScheme(.dark)
}
