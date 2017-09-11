package com.github.lordcrekit;

import org.json.JSONObject;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

class UploaderServiceThread implements Runnable {

  final static byte QUEUE_COMMAND = 'q';
  final static byte TERMINATE_COMMAND = 't';

  AtomicBoolean CloseNow = new AtomicBoolean(false);

  private final ZContext context;
  private final String address;

  private final UploaderCache cache;
  private final UploaderStrategy strategy;

  UploaderServiceThread(final ZContext context,
                        final String address,
                        final UploaderCache cache,
                        final UploaderStrategy strategy) {
    this.context = context;
    this.address = address;
    this.cache = cache;
    this.strategy = strategy;
  }

  @Override
  public void run() {
    final ZMQ.Socket sock = context.createSocket(ZMQ.PULL);
    try {
      sock.bind(address);

      loop:
      while (!this.CloseNow.get()) {
        final String msgStr = new String(sock.recv());
        final JSONObject msg = new JSONObject(msgStr);

        switch (msg.getInt("c")) {
          case QUEUE_COMMAND: {
            // <editor-fold defaultStat="collapsed" desc="Queue logic">
            final Path p = Paths.get(msg.getString("f"));
            final URI u;
            try {
              u = new URI(msg.getString("u"));
            } catch (URISyntaxException e) {
              e.printStackTrace();
              assert false;
              continue;
            }

            if (!Files.exists(p))
              continue;

            final UploaderCacheFileInfo info = this.cache.getFileInformation(p);

            if (info.Ignored) {
              // Don't upload ignored files.
              Logger.getLogger(UploaderServiceThread.class.getName()).log(
                  UploaderService.BEHAVIOUR_LOGGING_LEVEL,
                  "Not uploading " + p.getFileName() + " because it's ignored");
              continue;
            }

            if (info.TimeFrozen > 0 && info.TimestampWhenFrozen < 0) {
              // Don't upload it if it was frozen before being created.
              System.out.println(info.toString());
              Logger.getLogger(UploaderServiceThread.class.getName()).log(
                  UploaderService.BEHAVIOUR_LOGGING_LEVEL,
                  "Not uploading " + p.getFileName() + " because it was frozen before creation.");
              continue;
            }

            if (info.TimeUploaded < 0) {
              try {
                // It hasn't been uploaded before, we can safely upload it.
                Logger.getLogger(UploaderServiceThread.class.getName()).log(
                    UploaderService.BEHAVIOUR_LOGGING_LEVEL,
                    "Uploading " + p.getFileName());
                long newTime = this.strategy.upload(p, u);
                this.cache.update(p, newTime);
              } catch (IOException e) {
                e.printStackTrace();
              }
              continue;
            }

            final long fileTimestamp;
            try {
              // Upload it if it not up to date.
              fileTimestamp = Files.getLastModifiedTime(p).toMillis();
              assert false;
            } catch (IOException e) {
              e.printStackTrace();
              continue;
            }
            break;
            // </editor-fold>
          }

          case TERMINATE_COMMAND:
            break loop;

          default:
            assert false;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      assert false;
    } finally {
      context.destroySocket(sock);
      Logger.getLogger(UploaderServiceThread.class.getName()).log(
          UploaderService.SOCKET_LOGGING_LEVEL, "Closing thread socket.");
    }
  }
}
