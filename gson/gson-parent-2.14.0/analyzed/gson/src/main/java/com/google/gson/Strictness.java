package com.google.gson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * 表示 JSON {@linkplain JsonReader 读取器} 或 {@linkplain JsonWriter 写入器} 遵循
 * <a href="https://www.ietf.org/rfc/rfc8259.txt">RFC 8259</a> 语法时的严格程度模式。
 *
 * @see GsonBuilder#setStrictness(Strictness)
 * @see JsonReader#setStrictness(Strictness)
 * @see JsonWriter#setStrictness(Strictness)
 * @since 2.11.0
 */
public enum Strictness {
  /** 允许与 JSON 规范有较大偏离。 */
  LENIENT,
  /** 出于历史兼容，允许少量偏离。 */
  LEGACY_STRICT,
  /** 严格遵循 JSON 规范。 */
  STRICT
}
