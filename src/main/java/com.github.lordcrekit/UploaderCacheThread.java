package com.github.lordcrekit;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Random;

final class UploaderCacheThread implements Runnable {

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
    ZMQ.Socket sock = context.createSocket(ZMQ.REP);
    try {
      sock.bind(this.address);
      while (Thread.interrupted()) {
        sock.recv();
        // Do stuff
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
