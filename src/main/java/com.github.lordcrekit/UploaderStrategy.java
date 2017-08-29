package com.github.lordcrekit;

import java.io.File;
import java.net.URI;

/**
 * The UploaderStrategy decides how files are uploaded.
 *
 * @author William A. Norman <a href="norman.william.dev@gmail.com"/>
 */
public interface UploaderStrategy {

  /**
   * @param file
   * @param destination
   * @return The timestamp on the file when it was uploaded, or negative if the
   * file failed to upload.
   */
  long upload(File file, URI destination);
}
