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
import java.util.Random;
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

  private final Random rng = new Random();

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
        final String socketid = Integer.toString(rng.nextInt(), 36);

        final String msgStr = new String(sock.recv());
        final JSONObject msg = new JSONObject(msgStr);

        switch (msg.getInt("c")) {
          case QUEUE_COMMAND: {
            // <editor-fold defaultstat="collapsed" desc="Queue logic">

            final Path p = Paths.get(msg.getString("f"));
            final URI u;
            try {
              u = new URI(msg.getString("u"));
            } catch (URISyntaxException e) {
              e.printStackTrace();
              assert false;
              continue;
            }

            // <editor-fold defaultstate="collapsed" desc="File doesn't exist">
            if (!Files.exists(p)) {
              Logger.getLogger(UploaderServiceThread.class.getName()).log(
                  UploaderService.BEHAVIOUR_LOGGING_LEVEL,
                  socketid + ": Not uploading " + p.getFileName() + " because it does not exist");
              continue;
            }
            // </editor-fold>

            final UploaderCacheFileInfo info = this.cache.getFileInformation(p);
            /*
            Logger.getLogger(UploaderServiceThread.class.getName()).log(
                UploaderService.BEHAVIOUR_LOGGING_LEVEL,
                socketid + ": " + info.toString());
            final UploaderCacheInformation lol = this.cache.getCacheInformation();
            Logger.getLogger(UploaderServiceThread.class.getName()).log(
                UploaderService.BEHAVIOUR_LOGGING_LEVEL,
                socketid + ": " + lol.toString());
            */

            // <editor-fold defaultstate="collapsed" desc="Ignored patterns">
            if (info.Ignored) {
              Logger.getLogger(UploaderServiceThread.class.getName()).log(
                  UploaderService.BEHAVIOUR_LOGGING_LEVEL,
                  socketid + ": Not uploading " + p.getFileName() + " because it's ignored");
              continue;
            }
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Frozen before creation">
            if (info.TimeFrozen > 0 && info.TimestampWhenFrozen < 0) {
              System.out.println(info.toString());
              Logger.getLogger(UploaderServiceThread.class.getName()).log(
                  UploaderService.BEHAVIOUR_LOGGING_LEVEL,
                  socketid + ": Not uploading " + p.getFileName() + " because it was frozen before creation");
              continue;
            }
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Upload non uploaded file">
            if (info.TimeUploaded < 0) {
              try {
                Logger.getLogger(UploaderServiceThread.class.getName()).log(
                    UploaderService.BEHAVIOUR_LOGGING_LEVEL,
                    socketid + ": Uploading " + p.getFileName() + " because it has not been uploaded");

                long newTime = this.strategy.upload(p, u);
                this.cache.update(p, newTime);
                continue;
              } catch (IOException e) {
                e.printStackTrace();
                continue;
              }
            }
            // </editor-fold>

            final long fileTimestamp;
            try {
              fileTimestamp = Files.getLastModifiedTime(p).toMillis();
            } catch (IOException e) {
              e.printStackTrace();
              continue;
            }

            // <editor-fold defaultstate="collapsed" desc="Already up to date">
            if (info.TimeUploaded >= fileTimestamp) {
              Logger.getLogger(UploaderServiceThread.class.getName()).log(
                  UploaderService.BEHAVIOUR_LOGGING_LEVEL,
                  socketid + ": Not uploading " + p.getFileName() + " because it is already up to date.");
              continue;
            }
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Deal with frozen behaviour"
            if (info.TimeFrozen > 0) {
              if (info.TimestampWhenFrozen > info.TimeUploaded) {
                Logger.getLogger(UploaderServiceThread.class.getName()).log(
                    UploaderService.BEHAVIOUR_LOGGING_LEVEL,
                    socketid + ": Uploading " + p.getFileName() + " because the last uploaded version does not match the frozen version");

                long newTime = this.strategy.upload(p, u);
                this.cache.update(p, newTime);
                continue;

              } else {
                Logger.getLogger(UploaderServiceThread.class.getName()).log(
                    UploaderService.BEHAVIOUR_LOGGING_LEVEL,
                    socketid + ": Not uploading " + p.getFileName() + " because it is frozen");
                continue;
              }
            }
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Upload it because it's changed and is not frozen">
            Logger.getLogger(UploaderServiceThread.class.getName()).log(
                UploaderService.BEHAVIOUR_LOGGING_LEVEL,
                socketid + ": Uploading " + p.getFileName() + " because it has changed since the last request");

            long newTime = this.strategy.upload(p, u);
            this.cache.update(p, newTime);
            continue;
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
