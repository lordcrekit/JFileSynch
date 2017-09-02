package com.github.lordcrekit;

import org.json.JSONObject;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Random;

final class UploaderCacheThread implements Runnable {
  // update commands
  static final byte FREEZE_COMMAND = 'f';
  static final byte IGNORE_COMMAND = 'i';
  static final byte UPDATE_COMMAND = 'u';
  static final byte TERMINATE_COMMAND = 't';

  // information request commands
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
            sock.send("d");
            break loop;

          default:
            assert false;
        }
      }
    } finally {
      context.destroySocket(sock);
    }
  }

  private final void read() {
  }

  private final void write() {
  }
}
