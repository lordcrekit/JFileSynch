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
  public void testXD() throws IOException {
    final Path p = Paths.get("lol.json");
    try (final UploaderCache cache = new UploaderCache(CONTEXT, p)) {
      cache.freeze(Pattern.compile(".*"), 500);
    }
    try (final Reader r = Files.newBufferedReader(p)) {
      JSONObject o = new JSONObject(new JSONTokener(r));
      System.out.println(o.toString());
    }
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
        Assert.assertEquals(1, info.FrozenPatterns.size());
        Assert.assertEquals(".*", info.FrozenPatterns.keySet().iterator().next().pattern());
      }

      // Make sure it works after reloading the cache.
      try (final UploaderCache cache = new UploaderCache(CONTEXT, tempfile)) {
        final UploaderCacheInformation info = cache.getCacheInformation();

        Assert.assertEquals(1, info.FrozenPatterns.size());
        Assert.assertEquals(".*", info.FrozenPatterns.keySet().iterator().next().pattern());
      }
    } finally {
      Files.delete(tempfile);
    }
  }

  @Test
  public void testTimestampWhenFrozenIO() {
    Assert.fail("todo");
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
