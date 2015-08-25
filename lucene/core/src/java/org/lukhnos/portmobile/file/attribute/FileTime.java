package org.lukhnos.portmobile.file.attribute;

public class FileTime implements Comparable<FileTime> {
  long time;

  FileTime(long time) {
    this.time = time;
  }

  public static FileTime fromMillis(long value) {
    return new FileTime(value);
  }

  public String toString() {
    return Long.toString(time);
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof FileTime) ? compareTo((FileTime)obj) == 0 : false;
  }

  @Override
  public int compareTo(FileTime o) {
    if (o.time < time) {
      return -1;
    } else if (o.time == time) {
      return 0;
    }
    return 1;
  }
}
