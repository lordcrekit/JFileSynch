package com.github.lordcrekit;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.regex.Pattern;

public class UploaderCacheInformationTest {

  @Test
  public void testAddFrozenTimestamps() throws IOException {
    System.out.println("Test addFrozenTimestamps()");

    final Path root = Files.createTempDirectory(UploaderCacheInformation.class.getName());
    final Path testFile1 = Files.createTempFile(root, "test", ".freeze");
    final Path testFile2 = Files.createTempFile(root, "test", ".freeze");
    final Path testFile3 = Files.createTempFile(root, "test", "don't");
    try {
      Files.setLastModifiedTime(testFile1, FileTime.fromMillis(50));
      Files.setLastModifiedTime(testFile2, FileTime.fromMillis(60));
      Files.setLastModifiedTime(testFile3, FileTime.fromMillis(70));

      final UploaderCacheInformation info = new UploaderCacheInformation();
      info.addFrozenTimestamps(root, Pattern.compile(".*\\.freeze"));

      Assert.assertTrue(info.TimestampsWhenFrozen.containsKey(testFile1));
      Assert.assertEquals(50, (long) info.TimestampsWhenFrozen.get(testFile1));

      Assert.assertTrue(info.TimestampsWhenFrozen.containsKey(testFile2));
      Assert.assertEquals(60, (long) info.TimestampsWhenFrozen.get(testFile2));

      Assert.assertFalse(info.TimestampsWhenFrozen.containsKey(testFile3));

    } finally {
      Files.delete(testFile1);
      Files.delete(testFile2);
      Files.delete(testFile3);
      Files.delete(root);
    }
  }

  @Test
  public void testIsIgnored() {
    UploaderCacheInformation info = new UploaderCacheInformation();
  }

  @Test
  public void testIsFrozen() {
    UploaderCacheInformation info = new UploaderCacheInformation();
    info.FrozenPatterns.put(Pattern.compile(".*\\.freeze50"), (long) 50);
    info.FrozenPatterns.put(Pattern.compile(".*\\.freeze60"), (long) 60);

    Assert.assertEquals((long) -1, info.isFrozen(Paths.get("lol/tmp/notfrozen")));
    Assert.assertEquals((long) 50, info.isFrozen(Paths.get("lol/tmp/.freeze50")));
    Assert.assertEquals((long) 60, info.isFrozen(Paths.get("lol/tmp/.freeze60")));
  }

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
