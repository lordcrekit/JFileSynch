package com.github.lordcrekit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

class UploaderCacheInformation {
    final List<Pattern> IgnoredPatterns;

    final Map<Pattern, Long> FrozenPatterns;

    /**
     * The timestamp on a file when it was frozen.
     */
    final Map<Path, Long> TimestampsWhenFrozen;

    final Map<Path, Long> Timestamps;

    public UploaderCacheInformation() {
      this.IgnoredPatterns = new ArrayList<>();
      this.FrozenPatterns = new HashMap<>();
      this.TimestampsWhenFrozen = new HashMap<>();
      this.Timestamps = new HashMap<>();
    }

    public UploaderCacheInformation(final JSONObject json) {
      this.IgnoredPatterns = new ArrayList<>();
      final JSONArray ignoreAr = json.has("i") ? json.getJSONArray("i") : new JSONArray();
      for (int i = 0; i < ignoreAr.length(); i++)
        this.IgnoredPatterns.add(Pattern.compile(ignoreAr.getString(i)));

      this.FrozenPatterns = new HashMap<>();
      this.TimestampsWhenFrozen = new HashMap<>();
      this.Timestamps = new HashMap<>();
    }

    @Override
    public final boolean equals(Object o) {
      return o instanceof UploaderCacheInformation && equals((UploaderCacheInformation) o);
    }

    public final boolean equals(UploaderCacheInformation o) {
      return comparePatternList(this.IgnoredPatterns, o.IgnoredPatterns)
          && this.FrozenPatterns.equals(o.FrozenPatterns)
          && this.TimestampsWhenFrozen.equals(o.TimestampsWhenFrozen)
          && this.Timestamps.equals(o.Timestamps);
    }

    public final JSONObject toJSON() {
      final JSONObject obj = new JSONObject();
      obj.put("i", this.IgnoredPatterns);
      obj.put("f", this.FrozenPatterns);
      obj.put("ft", this.TimestampsWhenFrozen);
      obj.put("t", this.Timestamps);
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
}
