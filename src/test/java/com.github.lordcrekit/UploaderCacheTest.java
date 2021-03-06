package com.github.lordcrekit;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zeromq.ZContext;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.regex.Pattern;

public class UploaderCacheTest {

  static ZContext CONTEXT;

  @BeforeClass
  public static void setUp() {
    CONTEXT = new ZContext();
  }

  @AfterClass
  public static void tearDown() {
    CONTEXT.destroy();
  }

  @Test
  public void testFreezeIO() throws IOException {
    System.out.println("Test freeze(PATH)");

    final Path root = Files.createTempDirectory(UploaderCacheInformationTest.class.getName());
    final Path tempFile = Files.createTempFile(root, "", "");
    try {
      try (final UploaderCache cache = new UploaderCache(CONTEXT, tempFile)) {
        cache.freeze(root, Pattern.compile(".*"), 50);

        // Make sure it was entered correctly.
        final UploaderCacheInformation info = cache.getCacheInformation();
        Assert.assertEquals(1, info.FrozenPatterns.size());
        Assert.assertEquals(".*", info.FrozenPatterns.keySet().iterator().next().pattern());
      }

      // Make sure it works after reloading the cache.
      try (final UploaderCache cache = new UploaderCache(CONTEXT, tempFile)) {
        final UploaderCacheInformation info = cache.getCacheInformation();

        Assert.assertEquals(1, info.FrozenPatterns.size());
        Assert.assertEquals(".*", info.FrozenPatterns.keySet().iterator().next().pattern());
      }
    } finally {
      Files.delete(tempFile);
      Files.delete(root);
    }
  }

  @Test
  public void testTimestampWhenFrozenIO() throws IOException {
    final Path tempUploadFile = Files.createTempFile("", "");
    final Path root = Files.createTempDirectory(UploaderCacheInformationTest.class.getName());
    final Path tempFile = Files.createTempFile(root,"", ".freeze");
    Files.setLastModifiedTime(tempFile, FileTime.fromMillis(50));
    try {
      try (final UploaderCache cache = new UploaderCache(CONTEXT, tempFile)) {
        cache.freeze(root, Pattern.compile(".*\\.freeze"), 50);

        // Make sure it was entered correctly.
        final UploaderCacheInformation info = cache.getCacheInformation();
        Assert.assertEquals(1, info.TimestampsWhenFrozen.size());
        Assert.assertTrue(info.TimestampsWhenFrozen.containsKey(tempFile));
        Assert.assertEquals(50, (long) info.TimestampsWhenFrozen.get(tempFile));
      }

      // Make sure it works after reloading the cache.
      try (final UploaderCache cache = new UploaderCache(CONTEXT, tempFile)) {
        final UploaderCacheInformation info = cache.getCacheInformation();

        Assert.assertEquals(1, info.TimestampsWhenFrozen.size());
        Assert.assertTrue(info.TimestampsWhenFrozen.containsKey(tempFile));
        Assert.assertEquals(50, (long) info.TimestampsWhenFrozen.get(tempFile));
      }
    } finally {
      Files.delete(tempUploadFile);
      Files.delete(tempFile);
      Files.delete(root);
    }
  }

  @Test
  public void testIgnoreIO() throws IOException {
    final Path tempfile = Files.createTempFile("", "");
    try {
      try (final UploaderCache cache = new UploaderCache(CONTEXT, tempfile)) {
        cache.ignore(Pattern.compile(".*"));

        // Make sure it was entered correctly.
        final UploaderCacheInformation info = cache.getCacheInformation();
        Assert.assertEquals(1, info.IgnoredPatterns.size());
        Assert.assertEquals(".*", info.IgnoredPatterns.iterator().next().pattern());
      }

      // Make sure it works after reloading the cache.
      try (final UploaderCache cache = new UploaderCache(CONTEXT, tempfile)) {
        final UploaderCacheInformation info = cache.getCacheInformation();

        Assert.assertEquals(1, info.IgnoredPatterns.size());
        Assert.assertEquals(".*", info.IgnoredPatterns.iterator().next().pattern());
      }
    } finally {
      Files.delete(tempfile);
    }
  }

  @Test
  public void testTimestampIO() throws IOException {
    final Path tempUploadFile = Files.createTempFile("", "");
    final Path tempfile = Files.createTempFile("", "");
    try {
      try (final UploaderCache cache = new UploaderCache(CONTEXT, tempfile)) {
        cache.update(tempUploadFile, 50);

        // Make sure it was entered correctly.
        final UploaderCacheInformation info = cache.getCacheInformation();
        Assert.assertEquals(1, info.Timestamps.size());
        Assert.assertTrue(info.Timestamps.containsKey(tempUploadFile));
      }

      // Make sure it works after reloading the cache.
      try (final UploaderCache cache = new UploaderCache(CONTEXT, tempfile)) {
        final UploaderCacheInformation info = cache.getCacheInformation();

        Assert.assertEquals(1, info.Timestamps.size());
        Assert.assertTrue(info.Timestamps.containsKey(tempUploadFile));
      }
    } finally {
      Files.delete(tempUploadFile);
      Files.delete(tempfile);
    }
  }
}
