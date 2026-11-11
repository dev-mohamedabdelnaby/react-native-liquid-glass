import { StyleSheet, Text, useColorScheme, View } from 'react-native';

export default function App() {
  // Text defaults to black on both platforms; the window background follows the system appearance.
  const dark = useColorScheme() === 'dark';
  return (
    <View style={styles.container}>
      <Text style={dark ? styles.dark : styles.light}>Liquid Glass</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  light: { color: 'black' },
  dark: { color: 'white' },
});
