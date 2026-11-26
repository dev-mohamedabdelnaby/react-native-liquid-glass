// Plain React Native screens on the native stack, to compare the system back button
// (screens 1 and 2) with a custom one (screen 3) next to the library's own screens.
import { Pressable, StyleSheet, Text, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';

export type PushRoutes = {
  Push1: undefined;
  Push2: undefined;
  Push3: undefined;
};

type Props = NativeStackScreenProps<PushRoutes>;

function Page({
  text,
  next,
  onNext,
}: {
  text: string;
  next?: string;
  onNext?: () => void;
}) {
  return (
    <View style={styles.page}>
      <Text style={styles.text}>{text}</Text>
      {next && onNext ? (
        <Pressable
          accessibilityRole="button"
          onPress={onNext}
          style={styles.button}
        >
          <Text style={styles.buttonText}>{next}</Text>
        </Pressable>
      ) : null}
    </View>
  );
}

export function Push1({ navigation }: Props) {
  return (
    <Page
      text="Screen 1: the system back button."
      next="Next"
      onNext={() => navigation.push('Push2')}
    />
  );
}

export function Push2({ navigation }: Props) {
  return (
    <Page
      text="Screen 2: the system back button again."
      next="Custom back"
      onNext={() => navigation.push('Push3')}
    />
  );
}

export function Push3() {
  return <Page text="Screen 3: a custom back button." />;
}

/** Screen 3's leading button: a text button that closes the whole demo. */
export function CloseButton({ onPress }: { onPress: () => void }) {
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel="Close"
      onPress={onPress}
      hitSlop={8}
      style={styles.close}
    >
      <Text style={styles.closeText}>✕ Close</Text>
    </Pressable>
  );
}

/** Floating button over Home that opens the demo. */
export function PushTestButton({ onPress }: { onPress: () => void }) {
  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      style={[styles.button, styles.floating]}
    >
      <Text style={styles.buttonText}>Push test</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  page: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 24,
    padding: 24,
    backgroundColor: '#f2f2f7',
  },
  text: { fontSize: 17, textAlign: 'center', color: '#1c1c1e' },
  button: {
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 24,
    backgroundColor: '#007aff',
  },
  buttonText: { color: 'white', fontSize: 17, fontWeight: '600' },
  floating: { position: 'absolute', alignSelf: 'center', bottom: 48 },
  close: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
    backgroundColor: '#ff3b30',
  },
  closeText: { color: 'white', fontSize: 15, fontWeight: '600' },
});
