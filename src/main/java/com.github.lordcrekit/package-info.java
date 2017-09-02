/**
 * Everything that the UploadService uses.
 * <p>
 * All public classes are actually just handlers that communicate with worker threads through ZMQ sockets. Their calls
 * (unless otherwise specified) are asynchronous.
 *
 * @see com.github.lordcrekit.UploaderService
 */
package com.github.lordcrekit;