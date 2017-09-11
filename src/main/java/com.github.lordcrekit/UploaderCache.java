package com.github.lordcrekit;

import org.json.JSONObject;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

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

  private final String threadAddress;
  private final UploaderCacheThread threadService;
  private final Thread thread;

  /**
   *
   * @param context
   * @param cacheFile
   */
  public UploaderCache(final ZContext context, final Path cacheFile) {
    this.context = context;

    this.threadAddress = UploaderService.makeAddress(UploaderCacheThread.class.getSimpleName());
    this.threadService = new UploaderCacheThread(context, threadAddress, cacheFile);
    this.thread = new Thread(this.threadService, UploaderCache.class.getSimpleName());
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
  public void freeze(final Path root, final Pattern pattern, final long timestamp) {
    final ZMQ.Socket sock = this.context.createSocket(ZMQ.REQ);
    try {
      sock.connect(this.threadAddress);
      final JSONObject msg = new JSONObject();
      msg.put("c", UploaderCacheThread.FREEZE_COMMAND);
      msg.put("r", root.toString());
      msg.put("p", pattern.toString());
      msg.put("t", timestamp);

      sock.send(msg.toString());
      final byte[] code = sock.recv();
      assert code == UploaderCacheThread.SUCCESS_RESPONSE;
    } finally {
      this.context.destroySocket(sock);
    }
  }

  /**
   * Tell the cache that a pattern should be ignored.
   *
   * @param pattern
   *     The pattern to ignore. Any files(resolved) that this pattern matches will not be uploaded.
   */
  public void ignore(final Pattern pattern) {
    final ZMQ.Socket sock = this.context.createSocket(ZMQ.REQ);
    try {
      sock.connect(this.threadAddress);
      final JSONObject msg = new JSONObject();
      msg.put("c", UploaderCacheThread.IGNORE_COMMAND);
      msg.put("p", pattern.toString());

      sock.send(msg.toString());
      final byte[] code = sock.recv();
      assert code == UploaderCacheThread.SUCCESS_RESPONSE;
    } finally {
      this.context.destroySocket(sock);
    }
  }

  /**
   * Update the cache with the most recent upload date of a file.
   *
   * @param file
   * @param timestamp
   */
  public void update(final Path file, final long timestamp) {
    final ZMQ.Socket sock = this.context.createSocket(ZMQ.REQ);
    try {
      sock.connect(this.threadAddress);
      final JSONObject msg = new JSONObject();
      msg.put("c", UploaderCacheThread.UPDATE_COMMAND);
      msg.put("f", file.normalize().toString());
      msg.put("t", timestamp);

      sock.send(msg.toString());
      final byte[] code = sock.recv();
      assert code == UploaderCacheThread.SUCCESS_RESPONSE;
    } finally {
      this.context.destroySocket(sock);
    }
  }

  /**
   * @param p
   * @return
   */
  public UploaderCacheFileInfo getFileInformation(final Path p) {
    final ZMQ.Socket sock = this.context.createSocket(ZMQ.REQ);
    try {
      sock.connect(this.threadAddress);
      final JSONObject msg = new JSONObject();
      msg.put("c", UploaderCacheThread.GET_FILE_STATUS);
      msg.put("f", p.normalize().toString());

      sock.send(msg.toString());
      final byte[] statusMsg_bytes = sock.recv();

      final JSONObject statusMsg = new JSONObject(new String(statusMsg_bytes));
      return new UploaderCacheFileInfo(statusMsg);

    } finally {
      this.context.destroySocket(sock);
    }
  }

  /**
   * Get all the information currently stored in the entire UploaderCache.
   *
   * @return
   */
  public UploaderCacheInformation getCacheInformation() {

    final ZMQ.Socket sock = this.context.createSocket(ZMQ.REQ);
    try {
      sock.connect(this.threadAddress);

      final JSONObject msg = new JSONObject();
      msg.put("c", UploaderCacheThread.GET_CACHE_STATUS);
      sock.send(msg.toString());

      final byte[] res = sock.recv();
      final JSONObject resObj = new JSONObject(new String(res));
      return new UploaderCacheInformation(resObj);

    } finally {
      this.context.destroySocket(sock);
    }
  }

  @Override
  public void close() throws IOException {

    final ZMQ.Socket sock = this.context.createSocket(ZMQ.REQ);
    try {
      sock.connect(this.threadAddress);

      final JSONObject msg = new JSONObject();
      msg.put("c", UploaderCacheThread.TERMINATE_COMMAND);
      sock.send(msg.toString());

      this.threadService.CloseNow.set(true);
    } finally {
      this.context.destroySocket(sock);
    }

    try {
      this.thread.join();
    } catch (InterruptedException e) {
      assert false;
    }
  }
}
