package com.github.lordcrekit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zeromq.ZContext;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * @author William A. Norman <a href="norman.william.dev@gmail.com/>
 */
public class UploadServiceTest {

  private static ZContext CONTEXT;
  private static Path TEST_DIRECTORY;

  @BeforeClass
  public static void setUp() throws IOException {
    System.out.println(UploadServiceTest.class.getName());

    CONTEXT = new ZContext();
    TEST_DIRECTORY = Files.createTempDirectory(
        UploadServiceTest.class.getName());
  }

  @AfterClass
  public static void tearDown() throws IOException {
    CONTEXT.destroy();
    Files.delete(TEST_DIRECTORY);
  }

  class TestingRouter implements UploaderRouter {
    @Override
    public List<URI> route(Path path) {
      return Arrays.asList(path.toUri());
    }
  }

  class TestingStrategy implements UploaderStrategy {
    /**
     * The number of times this has run.
     */
    int Count;

    @Override
    public long upload(Path file, URI destination) throws IOException {
      // <editor-fold defaultstate="collapsed" desc="Simulate long upload">
      try {
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
      } catch (InterruptedException e) {
      }
      // </editor-fold>
      Count++;
      return Files.readAttributes(file, BasicFileAttributes.class).lastModifiedTime().toMillis();
    }
  }

  @Test
  public void testQueueUpload() {
  }

  /**
   * If the UploaderService is told to upload a file 5 times, but by the time it's finished the first uploaded the file
   * is on it's fifth iteration, it should only upload one more time and skip the rest.
   *
   * @throws IOException
   *     If the test fails to create necessary testing files.
   */
  @Test
  public void testManyChangesPolicy()
      throws IOException {

    System.out.println("\tTest changing faster than uploading");

    final Path cacheFile = Files.createTempFile(TEST_DIRECTORY, UploadServiceTest.class.getName(), "");
    try {
      final AtomicReference<Long> fileDate = new AtomicReference<Long>(new Long(1));
      final UploaderCache cache = new UploaderCache(CONTEXT, cacheFile);
      final TestingStrategy strategy = new TestingStrategy(fileDate);
      final UploaderService service = new UploaderService(cache, new TestingRouter(), strategy);

      final Path uploadPath = Paths.get(TEST_DIRECTORY.toString(), "test.txt");
      for (int i = 2; i < 10; i++) {
        service.queueUpload(uploadPath);
        fileDate.set(new Long(i));
        Thread.sleep(TimeUnit.SECONDS.toMillis(1) / 20);
      }

      service.terminate();
      service.awaitTermination();
      Assert.assertEquals(2, strategy.Count); // It should have only needed to upload twice. Or once maybe.

    } catch (InterruptedException e) {
      Assert.fail("Error in testing code. Should not have been interrupted.");
    } finally {
      Files.delete(cacheFile);
    }
  }

  @Test
  public void testIgnoredFiles() throws IOException {
    System.out.println("\tTest ignored files");

    final Path cacheFile = Files.createTempFile(TEST_DIRECTORY, "", "");
    final Path uploadFile = Files.createTempFile(TEST_DIRECTORY, ".ignore", "");
    final TestingStrategy strategy = new TestingStrategy();
    try (final UploaderCache cache = new UploaderCache(CONTEXT, cacheFile);
         final UploaderService service = new UploaderService(cache, new TestingRouter(), strategy)) {

      cache.ignore(Pattern.compile(".*\\.ignore.*"));

      service.queueUpload(uploadFile);
      service.terminate();
      service.awaitTermination();

      Assert.assertEquals(0, strategy.Count);
    } finally {
      Files.delete(cacheFile);
      Files.delete(uploadFile);
    }
  }

  @Test
  public void testFrozenFiles() throws IOException {
    System.out.println("\tTest frozen files");

    final Path cacheFile = Files.createTempFile(TEST_DIRECTORY, UploadServiceTest.class.getName(), "");
    final Path toUpload = Files.createTempFile(TEST_DIRECTORY, ".freeze", "");

    final TestingStrategy strategy = new TestingStrategy();
    try (final UploaderCache cache = new UploaderCache(CONTEXT, cacheFile);
         final UploaderService service = new UploaderService(cache, new TestingRouter(), strategy)) {

      cache.ignore(Pattern.compile(".*\\.freeze.*"));
      service.queueUpload(toUpload);
      service.terminate();
      service.awaitTermination();

      Assert.assertEquals(0, strategy.Count);
    } finally {
      Files.delete(cacheFile);
    }
  }
}
