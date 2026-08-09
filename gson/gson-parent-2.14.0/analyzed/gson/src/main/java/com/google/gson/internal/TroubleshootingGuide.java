package com.google.gson.internal;

public class TroubleshootingGuide {
  private TroubleshootingGuide() {}

  /** 创建指向指定故障排除章节的 URL。 */
  public static String createUrl(String id) {
    return "https://github.com/google/gson/blob/main/Troubleshooting.md#" + id;
  }
}
