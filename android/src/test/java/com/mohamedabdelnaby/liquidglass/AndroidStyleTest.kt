package com.mohamedabdelnaby.liquidglass

import com.mohamedabdelnaby.liquidglass.catalog.glass.AndroidStyle
import org.junit.Assert.assertEquals
import org.junit.Test

/** The JS `androidStyle` prop as the hosted screen's style. */
class AndroidStyleTest {
  @Test
  fun materialIsMaterial() {
    assertEquals(AndroidStyle.Material, androidStyleOf("material"))
  }

  @Test
  fun glassAndUnknownAreGlass() {
    assertEquals(AndroidStyle.Glass, androidStyleOf("glass"))
    assertEquals(AndroidStyle.Glass, androidStyleOf("bogus"))
  }
}
