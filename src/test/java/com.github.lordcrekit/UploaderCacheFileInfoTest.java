package com.github.lordcrekit;

import org.junit.Assert;
import org.junit.Test;

public class UploaderCacheFileInfoTest {
  @Test
  public void testEquals() {
    System.out.println("Test equals()");

    UploaderCacheFileInfo o1;
    UploaderCacheFileInfo o2;

    // Empty
    o1 = new UploaderCacheFileInfo(false, -1, -1, -1);
    o2 = new UploaderCacheFileInfo(false, -1, -1, -1);
    Assert.assertEquals(o1, o2);

    // Ignored
    o1 = new UploaderCacheFileInfo(true, -1, -1, -1);
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o2 = new UploaderCacheFileInfo(true, -1, -1, -1);
    Assert.assertEquals(o1, o2);

    // TimeFrozen
    o1 = new UploaderCacheFileInfo(false, 50, -1, -1);
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o2 = new UploaderCacheFileInfo(false, 50, -1, -1);
    Assert.assertEquals(o1, o2);

    // TimestampWhenFrozen
    o1 = new UploaderCacheFileInfo(false, -1, 50, -1);
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o2 = new UploaderCacheFileInfo(false, -1, 50, -1);
    Assert.assertEquals(o1, o2);

    // TimeUploaded
    o1 = new UploaderCacheFileInfo(false, -1, -1, 50);
    Assert.assertNotEquals(o1, o2);
    Assert.assertNotEquals(o2, o1);
    o2 = new UploaderCacheFileInfo(false, -1, -1, 50);
    Assert.assertEquals(o1, o2);
  }


  @Test
  public void testIO() {
    System.out.println("Test IO");
    UploaderCacheFileInfo o;
    UploaderCacheFileInfo n;

    o = new UploaderCacheFileInfo(false, -1, -1, -1);
    n = new UploaderCacheFileInfo(o.toJSON());
    Assert.assertEquals(o, n);

    o = new UploaderCacheFileInfo(true, -1, -1, -1);
    n = new UploaderCacheFileInfo(o.toJSON());
    Assert.assertEquals(o, n);

    o = new UploaderCacheFileInfo(false, 50, -1, -1);
    n = new UploaderCacheFileInfo(o.toJSON());
    Assert.assertEquals(o, n);

    o = new UploaderCacheFileInfo(false, -1, 50, -1);
    n = new UploaderCacheFileInfo(o.toJSON());
    Assert.assertEquals(o, n);

    o = new UploaderCacheFileInfo(false, -1, -1, 50);
    n = new UploaderCacheFileInfo(o.toJSON());
    Assert.assertEquals(o, n);
  }
}
