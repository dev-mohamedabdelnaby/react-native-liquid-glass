// react-native-vector-icons 10 ships no types: the two methods vectorIcon reads.
declare module 'react-native-vector-icons/Ionicons' {
  const Ionicons: {
    getRawGlyphMap(): Record<string, number>;
    getFontFamily(): string;
  };
  export default Ionicons;
}
