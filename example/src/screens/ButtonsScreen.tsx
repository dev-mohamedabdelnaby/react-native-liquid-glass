import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { GlassButton } from '@mohamed_abd_elnaby/react-native-liquid-glass';
import { FitButton } from '../controls';
import { GlassScreen } from '../GlassScreen';
import { SystemColors, useTheme } from '../theme';

// lastAction shows iOS's SF Symbol names, as on the original.
const TRANSPORT = [
  ['play.fill', 'Play'],
  ['pause.fill', 'Pause'],
  ['forward.fill', 'Forward'],
] as const;

export function ButtonsScreen() {
  const { t } = useTheme();
  const [lastAction, setLastAction] = useState('None');
  return (
    <GlassScreen title="Buttons" scroll={false} spacing={24}>
      <Text style={t.headline}>Last action: {lastAction}</Text>
      <FitButton title="Glass" onPress={() => setLastAction('Glass')} />
      <FitButton
        title="Glass Prominent"
        prominent
        onPress={() => setLastAction('Prominent')}
      />
      <FitButton
        title="Clear Glass"
        glass={{ style: 'clear' }}
        onPress={() => setLastAction('Clear')}
      />
      <FitButton
        title="Tinted Prominent"
        prominent
        tint={SystemColors.Orange}
        onPress={() => setLastAction('Tinted')}
      />
      <FitButton
        title="Large"
        icon="bolt.fill"
        size="large"
        onPress={() => setLastAction('Large')}
      />
      <View style={styles.row}>
        {TRANSPORT.map(([symbol, name]) => (
          <GlassButton
            key={symbol}
            icon={symbol}
            accessibilityLabel={name}
            style={styles.circle}
            onPress={() => setLastAction(symbol)}
          />
        ))}
      </View>
      <FitButton title="Disabled" disabled />
    </GlassScreen>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', gap: 16 },
  circle: { width: 46, height: 46 },
});
