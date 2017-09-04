package com.github.lordcrekit;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The UploadService is responsible for queuing the upload of files.
 * <p>
 * It controls one thread and communicates with it through ZMQ.PUSH Sockets.
 *
 * @author William A. Norman <a href="norman.william.dev@gmail.com/>
 */
public class UploaderService implements Closeable {

  final static Level SOCKET_LOGGING_LEVEL = Level.INFO;

  private final ZContext context;

  private final String address;
  private final UploaderCache cache;
  private final UploaderRouter router;
  private final UploaderStrategy strategy;

  private Thread thread;

  /**
   * Create a new UploadService.
   */
  public UploaderService(final ZContext context,
                         final UploaderCache cache,
                         final UploaderRouter router,
                         final UploaderStrategy strategy) {

    this.context = context;

    this.address = "inproc://"
        + UploaderService.class.getName()
        + Integer.toString(new Random().nextInt(), 36)
        + Long.toString(System.currentTimeMillis(), 36);

    this.cache = cache;
    this.router = router;
    this.strategy = strategy;
  }

  public void start() {
    if (this.thread == null) {
      this.thread = new Thread(new UploaderServiceThread(this.context, this.address, this.cache, this.strategy));
      this.thread.start();
    }
  }

  /**
   * Queue a file for upload.
   *
   * @param file
   *     The file to upload.
   * @param destination
   *     The destination to upload to.
   */
  public void queueUpload(final Path file, final URI destination) {
    final ZMQ.Socket sock = this.context.createSocket(ZMQ.PUSH);
    try {
      sock.connect(this.address);
      final JSONObject msg = new JSONObject();
      msg.put("c", UploaderServiceThread.QUEUE_COMMAND);
      msg.put("f", file.normalize().toString());
      msg.put("u", destination.normalize().toString());

      sock.send(msg.toString());
    } finally {
      context.destroySocket(sock);
      Logger.getLogger(UploaderService.class.getName()).log(
          UploaderService.SOCKET_LOGGING_LEVEL,
          "Closing queueing socket");
    }
  }

  /**
   * Queue a file for upload. The service's {@link UploaderRouter} determines locations to upload to.
   *
   * @param file
   *     The file to upload.
   */
  public void queueUpload(final Path file) {
    final List<URI> destinations = this.router.route(file);
    if (destinations.isEmpty()) {
      return;
    }

    final ZMQ.Socket sock = this.context.createSocket(ZMQ.PUSH);
    try {
      sock.connect(this.address);

      for (URI uri : destinations) {
        final JSONObject msg = new JSONObject();
        msg.put("c", UploaderServiceThread.QUEUE_COMMAND);
        msg.put("f", file.normalize().toString());
        msg.put("u", uri.normalize().toString());

        sock.send(msg.toString());
      }
    } finally {
      context.destroySocket(sock);
      Logger.getLogger(UploaderService.class.getName()).log(
          UploaderService.SOCKET_LOGGING_LEVEL,
          "Closing queuing socket");
    }
  }

  /**
   * Ask the UploadService to terminate operation. It will finish all uploads asked of it. If you want it to terminate
   * as soon as possible, call {@link #close()}.
   */
  public void terminate() {
    final ZMQ.Socket sock = this.context.createSocket(ZMQ.PUSH);
    try {
      sock.connect(this.address);
      final JSONObject msg = new JSONObject();
      msg.put("c", UploaderServiceThread.TERMINATE_COMMAND);

      sock.send(msg.toString());
    } finally {
      context.destroySocket(sock);
      Logger.getLogger(UploaderService.class.getName()).log(
          UploaderService.SOCKET_LOGGING_LEVEL,
          "Closing termination socket");
    }
  }

  /**
   * Wait for the UploadService to terminate naturally. It will complete every upload asked of it before it was asked to
   * terminate.
   *
   * @throws InterruptedException
   *     If the thread awaiting termination is interrupted.
   */
  public void awaitTermination() throws InterruptedException {
    this.thread.join();
  }

  @Override
  public void close() throws IOException {
    this.terminate();
    try {
      this.thread.interrupt();
      this.awaitTermination();
    } catch (InterruptedException e) {
      throw new IOException("Do not interrupt thread closing UploaderService.", e);
    }
  }
}