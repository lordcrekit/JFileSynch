package com.github.lordcrekit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author William A. Norman <a href="norman.william.dev@gmail.com/>
 */
public class UploaderServiceTest {

  private static ZContext CONTEXT;
  private static Path TEST_DIRECTORY;

  @BeforeClass
  public static void setUp() throws IOException {
    System.out.println(UploaderServiceTest.class.getName());

    CONTEXT = new ZContext();
    TEST_DIRECTORY = Files.createTempDirectory(
        UploaderServiceTest.class.getName());
  }

  @AfterClass
  public static void tearDown() throws IOException {
    try {
      Files.delete(TEST_DIRECTORY);
    } catch (IOException e) {
      throw e;
    } finally {
      System.out.println("Destroying testing context");
      CONTEXT.destroy();
    }

    System.out.println("Testing complete");
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
        Thread.sleep(500);
      } catch (InterruptedException e) {
      }
      // </editor-fold>
      Count++;
      return Files.readAttributes(file, BasicFileAttributes.class).lastModifiedTime().toMillis();
    }
  }

  @Test
  public void testQueueUpload() throws IOException {
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

    final Path cacheFile = Files.createTempFile(TEST_DIRECTORY, "cache", ".json");
    final Path uploadPath = Files.createTempFile(TEST_DIRECTORY, "test", ".txt");
    final TestingStrategy strategy = new TestingStrategy();
    try (final UploaderCache cache = new UploaderCache(CONTEXT, cacheFile)) {
      try (final UploaderService service = new UploaderService(CONTEXT, cache, new TestingRouter(), strategy)) {

        Files.write(uploadPath, "data".getBytes());
        for (int i = 2; i < 20; i++) {
          Files.setLastModifiedTime(uploadPath, FileTime.fromMillis(i * 1000));
          service.queueUpload(uploadPath);
          Thread.sleep(TimeUnit.SECONDS.toMillis(1) / 20);
        }

        service.terminate();
        service.awaitTermination();

      } catch (InterruptedException e) {
        Assert.fail("Error in testing code. Should not have been interrupted.");
      }

      // Make sure it uploaded to the most recent date.
      Assert.assertEquals(
          Files.getLastModifiedTime(uploadPath).toMillis(),
          cache.getFileInformation(uploadPath).TimeUploaded);

      // Make sure it didn't upload too many times
      Assert.assertEquals(2, strategy.Count);


    } finally {
      Files.delete(cacheFile);
      Files.delete(uploadPath);
    }
  }

  @Test
  public void testIgnoredFiles() throws IOException, InterruptedException {
    System.out.println("\tTest ignored files");

    final Path cacheFile = Files.createTempFile(TEST_DIRECTORY, "", "");
    final Path uploadFile = Files.createTempFile(TEST_DIRECTORY, ".ignore", "");

    final TestingStrategy strategy = new TestingStrategy();
    try (final UploaderCache cache = new UploaderCache(CONTEXT, cacheFile);
         final UploaderService service = new UploaderService(CONTEXT, cache, new TestingRouter(), strategy)) {

      cache.ignore(Pattern.compile(".*\\.ignore.*"));

      Files.write(uploadFile, "data".getBytes());
      service.queueUpload(uploadFile);

      service.terminate();
      service.awaitTermination();

    } finally {
      Files.delete(cacheFile);
      Files.delete(uploadFile);
    }

    Assert.assertEquals(0, strategy.Count);
  }

  @Test
  public void testFrozenFiles() throws IOException, InterruptedException {
    System.out.println("\tTest frozen files");

    final Path cacheFile = Files.createTempFile(TEST_DIRECTORY, "", "");

    final Path root = Files.createTempDirectory(TEST_DIRECTORY, "testFrozenFiles");
    final Path toUpload = Files.createTempFile(root, ".freeze", "");

    final TestingStrategy strategy = new TestingStrategy();
    try (final UploaderCache cache = new UploaderCache(CONTEXT, cacheFile);
         final UploaderService service = new UploaderService(CONTEXT, cache, new TestingRouter(), strategy)) {

      Files.write(toUpload, "data".getBytes());
      Files.setLastModifiedTime(toUpload, FileTime.fromMillis(50));
      service.queueUpload(toUpload);

      try {
        Thread.sleep(1500);
      } catch (InterruptedException e) {
        Assert.fail(e.getMessage());
      }
      cache.freeze(root, Pattern.compile(".*\\.freeze.*"), 70);

      Files.setLastModifiedTime(toUpload, FileTime.fromMillis(80));
      service.queueUpload(toUpload);

      service.terminate();
      service.awaitTermination();

    } finally {
      Files.delete(cacheFile);
      Files.delete(toUpload);
      Files.delete(root);
    }

    // It should have ignored the file since it was frozen appropriately.
    Assert.assertEquals(1, strategy.Count);
  }

  /**
   * Frozen files should still be uploaded if they are not up to date.
   *
   * @throws IOException
   */
  @Test
  public void testFrozenBackup() throws IOException, InterruptedException {
    System.out.println("\tTest frozen files");

    final Path cacheFile = Files.createTempFile(TEST_DIRECTORY, "", "");

    final Path root = Files.createTempDirectory(TEST_DIRECTORY, "testFrozenFiles");
    final Path toUpload = Files.createTempFile(root, ".freeze", "");

    final TestingStrategy strategy = new TestingStrategy();
    try (final UploaderCache cache = new UploaderCache(CONTEXT, cacheFile);
         final UploaderService service = new UploaderService(CONTEXT, cache, new TestingRouter(), strategy)) {

      Files.setLastModifiedTime(toUpload, FileTime.fromMillis(40));
      service.queueUpload(toUpload);

      Thread.sleep(700);
      Files.setLastModifiedTime(toUpload, FileTime.fromMillis(50));
      cache.freeze(root, Pattern.compile(".*\\.freeze"), 60);

      Thread.sleep(700);
      service.queueUpload(toUpload);

      service.terminate();
      service.awaitTermination();

    } finally {
      Files.delete(toUpload);
      Files.delete(cacheFile);
      Files.delete(root);
    }

    Assert.assertEquals(2, strategy.Count);
  }

  @Test
  public void testFrozenBeforeCreation() throws IOException, InterruptedException {
    final Path cacheFile = Files.createTempFile(TEST_DIRECTORY, "", "");

    final Path root = Files.createTempDirectory(TEST_DIRECTORY, "testFrozenFiles");
    Path toUpload = null;

    final TestingStrategy strategy = new TestingStrategy();
    try (final UploaderCache cache = new UploaderCache(CONTEXT, cacheFile);
         final UploaderService service = new UploaderService(CONTEXT, cache, new TestingRouter(), strategy)) {


      cache.freeze(root, Pattern.compile(".*\\.freeze"), 50);

      toUpload = Files.createTempFile(root, "after", ".freeze");
      Files.setLastModifiedTime(toUpload, FileTime.fromMillis(60));

      Thread.sleep(500);
      service.queueUpload(toUpload);

      service.terminate();
      service.awaitTermination();

    } finally {
      if (toUpload != null)
        Files.delete(toUpload);
      Files.delete(cacheFile);
      Files.delete(root);
    }

    Assert.assertEquals(0, strategy.Count);
  }

  /**
   * If a file is frozen, has never been uploaded, but it's date is younger than the freeze date, it should still be
   * uploaded.
   *
   * @throws IOException
   */
  @Test
  public void testFrozenBeforeCreationBackup() throws IOException, InterruptedException {
    final Path cacheFile = Files.createTempFile(TEST_DIRECTORY, "", "");

    final Path root = Files.createTempDirectory(TEST_DIRECTORY, "testFrozenFiles");
    final Path toUpload = Files.createTempFile(root, ".freeze", "");

    final TestingStrategy strategy = new TestingStrategy();
    try (final UploaderCache cache = new UploaderCache(CONTEXT, cacheFile);
         final UploaderService service = new UploaderService(CONTEXT, cache, new TestingRouter(), strategy)) {

      Files.write(toUpload, "data".getBytes());
      Files.setLastModifiedTime(toUpload, FileTime.fromMillis(50));
      cache.freeze(root, Pattern.compile(".*\\.freeze"), 60);
      service.queueUpload(toUpload);

      service.terminate();
      service.awaitTermination();

    } finally {
      Files.delete(toUpload);
      Files.delete(cacheFile);
      Files.delete(root);
    }

    Assert.assertEquals(1, strategy.Count);
  }
}