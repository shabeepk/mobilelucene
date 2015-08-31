package org.lukhnos.portmobile.util;

import java.util.Arrays;

public class Objects {
  public static <T> T requireNonNull(T obj) {
    if (obj == null) {
      throw new NullPointerException();
    }
    return obj;
  }

  public static <T> T requireNonNull(T obj, String msg) {
    if (obj == null) {
      throw new NullPointerException(msg);
    }
    return obj;
  }

  public static int hashCode(Object o) {
    return o == null ? 0 : o.hashCode();
  }

  public static int hash(Object... values) {
    return Arrays.hashCode(values);
  }

  public static String toString(Object o) {
    return o == null ? "null" : o.toString();
  }

  public static boolean equals(Object a, Object b) {
    if (a == null) {
      return b == null ? true : false;
    }

    return b == null ? false : a.equals(b);
  }
}
