package com.github.lordcrekit;

import org.json.JSONObject;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Random;
import java.util.logging.Logger;

final class UploaderCacheThread implements Runnable {

  // Responses
  static final byte[] SUCCESS_RESPONSE = new byte[] {'d'};

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

  private final ZContext context;
  private final String address;

  UploaderCacheThread(ZContext context, String address) {
    this.context = context;
    this.address = address;
  }

  @Override
  public void run() {
    final ZMQ.Socket sock = context.createSocket(ZMQ.REP);
    try {
      sock.bind(address);

      loop:
      while (!Thread.interrupted()) {
        final JSONObject msg = new JSONObject(String.valueOf(sock.recv()));
        switch (msg.getInt("c")) {

          case FREEZE_COMMAND:
            break;

          case IGNORE_COMMAND:
            break;

          case UPDATE_COMMAND:
            break;

          case TERMINATE_COMMAND:
            // Note that JeroMQ doesn't need this, but native ZMQ does (?).
            sock.send(SUCCESS_RESPONSE);
            break loop;

          default:
            assert false;
        }
      }
    } finally {
      context.destroySocket(sock);
      Logger.getLogger(UploaderCacheThread.class.getName()).log(
          UploaderService.SOCKET_LOGGING_LEVEL, "Closed thread socket.");
    }
  }

  private final void read() {
  }

  private final void write() {
  }
}
