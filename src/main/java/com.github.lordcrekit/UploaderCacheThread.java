package com.github.lordcrekit;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.regex.Pattern;

final class UploaderCacheThread implements Runnable {

  // Responses
  static final byte[] SUCCESS_RESPONSE = new byte[]{'d'};
  static final byte[] FAILURE_RESPONSE = new byte[]{'f'};

  // Update commands
  static final byte FREEZE_COMMAND = 'f';
  static final byte IGNORE_COMMAND = 'i';
  static final byte UPDATE_COMMAND = 'u';
  static final byte TERMINATE_COMMAND = 't';

  // Information request commands
  static final byte GET_FILE_STATUS = 'g';
  static final byte GET_CACHE_STATUS = 'c';

  /**
   * Because we can't interrupt the thread, this boolean is checked each loop.
   */
  final AtomicBoolean CloseNow = new AtomicBoolean(false);

  private final ZContext context;
  private final String address;

  private final Path cacheFile;

  UploaderCacheInformation cache;

  UploaderCacheThread(final ZContext context, final String address, final Path cacheFile) {
    this.context = context;
    this.address = address;

    this.cacheFile = cacheFile;
  }

  @Override
  public void run() {
    final ZMQ.Socket sock = context.createSocket(ZMQ.REP);
    try {
      try {
        read();
      } catch (IOException e) {
        e.printStackTrace();
        assert false;
      }

      sock.bind(address);

      loop:
      while (!this.CloseNow.get()) {
        final byte[] msg_bytes = sock.recv();
        final JSONObject msg = new JSONObject(new String(msg_bytes));
        switch (msg.getInt("c")) {

          case FREEZE_COMMAND:
            try {
              final String pattern = msg.getString("p");
              final long timestamp = msg.getLong("t");
              this.cache.FrozenPatterns.put(Pattern.compile(pattern), timestamp);
              write();
              sock.send(SUCCESS_RESPONSE);
            } catch (IOException e) {
              sock.send(FAILURE_RESPONSE);
            }
            break;

          case IGNORE_COMMAND:
            try {
              final String pattern = msg.getString("p");
              this.cache.IgnoredPatterns.add(Pattern.compile(pattern));
              write();
              sock.send(SUCCESS_RESPONSE);
            } catch (IOException e) {
              sock.send(FAILURE_RESPONSE);
            }
            break;

          case UPDATE_COMMAND:
            try {
              final Path path = Paths.get(msg.getString("f"));
              final long timestamp = msg.getLong("t");
              this.cache.Timestamps.put(path, timestamp);
              write();
              sock.send(SUCCESS_RESPONSE);
            } catch (IOException e) {
              sock.send(FAILURE_RESPONSE);
            }
            break;

          case TERMINATE_COMMAND:
            sock.send(SUCCESS_RESPONSE);
            break loop;

          case GET_FILE_STATUS:
            final String path = msg.getString("f");

            final JSONObject status = new JSONObject();
            status.put("i", isIgnored(path));
            status.put("f", isFrozen(path));
            status.put("ft", this.cache.TimestampsWhenFrozen.containsKey(path)
                ? this.cache.TimestampsWhenFrozen.get(path)
                : "-1");
            status.put("t", 1); //this.timestamps.get(path));

            sock.send(status.toString());
            break;

          case GET_CACHE_STATUS:
            sock.send(this.cache.toJSON().toString());
            break;

          default:
            sock.send(FAILURE_RESPONSE);
            assert false;
        }
      }
    } finally {
      Logger.getLogger(UploaderCacheThread.class.getName()).log(
          UploaderService.SOCKET_LOGGING_LEVEL, "Closing thread socket.");
      context.destroySocket(sock);
    }
  }

  private boolean isIgnored(final String path) {
    return false;
  }

  private long isFrozen(final String path) {
    return 50;
  }

  private final void read() throws IOException {
    if (!Files.exists(this.cacheFile) || Files.size(this.cacheFile) == 0)
      this.cache = new UploaderCacheInformation();
    else
      try (final Reader rdr = Files.newBufferedReader(this.cacheFile)) {
        final JSONObject obj = new JSONObject(new JSONTokener(rdr));
        this.cache = new UploaderCacheInformation(obj);
      }
  }

  private final void write() throws IOException {
    try (final Writer writer = Files.newBufferedWriter(this.cacheFile)) {
      this.cache.toJSON().write(writer);
    }
  }
}
