import { createContext, useContext, useMemo, type ReactNode } from 'react';
import type { GlassSettings } from './types';

export const DEFAULT_SETTINGS: Required<GlassSettings> = {
  appearance: 'system',
  tint: null,
  layoutDirection: 'system',
  androidStyle: 'glass',
  androidControlShape: 'pill',
  androidGlowIntensity: 0.35,
  clear: true,
  blur: 3,
  white: 0.25,
  black: 0.05,
  edgeLight: 0.15,
  refraction: { height: 14, amount: 24 },
  opaque: false,
  liquid: true,
  dropColor: null,
  sliderLiquid: true,
  switchLiquid: true,
  segmentedLiquid: true,
  tabBarLiquid: true,
  tabBarDrop: true,
  fontFamily: '',
};

const GlassSettingsContext = createContext(DEFAULT_SETTINGS);

export const useGlassSettings = () => useContext(GlassSettingsContext);

export function GlassSettingsProvider({
  value,
  children,
}: {
  value: Partial<GlassSettings>;
  children: ReactNode;
}) {
  const parent = useGlassSettings();
  const merged = useMemo(
    () => ({
      ...parent,
      ...Object.fromEntries(
        Object.entries(value).filter(([, v]) => v !== undefined)
      ),
    }),
    [parent, value]
  );
  return (
    <GlassSettingsContext.Provider value={merged}>
      {children}
    </GlassSettingsContext.Provider>
  );
}
