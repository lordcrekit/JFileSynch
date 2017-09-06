package com.github.lordcrekit;

import org.json.JSONObject;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Path;
import java.util.*;
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

  static String makeAddress() {
    return "inproc://"
        + UploaderCacheThread.class.getName()
        + Integer.toString(new Random().nextInt(46655)) // Up to 'zzz'
        + "." + Long.toString(System.currentTimeMillis(), 36);
  }

  /**
   * Because we can't interrupt the thread, this boolean is checked each loop.
   */
  AtomicBoolean CloseNow = new AtomicBoolean(false);

  // <editor-fold defaultState="collapsed" desc="Communication">
  private final ZContext context;
  private final String address;
  // </editor-fold>

  // <editor-fold defaultState="collapsed" desc="Cached information">
  private final Set<Pattern> ignoredPatterns = new HashSet<>();

  private final Map<Pattern, Long> frozenPatterns = new HashMap<>();

  /**
   * The timestamp on a file when it was frozen.
   */
  private final Map<Path, Long> timestampsWhenFrozen = new HashMap<>();
  // </editor-fold>

  UploaderCacheThread(ZContext context, String address) {
    this.context = context;
    this.address = address;
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
            sock.send(SUCCESS_RESPONSE);
            write();
            break;

          case IGNORE_COMMAND:
            sock.send(SUCCESS_RESPONSE);
            write();
            break;

          case UPDATE_COMMAND:
            sock.send(SUCCESS_RESPONSE);
            write();
            break;

          case TERMINATE_COMMAND:
            sock.send(SUCCESS_RESPONSE);
            break loop;

          case GET_FILE_STATUS:
            final String path = msg.getString("f");

            final JSONObject status = new JSONObject();
            status.put("i", isIgnored(path));
            status.put("f", isFrozen(path));
            status.put("ft", this.timestampsWhenFrozen.containsKey(path) ? this.timestampsWhenFrozen.get(path) : "-1");
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

  private boolean isIgnored(String path) {
    return false;
  }

  private long isFrozen(String path) {
    return 50;
  }

  private final void read() {
  }

  private final void write() {
  }
}
