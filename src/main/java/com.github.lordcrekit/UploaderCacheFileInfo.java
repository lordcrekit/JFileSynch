package com.github.lordcrekit;

import org.json.JSONObject;

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

  UploaderCacheFileInfo(boolean ignored, long timeFrozen, long timestampWhenFrozen, long timeUploaded) {
    this.Ignored = ignored;
    this.TimeFrozen = timeFrozen;
    this.TimestampWhenFrozen = timestampWhenFrozen;
    this.TimeUploaded = timeUploaded;
  }

  UploaderCacheFileInfo(JSONObject json) {
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
  public boolean equals(Object o) {
    return o instanceof UploaderCacheFileInfo && this.equals((UploaderCacheFileInfo) o);
  }

  public boolean equals(UploaderCacheFileInfo o) {
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
