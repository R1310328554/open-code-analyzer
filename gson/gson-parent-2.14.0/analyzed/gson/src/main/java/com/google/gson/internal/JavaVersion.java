/*
 * Copyright (C) 2017 The Gson authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal;

/** 检测当前 JVM 主版本号。 */
public final class JavaVersion {

  private static final int majorJavaVersion = determineMajorJavaVersion();

  private static int determineMajorJavaVersion() {
    return parseMajorJavaVersion(System.getProperty("java.version"));
  }

  static int parseMajorJavaVersion(String javaVersion) {
    int version = parseDotted(javaVersion);
    if (version == -1) version = extractBeginningInt(javaVersion);
    if (version == -1) return 6;
    return version;
  }

  private static int parseDotted(String javaVersion) {
    try {
      String[] parts = javaVersion.split("[._]", 3);
      int firstVer = Integer.parseInt(parts[0]);
      return firstVer == 1 && parts.length > 1 ? Integer.parseInt(parts[1]) : firstVer;
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private static int extractBeginningInt(String javaVersion) {
    try {
      StringBuilder num = new StringBuilder();
      for (int i = 0; i < javaVersion.length(); ++i) {
        char c = javaVersion.charAt(i);
        if (Character.isDigit(c)) num.append(c);
        else break;
      }
      return Integer.parseInt(num.toString());
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  /** @return Java 主版本号（1.8→8，9→9） */
  public static int getMajorJavaVersion() {
    return majorJavaVersion;
  }

  /** @return 是否 Java 9+ */
  public static boolean isJava9OrLater() {
    return majorJavaVersion >= 9;
  }

  private JavaVersion() {}
}
