import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import {
  createNativeStackNavigator,
  type NativeStackScreenProps,
} from '@react-navigation/native-stack';
import { useState } from 'react';
import {
  GlassButton,
  GlassView,
} from '@mohamed_abd_elnaby/react-native-liquid-glass';

type Routes = { Home: undefined; GlassChecks: undefined };

const Stack = createNativeStackNavigator<Routes>();

function Home({ navigation }: NativeStackScreenProps<Routes, 'Home'>) {
  return (
    <View style={styles.center}>
      <Pressable
        testID="open"
        style={styles.button}
        onPress={() => navigation.navigate('GlassChecks')}
      >
        <Text style={styles.buttonText}>Open glass checks</Text>
      </Pressable>
    </View>
  );
}

const hue = (i: number) => `hsl(${(i * 47) % 360}, 80%, 55%)`;

// Temporary screen for the Task 5 device checks (Task 8 rebuilds the example).
function GlassChecks() {
  const [presses, setPresses] = useState(0);
  const [done, setDone] = useState(0);
  return (
    <View style={styles.flex}>
      <View style={styles.scrollArea}>
        <ScrollView testID="rows">
          {Array.from({ length: 40 }, (_, i) => (
            <View key={i} style={[styles.row, { backgroundColor: hue(i) }]}>
              <Text style={styles.rowText}>Row {i}</Text>
            </View>
          ))}
        </ScrollView>
        <GlassView
          style={styles.overRows}
          glass={{ liquid: true, blur: 2 }}
          shape={{ cornerRadius: 24 }}
        >
          <Text style={styles.glassText}>Scrolling check</Text>
        </GlassView>
      </View>
      <View style={styles.stripes}>
        <GlassView style={styles.pressGlass}>
          <Pressable testID="press" onPress={() => setPresses((n) => n + 1)}>
            <Text style={styles.glassText}>Press me</Text>
          </Pressable>
        </GlassView>
        <Text style={styles.label}>Presses: {presses}</Text>
        <GlassView style={styles.outer} shape={{ cornerRadius: 28 }}>
          <GlassView style={styles.inner} glass={{ tint: 'rgba(255,0,0,0.3)' }}>
            <Text style={styles.glassText}>Nested</Text>
          </GlassView>
        </GlassView>
        <GlassView testID="zero" style={styles.zero} />
        <Text style={styles.label}>Zero width glass above</Text>
        <View style={styles.shapes}>
          <GlassView style={styles.shape} shape="circle">
            <Text style={styles.glassText}>O</Text>
          </GlassView>
          <GlassView style={styles.shape} shape={{ cornerRadius: 8 }}>
            <Text style={styles.glassText}>8</Text>
          </GlassView>
          <GlassView
            style={styles.shape}
            glass={{ opaque: true }}
            shape={{ cornerRadius: 8 }}
          >
            <Text style={styles.glassText}>Op</Text>
          </GlassView>
        </View>
        <View style={styles.shapes}>
          <GlassButton
            testID="done"
            title="Done"
            icon="checkmark"
            style={styles.glassButton}
            onPress={() => setDone((n) => n + 1)}
          />
          <GlassButton
            title="Go"
            prominent
            tint="#0a84ff"
            style={styles.glassButton}
          />
          <GlassButton title="Off" disabled style={styles.glassButton} />
        </View>
        <Text style={styles.label}>Done: {done}</Text>
      </View>
    </View>
  );
}

export default function App() {
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen name="Home" component={Home} />
        <Stack.Screen name="GlassChecks" component={GlassChecks} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  button: { padding: 16, backgroundColor: '#0a84ff', borderRadius: 12 },
  buttonText: { color: 'white', fontSize: 16 },
  scrollArea: { height: 300 },
  row: { height: 60, justifyContent: 'center', paddingLeft: 16 },
  rowText: { color: 'white', fontWeight: '700' },
  overRows: {
    position: 'absolute',
    top: 120,
    left: 40,
    right: 40,
    height: 70,
    alignItems: 'center',
    justifyContent: 'center',
  },
  stripes: { flex: 1, padding: 16, backgroundColor: '#ffb300', gap: 8 },
  pressGlass: {
    height: 56,
    width: 200,
    alignItems: 'center',
    justifyContent: 'center',
  },
  outer: { height: 110, width: 280, padding: 16 },
  inner: {
    height: 60,
    width: 160,
    alignItems: 'center',
    justifyContent: 'center',
  },
  zero: { height: 40, width: 0 },
  shapes: { flexDirection: 'row', gap: 12 },
  shape: {
    height: 60,
    width: 60,
    alignItems: 'center',
    justifyContent: 'center',
  },
  glassButton: { width: 100 },
  label: { color: '#222', fontWeight: '600' },
  glassText: { color: '#111', fontWeight: '700' },
});
