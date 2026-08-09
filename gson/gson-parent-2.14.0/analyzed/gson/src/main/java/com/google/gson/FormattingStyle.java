/*
 * Copyright (C) 2022 Google Inc.
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

package com.google.gson;

import com.google.gson.stream.JsonWriter;
import java.util.Objects;

/**
 * 用于控制序列化输出外观的类。
 *
 * <p>目前提供以下配置方法，未来可能会添加更多方法：
 *
 * <ul>
 *   <li>{@link #withNewline(String)}
 *   <li>{@link #withIndent(String)}
 *   <li>{@link #withSpaceAfterSeparators(boolean)}
 * </ul>
 *
 * @see GsonBuilder#setFormattingStyle(FormattingStyle)
 * @see JsonWriter#setFormattingStyle(FormattingStyle)
 * @see <a href="https://en.wikipedia.org/wiki/Newline">Wikipedia Newline article</a>
 * @since 2.11.0
 */
public class FormattingStyle {
  private final String newline;
  private final String indent;
  private final boolean spaceAfterSeparators;

  /**
   * 默认紧凑格式化样式：
   *
   * <ul>
   *   <li>无换行
   *   <li>无缩进
   *   <li>{@code ','} 和 {@code ':'} 后无空格
   * </ul>
   */
  public static final FormattingStyle COMPACT = new FormattingStyle("", "", false);

  /**
   * 默认美化打印格式化样式：
   *
   * <ul>
   *   <li>以 {@code "\n"} 作为换行
   *   <li>以两个空格作为缩进
   *   <li>{@code ':'} 与后续值之间有一个空格
   * </ul>
   */
  public static final FormattingStyle PRETTY = new FormattingStyle("\n", "  ", true);

  private FormattingStyle(String newline, String indent, boolean spaceAfterSeparators) {
    Objects.requireNonNull(newline, "newline == null");
    Objects.requireNonNull(indent, "indent == null");
    if (!newline.matches("[\r\n]*")) {
      throw new IllegalArgumentException(
          "Only combinations of \\n and \\r are allowed in newline.");
    }
    if (!indent.matches("[ \t]*")) {
      throw new IllegalArgumentException(
          "Only combinations of spaces and tabs are allowed in indent.");
    }
    this.newline = newline;
    this.indent = indent;
    this.spaceAfterSeparators = spaceAfterSeparators;
  }

  /**
   * 创建具有指定换行设置的 {@link FormattingStyle}。
   *
   * <p>可用于适配特定操作系统约定，例如为 Linux 和 macOS 硬编码 {@code "\n"}，为 Windows 使用
   * {@code "\r\n"}，或调用 {@link java.lang.System#lineSeparator()} 以匹配当前操作系统。
   *
   * <p>仅允许 {@code \n} 和 {@code \r} 的组合。
   *
   * @param newline 用作换行的字符串值
   * @return 新创建的 {@link FormattingStyle}
   * @see #getNewline()
   */
  public FormattingStyle withNewline(String newline) {
    return new FormattingStyle(newline, this.indent, this.spaceAfterSeparators);
  }

  /**
   * 创建具有指定缩进字符串的 {@link FormattingStyle}。
   *
   * <p>缩进中仅允许空格和制表符的组合。
   *
   * @param indent 用作缩进的字符串值
   * @return 新创建的 {@link FormattingStyle}
   * @see #getIndent()
   */
  public FormattingStyle withIndent(String indent) {
    return new FormattingStyle(this.newline, indent, this.spaceAfterSeparators);
  }

  /**
   * 创建在 JSON 输出的分隔符 {@code ','} 和 {@code ':'} 后是否使用空格的 {@link FormattingStyle}。
   *
   * <p>此设置对 {@linkplain #withNewline(String) 配置的换行} 无影响。若配置了非空换行，换行始终会添加在
   * {@code ','} 之后，且此情况下不会在 {@code ','} 后添加空格。
   *
   * @param spaceAfterSeparators 是否在 {@code ','} 和 {@code ':'} 后输出空格
   * @return 新创建的 {@link FormattingStyle}
   * @see #usesSpaceAfterSeparators()
   */
  public FormattingStyle withSpaceAfterSeparators(boolean spaceAfterSeparators) {
    return new FormattingStyle(this.newline, this.indent, spaceAfterSeparators);
  }

  /**
   * 返回用作换行的字符串值。
   *
   * @return 换行值
   * @see #withNewline(String)
   */
  public String getNewline() {
    return this.newline;
  }

  /**
   * 返回用作缩进的字符串值。
   *
   * @return 缩进值
   * @see #withIndent(String)
   */
  public String getIndent() {
    return this.indent;
  }

  /**
   * 返回是否在 {@code ','} 和 {@code ':'} 后使用空格。
   *
   * @see #withSpaceAfterSeparators(boolean)
   */
  public boolean usesSpaceAfterSeparators() {
    return this.spaceAfterSeparators;
  }
}
