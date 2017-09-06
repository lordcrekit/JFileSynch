package com.github.lordcrekit;

import org.junit.Test;
import org.zeromq.ZContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CleanupIntegrityTest {
  @Test
  public void testCacheCleanup() throws IOException {
    System.out.println("Test Cache cleanup");

    final Path tempfile = Files.createTempFile(CleanupIntegrityTest.class.getName(), "Cache");
    final ZContext context = new ZContext();
    try (final UploaderCache cache = new UploaderCache(context, tempfile)) {
    } finally {
      context.destroy();
      Files.delete(tempfile);
    }
  }

  @Test
  public void testUploaderCleanup() throws IOException {
    System.out.println("Test Uploader cleanup");

    final ZContext context = new ZContext();
    try (final UploaderService service = new UploaderService(context, null, null, null)) {
    } finally {
      context.destroy();
    }
  }
}
