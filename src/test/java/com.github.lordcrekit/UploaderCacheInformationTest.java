package com.github.lordcrekit;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class UploaderCacheInformationTest {

  @Test
  public void testEquals() {
    System.out.println("Test equals(OBJECT)");
    final UploaderCacheInformation o1 = new UploaderCacheInformation();
    final UploaderCacheInformation o2 = new UploaderCacheInformation();

    // Empty check
    Assert.assertEquals(o1, o2);

    // <editor-fold defaultstate="collapsed" desc="Ignored patterns">
    o1.IgnoredPatterns.add(Pattern.compile(".*"));
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o2.IgnoredPatterns.add(Pattern.compile("[a-d].*"));
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o2.IgnoredPatterns.clear();
    o2.IgnoredPatterns.add(Pattern.compile(".*"));
    Assert.assertEquals(o1, o2);

    o2.IgnoredPatterns.add(Pattern.compile("[a-d].*"));
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o1.IgnoredPatterns.add(Pattern.compile("[a-d].*"));
    Assert.assertEquals(o1, o2);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Frozen patterns">
    o1.FrozenPatterns.put(Pattern.compile(".*"), (long) 50);
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o2.FrozenPatterns.put(Pattern.compile("[a-d].*"), (long) 50);
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o2.FrozenPatterns.clear();
    o2.FrozenPatterns.put(Pattern.compile(".*"), (long) 50);
    Assert.assertEquals(o1, o2);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Frozen timestamps">
    o1.TimestampsWhenFrozen.put(Paths.get("xD"), (long) 60);
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o2.TimestampsWhenFrozen.put(Paths.get("xD"), (long) 70);
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o2.TimestampsWhenFrozen.clear();
    o2.TimestampsWhenFrozen.put(Paths.get("zXD"), (long) 60);
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o2.TimestampsWhenFrozen.clear();
    o2.TimestampsWhenFrozen.put(Paths.get("xD"), (long) 60);
    Assert.assertEquals(o1, o2);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Timestamps">
    o1.Timestamps.put(Paths.get("xD"), (long) 60);
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o2.Timestamps.put(Paths.get("xD"), (long) 70);
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o2.Timestamps.clear();
    o2.Timestamps.put(Paths.get("zXD"), (long) 60);
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o2.Timestamps.clear();
    o2.Timestamps.put(Paths.get("xD"), (long) 60);
    Assert.assertEquals(o1, o2);
    // </editor-fold>
  }

  @Test
  public void testIO() throws IOException {
    final UploaderCacheInformation o1 = new UploaderCacheInformation();
    UploaderCacheInformation o2;

    // Empty
    o2 = new UploaderCacheInformation(o1.toJSON());
    Assert.assertEquals(o1, o2);

    // <editor-fold defaultstate="collapsed" desc="Ignored patterns">
    o1.IgnoredPatterns.add(Pattern.compile(".*"));
    o2 = new UploaderCacheInformation(o1.toJSON());
    Assert.assertEquals(o1, o2);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Frozen patterns">
    o1.FrozenPatterns.put(Pattern.compile("[a-z]*"), (long) 60);
    o2 = new UploaderCacheInformation(o1.toJSON());
    Assert.assertEquals(o1, o2);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Frozen timestamps">
    o1.TimestampsWhenFrozen.put(Paths.get("xD"), (long) 60);
    o2 = new UploaderCacheInformation(o1.toJSON());
    Assert.assertEquals(o1, o2);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Timestamps">
    o1.Timestamps.put(Paths.get("lol"), (long) 60);
    o2 = new UploaderCacheInformation(o1.toJSON());
    Assert.assertEquals(o1, o2);
    // </editor-fold>
  }
}
