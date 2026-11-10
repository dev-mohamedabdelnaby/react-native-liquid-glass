// SF Symbol name -> Material icon key understood by the Android glass engine.
export const ANDROID_ICONS: Record<string, string> = {
  'checkmark': 'check',
  'xmark': 'close',
  'plus': 'add',
  'minus': 'remove',
  'magnifyingglass': 'search',
  'heart': 'favorite_border',
  'square.and.arrow.up': 'share',
  'ellipsis': 'more_horiz',
  'trash': 'delete',
};

// An explicit, non-empty override wins; a symbol outside the map gives null.
export function androidIconName(
  systemImage: string | undefined,
  override?: string
): string | null {
  if (override) return override;
  if (systemImage === undefined || !Object.hasOwn(ANDROID_ICONS, systemImage)) {
    return null;
  }
  return ANDROID_ICONS[systemImage] ?? null;
}
