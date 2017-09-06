package com.github.lordcrekit;

import org.junit.Assert;
import org.junit.Test;
import org.zeromq.ZContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class UploaderCacheTest {
  @Test
  public void testFreeze() throws IOException {
    System.out.println("Test freeze(PATH)");

    final Path tempfile = Files.createTempFile("", "");
    final ZContext CONTEXT = new ZContext();

    try {
      try (final UploaderCache cache = new UploaderCache(CONTEXT, tempfile)) {
        cache.freeze(Pattern.compile(".*"), 50);
      }
      try (final UploaderCache cache = new UploaderCache(CONTEXT, tempfile)) {
        Assert.fail("TODO");
      }
    } finally {
      CONTEXT.destroy();
      Files.delete(tempfile);
    }
  }
}
