import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { GlassView } from '@mohamed_abd_elnaby/react-native-liquid-glass';
import { Icon } from '../controls';
import { GlassScreen } from '../GlassScreen';
import { SystemColors, useTheme } from '../theme';

export function InteractiveGlassScreen() {
  const { t } = useTheme();
  const [taps, setTaps] = useState(0);
  const tap = () => setTaps((n) => n + 1);
  return (
    <GlassScreen title="Interactive" scroll={false} spacing={32}>
      <Text style={t.headline}>Press and hold the shapes</Text>
      <GlassView shape="circle" glass={{ interactive: true }}>
        <Pressable
          accessibilityRole="button"
          accessibilityLabel="Tap"
          onPress={tap}
          style={styles.circle}
        >
          <Icon name="hand_tap" size={44} />
        </Pressable>
      </GlassView>
      <GlassView glass={{ tint: SystemColors.Purple, interactive: true }}>
        <Pressable accessibilityRole="button" onPress={tap} style={styles.pad}>
          <Text style={[t.title3, styles.digits]}>Tapped {taps} times</Text>
        </Pressable>
      </GlassView>
      <View style={styles.row}>
        <GlassView style={styles.pad}>
          <Text style={t.body}>Static</Text>
        </GlassView>
        <GlassView glass={{ interactive: true }} style={styles.pad}>
          <Text style={t.body}>Interactive</Text>
        </GlassView>
      </View>
    </GlassScreen>
  );
}

const styles = StyleSheet.create({
  circle: {
    width: 120,
    height: 120,
    alignItems: 'center',
    justifyContent: 'center',
  },
  pad: { padding: 16 },
  digits: { fontVariant: ['tabular-nums'] },
  row: { flexDirection: 'row', gap: 20 },
});
