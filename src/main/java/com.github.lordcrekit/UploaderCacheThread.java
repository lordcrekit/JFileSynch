package com.github.lordcrekit;

import org.json.JSONObject;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

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

  UploaderCache.CacheInfo cache;

  UploaderCacheThread(final ZContext context, final String address, final Path cacheFile) {
    this.context = context;
    this.address = address;

    this.cacheFile = cacheFile;
  }

  @Override
  public void run() {
    final ZMQ.Socket sock = context.createSocket(ZMQ.REP);
    try {
      read();

      final ZMQ.Poller poller = context.createPoller(1);
      sock.bind(address);

      loop:
      while (!this.CloseNow.get()) {
        final byte[] msg_bytes = sock.recv();
        final JSONObject msg = new JSONObject(new String(msg_bytes));
        switch (msg.getInt("c")) {

          case FREEZE_COMMAND:
            try {
              write();
              sock.send(SUCCESS_RESPONSE);
            } catch (IOException e) {
              sock.send(FAILURE_RESPONSE);
            }
            break;

          case IGNORE_COMMAND:
            try {
              write();
              sock.send(SUCCESS_RESPONSE);
            } catch (IOException e) {
              sock.send(FAILURE_RESPONSE);
            }
            break;

          case UPDATE_COMMAND:
            try {
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
            status.put("ft", this.cache.timestampsWhenFrozen.containsKey(path)
                ? this.cache.timestampsWhenFrozen.get(path)
                : "-1");
            status.put("t", 1); //this.timestamps.get(path));

            sock.send(status.toString());
            break;

          case GET_CACHE_STATUS:
            sock.send(SUCCESS_RESPONSE);
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

  private final void read() {
    this.cache = new UploaderCache.CacheInfo();
  }

  private final void write() throws IOException {
    try (final Reader rdr = Files.newBufferedReader(this.cacheFile)) {
      final JSONObject store = new JSONObject(rdr);
      this.cache = new UploaderCache.CacheInfo();
    }
  }
}
