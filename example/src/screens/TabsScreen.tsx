import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { useNavigation, useRoute } from '@react-navigation/native';
import { StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { GlassView } from '@mohamed_abd_elnaby/react-native-liquid-glass';
import { Icon } from '../controls';
import { GlassScreen, LargeTitle } from '../GlassScreen';
import { useTheme, withAlpha } from '../theme';

// LiquidBottomTabBar: 62 dp tall, 20 dp from the screen edges, 12 dp above the system bar.
const BAR_HEIGHT = 62;
const BAR_GAP = 12;

const Tab = createBottomTabNavigator();

/** A bottom-tabs navigator over the backdrop, with a GlassView behind its floating tab bar. */
export function TabsScreen() {
  const { c } = useTheme();
  const insets = useSafeAreaInsets();
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        // The bar floats, so it takes no safe-area padding of its own.
        safeAreaInsets: { bottom: 0 },
        tabBarActiveTintColor: c.primary,
        tabBarInactiveTintColor: c.onSurface,
        tabBarLabelStyle: styles.label,
        tabBarStyle: [styles.bar, { bottom: insets.bottom + BAR_GAP }],
        tabBarBackground: TabBarGlass,
        tabBarIcon: route.name === 'Home' ? HomeIcon : ProfileIcon,
      })}
    >
      <Tab.Screen name="Home" component={TabList} />
      <Tab.Screen name="Profile" component={TabList} />
    </Tab.Navigator>
  );
}

const TabBarGlass = () => <GlassView style={StyleSheet.absoluteFill} />;
const HomeIcon = ({ color }: { color: string }) => (
  <Icon name="house" size={24} color={color} />
);
const ProfileIcon = ({ color }: { color: string }) => (
  <Icon name="person" size={24} color={color} />
);

function TabList() {
  const { c, t } = useTheme();
  const navigation = useNavigation();
  const title = useRoute().name;
  return (
    <GlassScreen
      title=""
      closeButton
      // Close leaves the whole demo, not just this tab.
      onBack={() => navigation.getParent()?.goBack()}
      spacing={12}
      // The scaffold adds the bottom safe area.
      bottomInset={BAR_HEIGHT + BAR_GAP}
    >
      <LargeTitle>{title}</LargeTitle>
      {Array.from({ length: 30 }, (_, i) => (
        <View
          key={i}
          style={[styles.card, { backgroundColor: withAlpha(c.surface, 0.6) }]}
        >
          <Text style={t.body}>
            {title} item {i + 1}
          </Text>
        </View>
      ))}
    </GlassScreen>
  );
}

const styles = StyleSheet.create({
  bar: {
    position: 'absolute',
    marginHorizontal: 20,
    height: BAR_HEIGHT,
    paddingBottom: 0,
    borderTopWidth: 0,
    backgroundColor: 'transparent',
    elevation: 0,
  },
  label: { fontSize: 10 },
  card: {
    minHeight: 60,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
