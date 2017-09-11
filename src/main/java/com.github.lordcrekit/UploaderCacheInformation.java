package com.github.lordcrekit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The actual information stored by an UploaderCacheThread (accessed by an UploaderCache).
 *
 * @see UploaderCache
 * @see UploaderCacheThread
 */
class UploaderCacheInformation {

  /**
   *
   */
  final List<Pattern> IgnoredPatterns;

  /**
   *
   */
  final Map<Pattern, Long> FrozenPatterns;

  /**
   * The timestamp on a file when it was frozen.
   */
  final Map<Path, Long> TimestampsWhenFrozen;

  /**
   * The timestamp on files that have been uploaded.
   */
  final Map<Path, Long> Timestamps;

  UploaderCacheInformation() {
    this.IgnoredPatterns = new ArrayList<>();
    this.FrozenPatterns = new LinkedHashMap<>();
    this.TimestampsWhenFrozen = new HashMap<>();
    this.Timestamps = new HashMap<>();
  }

  UploaderCacheInformation(final JSONObject json) {
    this.IgnoredPatterns = new ArrayList<>();
    final JSONArray ignoreAr = json.has("i") ? json.getJSONArray("i") : new JSONArray();
    for (int i = 0; i < ignoreAr.length(); i++)
      this.IgnoredPatterns.add(Pattern.compile(ignoreAr.getString(i)));

    this.FrozenPatterns = new LinkedHashMap<>();
    final JSONObject frozenObj = json.has("f") ? json.getJSONObject("f") : new JSONObject();
    for (Iterator<String> key = frozenObj.keys(); key.hasNext(); ) {
      final String p = key.next();
      final long t = frozenObj.getLong(p);
      this.FrozenPatterns.put(Pattern.compile(p), t);
    }

    this.TimestampsWhenFrozen = new HashMap<>();
    final JSONObject frozenTimeObj = json.has("ft") ? json.getJSONObject("ft") : new JSONObject();
    for (Iterator<String> key = frozenTimeObj.keys(); key.hasNext();) {
      final String p = key.next();
      final long t = frozenTimeObj.getLong(p);
      this.TimestampsWhenFrozen.put(Paths.get(p), t);
    }

    this.Timestamps = new HashMap<>();
    final JSONObject timeObj = json.has("t") ? json.getJSONObject("t") : new JSONObject();
    for (Iterator<String> key = timeObj.keys(); key.hasNext();) {
      final String p = key.next();
      final long t = timeObj.getLong(p);
      this.Timestamps.put(Paths.get(p), t);
    }
  }

  final void addFrozenTimestamps(final Path root, final Pattern pattern) {

  }

  @Override
  public final boolean equals(Object o) {
    return o instanceof UploaderCacheInformation && equals((UploaderCacheInformation) o);
  }

  public final boolean equals(UploaderCacheInformation o) {
    return comparePatternList(this.IgnoredPatterns, o.IgnoredPatterns)
        && comparePatternLongMap(this.FrozenPatterns, o.FrozenPatterns)
        && this.TimestampsWhenFrozen.equals(o.TimestampsWhenFrozen)
        && this.Timestamps.equals(o.Timestamps);
  }


  public final JSONObject toJSON() {
    final JSONObject obj = new JSONObject();
    {
      final JSONArray ignAr = new JSONArray();
      for (Pattern p : this.IgnoredPatterns)
        ignAr.put(p.pattern());
      obj.put("i", IgnoredPatterns);
    }
    {
      final JSONObject frzAr = new JSONObject();
      for (Map.Entry<Pattern, Long> e : this.FrozenPatterns.entrySet())
        frzAr.put(e.getKey().pattern(), e.getValue());
      obj.put("f", frzAr);
    }
    {
      final JSONObject frzTimes = new JSONObject();
      for (Map.Entry<Path, Long> e : this.TimestampsWhenFrozen.entrySet())
        frzTimes.put(e.getKey().toString(), e.getValue());
      obj.put("ft", frzTimes);
    }
    {
      final JSONObject timeAr = new JSONObject();
      for (Map.Entry<Path, Long> e : this.Timestamps.entrySet())
        timeAr.put(e.getKey().toString(), e.getValue());
      obj.put("t", timeAr);
    }
    return obj;
  }

  @Override
  public final String toString() {
    return this.toJSON().toString();
  }

  private static final boolean comparePatternList(List<Pattern> o1, List<Pattern> o2) {
    if (o1.size() != o2.size())
      return false;
    for (int i = 0; i < o1.size(); i++) {
      if (!o1.get(i).pattern().equals(o2.get(i).pattern()))
        return false;
    }
    return true;
  }

  /**
   * This only works if both Maps are ordered (for example, {@link java.util.LinkedHashMap}).
   *
   * @param o1
   * @param o2
   * @return
   */
  private static final boolean comparePatternLongMap(Map<Pattern, Long> o1, Map<Pattern, Long> o2) {
    if (o1.size() != o2.size())
      return false;

    final Set<Map.Entry<Pattern, Long>> o1s = o1.entrySet();
    final Set<Map.Entry<Pattern, Long>> o2s = o2.entrySet();
    final Iterator<Map.Entry<Pattern, Long>> o1si = o1s.iterator();
    final Iterator<Map.Entry<Pattern, Long>> o2si = o2s.iterator();

    while (o1si.hasNext()) {
      final Map.Entry<Pattern, Long> o1e = o1si.next();
      final Map.Entry<Pattern, Long> o2e = o2si.next();
      if (!(o1e.getKey().pattern().equals(o2e.getKey().pattern())
          && o1e.getValue().equals(o2e.getValue())))
        return false;
    }
    return true;

  }
}
