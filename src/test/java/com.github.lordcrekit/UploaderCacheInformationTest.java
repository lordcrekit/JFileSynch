package com.github.lordcrekit;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class UploaderCacheInformationTest {

  @Test
  public void testEquals() {
    final UploaderCacheInformation o1 = new UploaderCacheInformation();
    final UploaderCacheInformation o2 = new UploaderCacheInformation();

    // Empty check
    Assert.assertEquals(o1, o2);

    // <editor-fold defaultstate="collapsed" desc="Ignored patterns">
    o1.IgnoredPatterns.add(Pattern.compile(".*"));
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o2.IgnoredPatterns.add(Pattern.compile(".*"));
    Assert.assertEquals(o1, o2);

    o2.IgnoredPatterns.add(Pattern.compile("[a-d].*"));
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o1.IgnoredPatterns.add(Pattern.compile("[a-d].*"));
    Assert.assertEquals(o1, o2);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Frozen patterns">
    Assert.fail("todo");
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Frozen timestamps">
    Assert.fail("todo");
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Timestamps">
    Assert.fail("todo");
    // </editor-fold>
  }

  @Test
  public void testIO() {
    final UploaderCacheInformation o;
  }
}
