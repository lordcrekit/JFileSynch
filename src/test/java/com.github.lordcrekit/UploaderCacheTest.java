package com.github.lordcrekit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zeromq.ZContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    final Path tempfile = Files.createTempFile("", "");
    try {
      try (final UploaderCache cache = new UploaderCache(CONTEXT, tempfile)) {
        cache.freeze(Pattern.compile(".*"), 50);

        // Make sure it was entered correctly.
        final UploaderCacheInformation info = cache.getCacheInformation();
        Assert.assertTrue(info.FrozenPatterns.containsKey(".*"));
        Assert.assertEquals(1, info.FrozenPatterns.size());
      }

      // Make sure it works after reloading the cache.
      try (final UploaderCache cache = new UploaderCache(CONTEXT, tempfile)) {
        final UploaderCacheInformation info = cache.getCacheInformation();

        Assert.assertTrue(info.FrozenPatterns.containsKey(".*"));
        Assert.assertEquals(1, info.FrozenPatterns.size());
      }
    } finally {
      Files.delete(tempfile);
    }
  }

  @Test
  public void testTimestampWhenFrozenIO() {

  }

  @Test
  public void testIgnoreIO() throws IOException {
    final Path tempfile = Files.createTempFile("", "");
    try {
      try (final UploaderCache cache = new UploaderCache(CONTEXT, tempfile)) {
        cache.ignore(Pattern.compile(".*"));

        // Make sure it was entered correctly.
        final UploaderCacheInformation info = cache.getCacheInformation();
        Assert.assertTrue(info.IgnoredPatterns.contains(".*"));
        Assert.assertEquals(1, info.IgnoredPatterns.size());
      }

      // Make sure it works after reloading the cache.
      try (final UploaderCache cache = new UploaderCache(CONTEXT, tempfile)) {
        final UploaderCacheInformation info = cache.getCacheInformation();

        Assert.assertTrue(info.IgnoredPatterns.contains(".*"));
        Assert.assertEquals(1, info.IgnoredPatterns.size());
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
        Assert.assertTrue(info.Timestamps.containsKey(tempUploadFile.toString()));
        Assert.assertEquals(1, info.Timestamps.size());
      }

      // Make sure it works after reloading the cache.
      try (final UploaderCache cache = new UploaderCache(CONTEXT, tempfile)) {
        final UploaderCacheInformation info = cache.getCacheInformation();

        Assert.assertTrue(info.Timestamps.containsKey(tempUploadFile.toString()));
        Assert.assertEquals(1, info.Timestamps.size());
      }
    } finally {
      Files.delete(tempUploadFile);
      Files.delete(tempfile);
    }
  }
}
