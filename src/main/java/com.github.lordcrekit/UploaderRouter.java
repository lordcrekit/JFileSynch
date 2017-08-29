package com.github.lordcrekit;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

/**
 * The UploaderRouter decides where files should be uploaded to.
 *
 * @author William A. Norman <a href="norman.william.dev@gmail.com"/>
 */
public interface UploaderRouter {

  /**
   * Decide the destinations for a File.
   *
   * @param path
   * @return
   */
  List<URI> route(Path path);
}
