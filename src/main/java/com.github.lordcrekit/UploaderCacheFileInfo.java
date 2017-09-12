package com.github.lordcrekit;

import org.json.JSONObject;

import java.nio.file.Path;

final class UploaderCacheFileInfo {

  /**
   * If the file should be ignored.
   */
  final boolean Ignored;

  /**
   * The time that the file was frozen at, or <code>-1</code> if it is not frozen.
   */
  final long TimeFrozen;

  /**
   * The timestamp on the file when it was frozen. Will be <code>-1</code> if it was never frozen.
   */
  final long TimestampWhenFrozen;

  /**
   * The last Timestamp of the file when it was last uploaded, or <code>-1</code> if it was never uploaded.
   */
  final long TimeUploaded;

  UploaderCacheFileInfo(
      final boolean ignored,
      final long timeFrozen,
      final long timestampWhenFrozen,
      final long timeUploaded) {

    this.Ignored = ignored;
    this.TimeFrozen = timeFrozen;
    this.TimestampWhenFrozen = timestampWhenFrozen;
    this.TimeUploaded = timeUploaded;
  }

  UploaderCacheFileInfo(final UploaderCacheInformation cache, final Path path) {
    this.Ignored = cache.isIgnored(path);
    this.TimeFrozen = cache.isFrozen(path);
    this.TimestampWhenFrozen = cache.TimestampsWhenFrozen.containsKey(path)
        ? cache.TimestampsWhenFrozen.get(path) : -1;
    this.TimeUploaded = cache.Timestamps.containsKey(path)
        ? cache.Timestamps.get(path) : -1;
  }

  UploaderCacheFileInfo(final JSONObject json) {
    this.Ignored = json.has("i")
        ? json.getBoolean("i")
        : false;
    this.TimeFrozen = json.has("f")
        ? json.getLong("f")
        : -1;
    this.TimestampWhenFrozen= json.has("ft")
        ? json.getLong("ft")
        : -1;
    this.TimeUploaded = json.has("t")
        ? json.getLong("t")
        : -1;
  }

  public JSONObject toJSON() {
    final JSONObject obj = new JSONObject();
    if (this.Ignored)
      obj.put("i", this.Ignored);
    if (this.TimeFrozen > 0)
      obj.put("f", this.TimeFrozen);
    if (this.TimestampWhenFrozen > 0)
      obj.put("ft", this.TimestampWhenFrozen);
    if (this.TimeUploaded > 0)
      obj.put("t", this.TimeUploaded);

    return obj;
  }

  @Override
  public boolean equals(final Object o) {
    return o instanceof UploaderCacheFileInfo && this.equals((UploaderCacheFileInfo) o);
  }

  public boolean equals(final UploaderCacheFileInfo o) {
    return this.Ignored == o.Ignored
        && this.TimeFrozen == o.TimeFrozen
        && this.TimestampWhenFrozen == o.TimestampWhenFrozen
        && this.TimeUploaded == o.TimeUploaded;
  }

  @Override
  public String toString() {
    return UploaderCacheFileInfo.class.getSimpleName()
        + "(i=" + Ignored
        + ", f=" + TimeFrozen
        + ", ft=" + TimestampWhenFrozen
        + ", t=" + TimeUploaded
        + ")";
  }
}
