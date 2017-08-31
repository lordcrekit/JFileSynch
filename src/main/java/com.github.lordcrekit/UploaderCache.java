package com.github.lordcrekit;

import org.zeromq.ZContext;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * The UploaderCache is a shared cache for all uploading components of a given directory. If the cache is within the
 * files being uploaded, it is recommended to set it or it's directory to frozen / ignored, otherwise you could have a
 * loop.
 *
 * @author William A. Norman <a href="norman.william.dev@gmail.com"/>
 */
public class UploaderCache implements Closeable {

  private final ZContext context;
  private final Path cacheFile;

  private final String threadAddress;
  private final Thread thread;

  public UploaderCache(ZContext context, Path cacheFile) {
    this.context = context;
    this.cacheFile = cacheFile;

    this.threadAddress = UploaderCacheThread.makeAddress();
    this.thread = new Thread(new UploaderCacheThread(context, threadAddress));
    this.thread.start();
  }

  /**
   * Tell the cache that a pattern should be frozen.
   *
   * @param pattern
   *     The pattern to freeze.
   * @param timestamp
   *     The time to freeze it at. This can be set to the future, if you know you're going to want to freeze at a
   *     certain time.
   */
  public void freeze(Pattern pattern, long timestamp) {
  }

  /**
   * Tell the cache that a pattern should be ignored.
   *
   * @param pattern
   *     The pattern to ignore. Any files(resolved) that this pattern matches will not be uploaded.
   */
  public void ignore(Pattern pattern) {
  }

  /**
   * Update the cache with the most recent upload date of a file.
   *
   * @param file
   * @param timestamp
   */
  public void update(Path file, long timestamp) {
  }

  public long getTimestamp(Path file) {
    return -1;
  }

  @Override
  public void close() throws IOException {

  }
}
