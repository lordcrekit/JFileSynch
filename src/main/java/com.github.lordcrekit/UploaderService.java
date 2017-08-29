package com.github.lordcrekit;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Random;

/**
 * The UploadService is responsible for queuing the upload of files.
 *
 * @author William A. Norman <a href="norman.william.dev@gmail.com/>
 */
public class UploaderService implements Closeable {

  private final String address;
  private final UploaderCache cache;
  private final UploaderRouter router;
  private final UploaderStrategy strategy;

  /**
   * Create a new UploadService.
   */
  public UploaderService(UploaderCache cache,
                         UploaderRouter router,
                         UploaderStrategy strategy) {

    this.address = "inproc://"
        + UploaderService.class.getName()
        + Integer.toString(new Random().nextInt(), 36)
        + Long.toString(System.currentTimeMillis(), 36);

    this.cache = cache;
    this.router = router;
    this.strategy = strategy;
  }

  public void start() {
  }

  /**
   * Queue a file for upload.
   *
   * @param file
   *     The file to upload.
   * @param destination
   *     The destination to upload to.
   */
  public void queueUpload(Path file, URI destination) {
  }

  /**
   * @param file
   */
  public void queueUpload(Path file) {
  }

  /**
   * Ask the UploadService to terminate operation. It will finish all uploads asked of it. If you want it to terminate
   * as soon as possible, call {@link #close()}.
   */
  public void terminate() {
  }

  /**
   * Wait for the UploadService to terminate naturally. It will complete every upload asked of it before it was asked to
   * terminate.
   */
  public void awaitTermination() {
  }

  @Override
  public void close() throws IOException {
  }
}
