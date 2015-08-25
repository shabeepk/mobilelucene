package org.lukhnos.portmobile.file.attribute;

import java.io.File;

public class BasicFileAttributes {
  File file;

  public BasicFileAttributes(File file) {
    this.file = file;
  }

  BasicFileAttributes() {
    file = null;
  }

  /**
   * WARNING: This is NOT the actual creation time. This curretly works just because of how
   * SimpleFSLock works!!
   */
  public FileTime creationTime() {
    if (file != null) {
      return new FileTime(file.lastModified());
    }
    return new FileTime(0);
  }
}
