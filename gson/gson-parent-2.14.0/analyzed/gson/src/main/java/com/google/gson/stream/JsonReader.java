/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.stream;

import com.google.gson.Strictness;
import com.google.gson.internal.JsonReaderInternalAccess;
import com.google.gson.internal.TroubleshootingGuide;
import com.google.gson.internal.bind.JsonTreeReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Objects;

/**
 * 将 JSON (<a href="https://www.ietf.org/rfc/rfc8259.txt">RFC 8259</a>) 编码值读取为令牌流。该流包含文字值（字
 * 符串、数字、布尔值和空值）以及对象和数组的开始和结束分隔符。标记按深度优先顺序遍历，与它们在 JSON 文档中出现的顺序相同。在 JSON 对象中，名称/值对由单个标记表示。
 * <h2>解析 JSON</h2>
 * 要为您自己的 JSON 流创建递归下降解析器，首先创建一个用于创建 {@code JsonReader} 的入口点方法。
 * <p>接下来，为 JSON 文本中的每个结构创建处理程序方法。您需要为每种对象类型和每种数组类型提供一个方法。
 * <ul> <li><strong>数组处理</strong>方法中，首先调用{@link #beginArray}来消耗数组的左括号。然后创建一个累积值的 while
 * 循环，当 {@link #hasNext} 为 false 时终止。最后，通过调用 {@link #endArray} 读取数组的右括号。
 * <li>在<strong>对象处理</strong>方法中，首先调用{@link #beginObject}来消耗对象的左大括号。然后创建一个 while
 * 循环，根据局部变量的名称为其赋值。当 {@link #hasNext} 为 false 时，此循环应终止。最后，通过调用 {@link #endObject}
 * 读取对象的右大括号。 </ul>
 * <p>当遇到嵌套对象或数组时，委托给相应的处理程序方法。
 * <p>当遇到未知名称时，严格解析器应该失败并出现异常。宽松的解析器应该调用 {@link #skipValue()} 来递归地跳过值的嵌套标记，否则可能会发生冲突。
 * <p>如果值可能为空，则应首先使用 {@link #peek()} 进行检查。可以使用 {@link #nextNull()} 或 {@link #skipValue()}
 * 使用空文字。
 * <h2>配置</h2>
 * 可以使用以下方法自定义该读取器的行为：
 * <ul> <li>{@link #setStrictness(Strictness)}，默认为 {@link Strictness#LEGACY_STRICT}
 * <li>{@link #setNestingLimit(int)}，默认为 {@value #DEFAULT_NESTING_LIMIT} </ul>
 * {@link com.google.gson.Gson} 类内部使用的 {@code JsonReader} 实例的默认配置有所不同，并且可以使用各种 {@link
 * com.google.gson.GsonBuilder} 方法进行调整。
 * <h2>示例</h2>
 * 假设我们想要解析如下消息流：
 * <pre>{@code [ { "id": 912345678901, "text": "如何在 Java 中读取 JSON 流？", "geo": null,
 * "user": { "name": "json_newb", "followers_count": 41 } }, { "id": 912345678902, "text":
 * "@json_newb只需使用 JsonReader！", "geo": [50.454722, -104.606667], "user": { "name":
 * "jesse", "followers_count": 2 } } ] }</pre>
 * 此代码实现了上述结构的解析器：
 * <pre>{@code public List<Message> readJsonStream(InputStream in) 抛出 IOException {
 * JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));尝试 { 返回
 * readMessagesArray(reader); } 最后 { reader.close(); } }
 * public List<Message> readMessagesArray(JsonReader reader) throws IOException {
 * List<Message> messages = new ArrayList<>();
 * reader.beginArray(); while (reader.hasNext()) { messages.add(readMessage(reader)); } rea
 * der.endArray();返回消息； }
 * 公共消息 readMessage(JsonReader reader) 抛出 IOException { long id = -1;字符串文本=空；用户用户=空；列表
 * <Double> geo = null;
 * reader.beginObject(); while (reader.hasNext()) { 字符串名称 = reader.nextName(); if (name.equ
 * als("id")) { id = reader.nextLong(); } else if (name.equals("text")) { text = reader.nex
 * tString(); } else if (name.equals("geo") && reader.peek() != JsonToken.NULL) { geo = rea
 * dDoublesArray(reader); } else if (name.equals("user")) { user = readUser(reader); } else
 *  { reader.skipValue(); } } reader.endObject();返回新消息（id，文本，用户，地理）； }
 * public List<Double> readDoublesArray(JsonReader reader) throws IOException {
 * List<Double> doubles = new ArrayList<>();
 * reader.beginArray(); while (reader.hasNext()) { doubles.add(reader.nextDouble()); } read
 * er.endArray();返回双打； }
 * public User readUser(JsonReader reader) throws IOException { String 用户名 = null; int
 * 追随者计数 = -1;
 * reader.beginObject(); while (reader.hasNext()) { 字符串名称 = reader.nextName(); if (name.equ
 * als("name")) { 用户名 = reader.nextString(); } else if (name.equals("followers_count")) { f
 * ollowersCount = reader.nextInt(); } else { reader.skipValue(); } } reader.endObject();返回
 * 新用户（用户名，关注者数量）； } } X
 * <h2>号码处理</h2>
 * 该读取器允许将数值读取为字符串，并将字符串值读取为数字。例如，可以使用 {@link #nextInt} 或 {@link #nextString} 读取 JSON 数组 {@
 * code [1, "1"]} 的两个元素。此行为旨在防止有损数字转换：double 是 JavaScript 唯一的数字类型，并且像 {@code 90071992547409
 * 93} 这样非常大的值无法在该平台上准确表示。为了最大限度地减少精度损失，应将极大的值作为 JSON 中的字符串进行写入和读取。
 * 非执行前缀
 * 使用 JSON 提供私有数据的 Web 服务器可能容易受到 <a
 * href="http://en.wikipedia.org/wiki/JSON#Cross-site_request_forgery"> 跨站点请求伪造 </a>
 * 攻击。在此类攻击中，恶意站点通过使用 HTML {@code <script>} 标记执行私有 JSON 文件来获取对私有 JSON 文件的访问权限。
 * <p>使用 <code> 前缀 JSON 文件")]}'\n"</code> 使它们不可被 {@code <script>} 标签执行，从而解除攻击。由于前缀是格式错误的
 * JSON，因此遇到它时严格解析会失败。当启用 {@linkplain #setStrictness(Strictness) lenient parsing}
 * 时，此类允许非执行前缀。
 * <p>Each {@code JsonReader} 可用于读取单个 JSON 流。此类的实例不是线程安全的。
 * @author Jesse Wilson
 * @since 1.6
 */
public class JsonReader implements Closeable {
  private static final long MIN_INCOMPLETE_INTEGER = Long.MIN_VALUE / 10;

  private static final int PEEKED_NONE = 0;
  private static final int PEEKED_BEGIN_OBJECT = 1;
  private static final int PEEKED_END_OBJECT = 2;
  private static final int PEEKED_BEGIN_ARRAY = 3;
  private static final int PEEKED_END_ARRAY = 4;
  private static final int PEEKED_TRUE = 5;
  private static final int PEEKED_FALSE = 6;
  private static final int PEEKED_NULL = 7;
  private static final int PEEKED_SINGLE_QUOTED = 8;
  private static final int PEEKED_DOUBLE_QUOTED = 9;
  private static final int PEEKED_UNQUOTED = 10;

<<<<<<< Updated upstream
  /** 返回此值时，字符串存于 peekedString。 */
=======
  /**
   */
>>>>>>> Stashed changes
  private static final int PEEKED_BUFFERED = 11;

  private static final int PEEKED_SINGLE_QUOTED_NAME = 12;
  private static final int PEEKED_DOUBLE_QUOTED_NAME = 13;
  private static final int PEEKED_UNQUOTED_NAME = 14;

<<<<<<< Updated upstream
  /** 返回此值时，整数值存于 peekedLong。 */
=======
  /**
   */
>>>>>>> Stashed changes
  private static final int PEEKED_LONG = 15;

  private static final int PEEKED_NUMBER = 16;
  private static final int PEEKED_EOF = 17;

  /* State machine when parsing numbers */
  private static final int NUMBER_CHAR_NONE = 0;
  private static final int NUMBER_CHAR_SIGN = 1;
  private static final int NUMBER_CHAR_DIGIT = 2;
  private static final int NUMBER_CHAR_DECIMAL = 3;
  private static final int NUMBER_CHAR_FRACTION_DIGIT = 4;
  private static final int NUMBER_CHAR_EXP_E = 5;
  private static final int NUMBER_CHAR_EXP_SIGN = 6;
  private static final int NUMBER_CHAR_EXP_DIGIT = 7;

<<<<<<< Updated upstream
  /** 输入 JSON 流。 */
=======
  /**
   */
>>>>>>> Stashed changes
  private final Reader in;

  private Strictness strictness = Strictness.LEGACY_STRICT;
  // 默认嵌套限制基于
  // https://github.com/square/moshi/blob/parent-1.15.0/moshi/src/main/java/com/squareup/moshi/JsonReader.java#L228-L230
  static final int DEFAULT_NESTING_LIMIT = 255;
  private int nestingLimit = DEFAULT_NESTING_LIMIT;

  static final int BUFFER_SIZE = 1024;

  /**
   * 使用手动缓冲区可以轻松读取和取消读取即将到来的字符，这样我们就可以在没有中间 StringBuilder 的情况下创建字符串。我们直接从该缓冲区中解码文字，因此它必须至少与可以
   * 报告为数字的最长标记一样长。
   */
  private final char[] buffer = new char[BUFFER_SIZE];

  private int pos = 0;
  private int limit = 0;

  private int lineNumber = 0;
  private int lineStart = 0;

  int peeked = PEEKED_NONE;

  /**
   * 完全由带有可选前导破折号的数字组成的已查看值。正值不能有前导 0。
   */
  private long peekedLong;

  /**
   * The number of characters in a peeked number literal. Increment 'pos' by this after
   * reading a number.
   */
  private int peekedNumberLength;

  /**
   * 应在下一个 double、long 或 string 上解析的已查看字符串。这是在解析数值之前填充的，如果解析失败则使用该值。
   */
  private String peekedString;

<<<<<<< Updated upstream
  /** 嵌套栈；用手动数组而非 ArrayList 可节省约 20% 开销。 */
=======
  /**
   */
>>>>>>> Stashed changes
  private int[] stack = new int[32];

  private int stackSize = 0;

  {
    stack[stackSize++] = JsonScope.EMPTY_DOCUMENT;
  }

  /*
   * The path members. It corresponds directly to stack: At indices where the
   * stack contains an object (EMPTY_OBJECT, DANGLING_NAME or NONEMPTY_OBJECT),
   * pathNames contains the name at this scope. Where it contains an array
   * (EMPTY_ARRAY, NONEMPTY_ARRAY) pathIndices contains the current index in
   * that array. Otherwise the value is undefined, and we take advantage of that
   * by incrementing pathIndices when doing so isn't useful.
   */
  private String[] pathNames = new String[32];
  private int[] pathIndices = new int[32];

<<<<<<< Updated upstream
  /** 创建从 {@code in} 读取 JSON 的新实例。 */
=======
  /**
   */
>>>>>>> Stashed changes
  public JsonReader(Reader in) {
    this.in = Objects.requireNonNull(in, "in == null");
  }

  /**
   * 设置该读者的严格程度。
   * @deprecated 请改用 {@link #setStrictness(Strictness)}。 {@code JsonReader.setLenient(true)} 应替换为 {@code JsonReader.setStrictness(Strictness.LENIENT)}，{@code JsonReader.setLenient(false)} 应替换为 {@code JsonReader.setStrictness(Strictness.LEGACY_STRICT)}。<br> 但是，如果您以前使用过 {@code setLenient(false)}，那么您现在可能更喜欢 {@link Strictness#STRICT}。
   * @param lenient 这位读者是否应该宽容。如果为 true，则严格性设置为 {@link Strictness#LENIENT}。如果为 false，则严格性设置为 {@link Strictness#LEGACY_STRICT}。
   * @see #setStrictness(Strictness)
   */
  @Deprecated
  // 不要指定 @InlineMe，因此使用 `setLenient(false)` 的调用者会意识到新的
  // 严格性.STRICT
  @SuppressWarnings("InlineMeSuggester")
  public final void setLenient(boolean lenient) {
    setStrictness(lenient ? Strictness.LENIENT : Strictness.LEGACY_STRICT);
  }

  /**
   * 如果此读取器的 {@link Strictness} 等于 {@link Strictness#LENIENT}，则返回 true。
   * @see #getStrictness()
   */
  public final boolean isLenient() {
    return strictness == Strictness.LENIENT;
  }

  /**
   * 配置此解析器接受的内容的自由度。
   * <p>在 {@linkplain Strictness#STRICT strict} 模式下，解析器仅接受符合 <a
   * href="https://www.ietf.org/rfc/rfc8259.txt">RFC 8259</a> 的 JSON。在 {@linkplain
   * Strictness#LEGACY_STRICT legacy strict} 模式（默认）下，仅接受符合 RFC 8259 的
   * JSON，出于向后兼容性原因，下面指出了一些例外情况。在 {@linkplain Strictness#LENIENT lenient} 模式下，接受所有类型的不符合规范的
   * JSON（见下文）。
   * <dl> <dt>{@link Strictness#STRICT} <dd>在严格模式下，仅接受符合 RFC 8259 的输入。 <dt>{@link
   * Strictness#LEGACY_STRICT} <dd>在传统严格模式下，接受以下与 RFC 8259 的偏差： <ul> <li>JsonReader 允许文本
   * {@code true}、{@code false} 和 {@code null} 具有任意大小写，例如 {@code fAlSe} 或 {@code NULL}
   * <li>JsonReader 支持转义序列 {@code \'}，表示 {@code '}（单引号） <li>JsonReader 支持转义序列
   * <code>\<i>LF</i></code>（其中 {@code LF} 是 Unicode 字符） {@code U+000A})，导致读取的 JSON 字符串中出现
   * {@code LF} <li>JsonReader 允许未转义的控制字符（{@code U+0000} 到 {@code U+001F}） </ul> <dt>{@link
   * Strictness#LENIENT} <dd>在宽松模式下，除了以下偏离之外，还接受在传统严格模式下接受的所有输入来自 RFC 8259：<ul> <li> 以 <a
   * href="#nonexecuteprefix"> 非执行前缀开头的流 </a>, {@code ")]}'\n"} <li>
   * 包含多个顶级值的流。使用传统严格或严格解析时，每个流必须恰好包含一个顶级值。 <li>Numbers 可以是 {@link Double#isNaN() NaNs} 或
   * {@link Double#isInfinite() infinities}，分别由 {@code NaN} 和 {@code (-)Infinity} 表示。 {@code
   * /*} 和以 {@code *}{@code /} 结尾的注释不能嵌套在未加引号的 <li>Name 或未加引号的 <li> 字符串中，而不是由 {@code ;} 分隔。
   * {@code ,}。 <li> 不必要的数组分隔符。这些被解释为 <li> 名称和值由 {@code =} 或 {@code =>} 分隔，而不是 <li> 名称/值对，由
   * {@code ;} 分隔。 {@code ,}。</ul> </dl>
   * @param strictness 该读者的新严格值。可能不是 {@code null}。
   * @see #getStrictness()
   * @since 2.11.0
   */
  public final void setStrictness(Strictness strictness) {
    Objects.requireNonNull(strictness);
    this.strictness = strictness;
  }

  /**
   * 返回此阅读器的 {@linkplain Strictness strictness}。
   * @see #setStrictness(Strictness)
   * @since 2.11.0
   */
  public final Strictness getStrictness() {
    return strictness;
  }

  /**
   * 设置该读取器的嵌套限制。
   * <p> 的嵌套限制定义了可以同时打开多少个 JSON 数组或对象。例如，嵌套限制为 0 意味着根本不能打开任何数组或对象，嵌套限制为 1 意味着可以同时打开一个数组或对象，依此
   * 类推。因此，嵌套限制为 3 允许读取 JSON 数据 <code>[{"a":[true]}]</code>，但嵌套限制为 2 时，它将在内部 {@code [true]} 处
   * 失败。
   * <p> 当递归 {@link com.google.gson.TypeAdapter} 实现处理深度嵌套的 JSON 数据时，嵌套限制有助于防止 {@link
   * StackOverflowError}。
   * <p>默认嵌套限制为{@value #DEFAULT_NESTING_LIMIT}。
   * @throws IllegalArgumentException 如果嵌套限制为负数。
   * @since 2.12.0
   * @see #getNestingLimit()
   */
  public final void setNestingLimit(int limit) {
    if (limit < 0) {
      throw new IllegalArgumentException("Invalid nesting limit: " + limit);
    }
    this.nestingLimit = limit;
  }

  /**
   * 返回此读取器的嵌套限制。
   * @since 2.12.0
   * @see #setNestingLimit(int)
   */
  public final int getNestingLimit() {
    return nestingLimit;
  }

  /**
   * 使用 JSON 流中的下一个标记并断言它是新数组的开始。
   * @throws IllegalStateException 如果下一个标记不是数组的开头。
   */
  public void beginArray() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_BEGIN_ARRAY) {
      push(JsonScope.EMPTY_ARRAY);
      pathIndices[stackSize - 1] = 0;
      peeked = PEEKED_NONE;
    } else {
      throw unexpectedTokenError("BEGIN_ARRAY");
    }
  }

  /**
   * 使用 JSON 流中的下一个标记并断言它是当前数组的末尾。
   * @throws IllegalStateException 如果下一个标记不是数组的末尾。
   */
  public void endArray() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_END_ARRAY) {
      stackSize--;
      pathIndices[stackSize - 1]++;
      peeked = PEEKED_NONE;
    } else {
      throw unexpectedTokenError("END_ARRAY");
    }
  }

  /**
   * 使用 JSON 流中的下一个标记并断言它是新对象的开始。
   * @throws IllegalStateException 如果下一个标记不是对象的开头。
   */
  public void beginObject() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_BEGIN_OBJECT) {
      push(JsonScope.EMPTY_OBJECT);
      peeked = PEEKED_NONE;
    } else {
      throw unexpectedTokenError("BEGIN_OBJECT");
    }
  }

  /**
   * 使用 JSON 流中的下一个标记并断言它是当前对象的末尾。
   * @throws IllegalStateException 如果下一个标记不是对象的结尾。
   */
  public void endObject() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_END_OBJECT) {
      stackSize--;
      pathNames[stackSize] = null; // Free the last path name so that it can be garbage collected!
      pathIndices[stackSize - 1]++;
      peeked = PEEKED_NONE;
    } else {
      throw unexpectedTokenError("END_OBJECT");
    }
  }

<<<<<<< Updated upstream
  /** 当前数组或对象是否还有下一元素。 */
=======
  /**
   */
>>>>>>> Stashed changes
  public boolean hasNext() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    return p != PEEKED_END_OBJECT && p != PEEKED_END_ARRAY && p != PEEKED_EOF;
  }

<<<<<<< Updated upstream
  /** 返回下一令牌类型但不消费。 */
=======
  /**
   */
>>>>>>> Stashed changes
  public JsonToken peek() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    switch (p) {
      case PEEKED_BEGIN_OBJECT:
        return JsonToken.BEGIN_OBJECT;
      case PEEKED_END_OBJECT:
        return JsonToken.END_OBJECT;
      case PEEKED_BEGIN_ARRAY:
        return JsonToken.BEGIN_ARRAY;
      case PEEKED_END_ARRAY:
        return JsonToken.END_ARRAY;
      case PEEKED_SINGLE_QUOTED_NAME:
      case PEEKED_DOUBLE_QUOTED_NAME:
      case PEEKED_UNQUOTED_NAME:
        return JsonToken.NAME;
      case PEEKED_TRUE:
      case PEEKED_FALSE:
        return JsonToken.BOOLEAN;
      case PEEKED_NULL:
        return JsonToken.NULL;
      case PEEKED_SINGLE_QUOTED:
      case PEEKED_DOUBLE_QUOTED:
      case PEEKED_UNQUOTED:
      case PEEKED_BUFFERED:
        return JsonToken.STRING;
      case PEEKED_LONG:
      case PEEKED_NUMBER:
        return JsonToken.NUMBER;
      case PEEKED_EOF:
        return JsonToken.END_DOCUMENT;
      default:
        throw new AssertionError();
    }
  }

  @SuppressWarnings("fallthrough")
  int doPeek() throws IOException {
    int peekStack = stack[stackSize - 1];
    if (peekStack == JsonScope.EMPTY_ARRAY) {
      stack[stackSize - 1] = JsonScope.NONEMPTY_ARRAY;
    } else if (peekStack == JsonScope.NONEMPTY_ARRAY) {
      // 在下一个元素之前查找逗号。
      int c = nextNonWhitespace(true);
      switch (c) {
        case ']':
          peeked = PEEKED_END_ARRAY;
          return peeked;
        case ';':
          checkLenient(); // fall-through
        case ',':
          break;
        default:
          throw syntaxError("Unterminated array");
      }
    } else if (peekStack == JsonScope.EMPTY_OBJECT || peekStack == JsonScope.NONEMPTY_OBJECT) {
      stack[stackSize - 1] = JsonScope.DANGLING_NAME;
      // 在下一个元素之前查找逗号。
      if (peekStack == JsonScope.NONEMPTY_OBJECT) {
        int c = nextNonWhitespace(true);
        switch (c) {
          case '}':
            peeked = PEEKED_END_OBJECT;
            return peeked;
          case ';':
            checkLenient(); // fall-through
          case ',':
            break;
          default:
            throw syntaxError("Unterminated object");
        }
      }
      int c = nextNonWhitespace(true);
      switch (c) {
        case '"':
          peeked = PEEKED_DOUBLE_QUOTED_NAME;
          return peeked;
        case '\'':
          checkLenient();
          peeked = PEEKED_SINGLE_QUOTED_NAME;
          return peeked;
        case '}':
          if (peekStack != JsonScope.NONEMPTY_OBJECT) {
            peeked = PEEKED_END_OBJECT;
            return peeked;
          } else {
            throw syntaxError("Expected name");
          }
        default:
          checkLenient();
          pos--; // Don't consume the first character in an unquoted string.
          if (isLiteral((char) c)) {
            peeked = PEEKED_UNQUOTED_NAME;
            return peeked;
          } else {
            throw syntaxError("Expected name");
          }
      }
    } else if (peekStack == JsonScope.DANGLING_NAME) {
      stack[stackSize - 1] = JsonScope.NONEMPTY_OBJECT;
      // 在值之前查找冒号。
      int c = nextNonWhitespace(true);
      switch (c) {
        case ':':
          break;
        case '=':
          checkLenient();
          if ((pos < limit || fillBuffer(1)) && buffer[pos] == '>') {
            pos++;
          }
          break;
        default:
          throw syntaxError("Expected ':'");
      }
    } else if (peekStack == JsonScope.EMPTY_DOCUMENT) {
      if (strictness == Strictness.LENIENT) {
        consumeNonExecutePrefix();
      }
      stack[stackSize - 1] = JsonScope.NONEMPTY_DOCUMENT;
    } else if (peekStack == JsonScope.NONEMPTY_DOCUMENT) {
      int c = nextNonWhitespace(false);
      if (c == -1) {
        peeked = PEEKED_EOF;
        return peeked;
      } else {
        checkLenient();
        pos--;
      }
    } else if (peekStack == JsonScope.CLOSED) {
      throw new IllegalStateException("JsonReader is closed");
    }

    int c = nextNonWhitespace(true);
    switch (c) {
      case ']':
        if (peekStack == JsonScope.EMPTY_ARRAY) {
          peeked = PEEKED_END_ARRAY;
          return peeked;
        }
      // 处理“,]”的失败
      case ';':
      case ',':
        // 在宽松模式下，数组中的 0 长度文字表示“null”。
        if (peekStack == JsonScope.EMPTY_ARRAY || peekStack == JsonScope.NONEMPTY_ARRAY) {
          checkLenient();
          pos--;
          peeked = PEEKED_NULL;
          return peeked;
        } else {
          throw syntaxError("Unexpected value");
        }
      case '\'':
        checkLenient();
        peeked = PEEKED_SINGLE_QUOTED;
        return peeked;
      case '"':
        peeked = PEEKED_DOUBLE_QUOTED;
        return peeked;
      case '[':
        peeked = PEEKED_BEGIN_ARRAY;
        return peeked;
      case '{':
        peeked = PEEKED_BEGIN_OBJECT;
        return peeked;
      default:
        pos--; // Don't consume the first character in a literal value.
    }

    int result = peekKeyword();
    if (result != PEEKED_NONE) {
      return result;
    }

    result = peekNumber();
    if (result != PEEKED_NONE) {
      return result;
    }

    if (!isLiteral(buffer[pos])) {
      throw syntaxError("Expected value");
    }

    checkLenient();
    peeked = PEEKED_UNQUOTED;
    return peeked;
  }

  private int peekKeyword() throws IOException {
    // 通过第一个字符找出我们要匹配的关键字。
    char c = buffer[pos];
    String keyword;
    String keywordUpper;
    int peeking;

    // 查看第一个字母来确定我们要匹配的关键字。
    if (c == 't' || c == 'T') {
      keyword = "true";
      keywordUpper = "TRUE";
      peeking = PEEKED_TRUE;
    } else if (c == 'f' || c == 'F') {
      keyword = "false";
      keywordUpper = "FALSE";
      peeking = PEEKED_FALSE;
    } else if (c == 'n' || c == 'N') {
      keyword = "null";
      keywordUpper = "NULL";
      peeking = PEEKED_NULL;
    } else {
      return PEEKED_NONE;
    }

    // STRICT 模式下不允许使用大写关键字
    boolean allowsUpperCased = strictness != Strictness.STRICT;

    // 确认 chars [0..length) 与关键字匹配。
    int length = keyword.length();
    for (int i = 0; i < length; i++) {
      if (pos + i >= limit && !fillBuffer(i + 1)) {
        return PEEKED_NONE;
      }
      c = buffer[pos + i];
      boolean matched = c == keyword.charAt(i) || (allowsUpperCased && c == keywordUpper.charAt(i));
      if (!matched) {
        return PEEKED_NONE;
      }
    }

    if ((pos + length < limit || fillBuffer(length + 1)) && isLiteral(buffer[pos + length])) {
      return PEEKED_NONE; // Don't match trues, falsey or nullsoft!
    }

    // 我们发现关键字后跟 EOF 或非文字字符。
    pos += length;
    peeked = peeking;
    return peeked;
  }

  private int peekNumber() throws IOException {
    // 与 nextNonWhitespace 一样，它使用局部变量“p”和“l”来保存内循环字段访问。
    char[] buffer = this.buffer;
    int p = pos;
    int l = limit;

    long value = 0; // Negative to accommodate Long.MIN_VALUE more easily.
    boolean negative = false;
    boolean fitsInLong = true;
    int last = NUMBER_CHAR_NONE;

    int i = 0;

    charactersOfNumber:
    for (; true; i++) {
      if (p + i == l) {
        if (i == buffer.length) {
          // 虽然这看起来是一个格式良好的数字，但它太长了，无法继续阅读。放弃
          // 并让应用程序将其作为不带引号的文字处理。
          return PEEKED_NONE;
        }
        if (!fillBuffer(i + 1)) {
          break;
        }
        p = pos;
        l = limit;
      }

      char c = buffer[p + i];
      switch (c) {
        case '-':
          if (last == NUMBER_CHAR_NONE) {
            negative = true;
            last = NUMBER_CHAR_SIGN;
            continue;
          } else if (last == NUMBER_CHAR_EXP_E) {
            last = NUMBER_CHAR_EXP_SIGN;
            continue;
          }
          return PEEKED_NONE;

        case '+':
          if (last == NUMBER_CHAR_EXP_E) {
            last = NUMBER_CHAR_EXP_SIGN;
            continue;
          }
          return PEEKED_NONE;

        case 'e':
        case 'E':
          if (last == NUMBER_CHAR_DIGIT || last == NUMBER_CHAR_FRACTION_DIGIT) {
            last = NUMBER_CHAR_EXP_E;
            continue;
          }
          return PEEKED_NONE;

        case '.':
          if (last == NUMBER_CHAR_DIGIT) {
            last = NUMBER_CHAR_DECIMAL;
            continue;
          }
          return PEEKED_NONE;

        default:
          if (c < '0' || c > '9') {
            if (!isLiteral(c)) {
              break charactersOfNumber;
            }
            return PEEKED_NONE;
          }
          if (last == NUMBER_CHAR_SIGN || last == NUMBER_CHAR_NONE) {
            value = -(c - '0');
            last = NUMBER_CHAR_DIGIT;
          } else if (last == NUMBER_CHAR_DIGIT) {
            if (value == 0) {
              return PEEKED_NONE; // Leading '0' prefix is not allowed (since it could be octal).
            }
            long newValue = value * 10 - (c - '0');
            fitsInLong &=
                value > MIN_INCOMPLETE_INTEGER
                    || (value == MIN_INCOMPLETE_INTEGER && newValue < value);
            value = newValue;
          } else if (last == NUMBER_CHAR_DECIMAL) {
            last = NUMBER_CHAR_FRACTION_DIGIT;
          } else if (last == NUMBER_CHAR_EXP_E || last == NUMBER_CHAR_EXP_SIGN) {
            last = NUMBER_CHAR_EXP_DIGIT;
          }
      }
    }

    // 我们已经阅读了完整的数字。确定它是 PEEKED_LONG 还是 PEEKED_NUMBER。
    // 不要存储-0那么久；用户可能想将其读为 double -0.0
    // Don't try to convert Long.MIN_VALUE to positive long; it would overflow MAX_VALUE
    if (last == NUMBER_CHAR_DIGIT
        && fitsInLong
        && (value != Long.MIN_VALUE || negative)
        && (value != 0 || !negative)) {
      peekedLong = negative ? value : -value;
      pos += i;
      peeked = PEEKED_LONG;
      return peeked;
    } else if (last == NUMBER_CHAR_DIGIT
        || last == NUMBER_CHAR_FRACTION_DIGIT
        || last == NUMBER_CHAR_EXP_DIGIT) {
      peekedNumberLength = i;
      peeked = PEEKED_NUMBER;
      return peeked;
    } else {
      return PEEKED_NONE;
    }
  }

  @SuppressWarnings("fallthrough")
  private boolean isLiteral(char c) throws IOException {
    switch (c) {
      case '/':
      case '\\':
      case ';':
      case '#':
      case '=':
        checkLenient(); // fall-through
      case '{':
      case '}':
      case '[':
      case ']':
      case ':':
      case ',':
      case ' ':
      case '\t':
      case '\f':
      case '\r':
      case '\n':
        return false;
      default:
        return true;
    }
  }

  /**
   * 返回下一个令牌（{@link JsonToken#NAME property name}）并使用它。
   * @throws IllegalStateException 如果下一个标记不是属性名称。
   */
  public String nextName() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    String result;
    if (p == PEEKED_UNQUOTED_NAME) {
      result = nextUnquotedValue();
    } else if (p == PEEKED_SINGLE_QUOTED_NAME) {
      result = nextQuotedValue('\'');
    } else if (p == PEEKED_DOUBLE_QUOTED_NAME) {
      result = nextQuotedValue('"');
    } else {
      throw unexpectedTokenError("a name");
    }
    peeked = PEEKED_NONE;
    pathNames[stackSize - 1] = result;
    return result;
  }

  /**
   * 返回下一个令牌的 {@link JsonToken#STRING string} 值，并使用它。如果下一个标记是数字，则此方法将返回其字符串形式。
   * @throws IllegalStateException 如果下一个标记不是字符串。
   */
  public String nextString() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    String result;
    if (p == PEEKED_UNQUOTED) {
      result = nextUnquotedValue();
    } else if (p == PEEKED_SINGLE_QUOTED) {
      result = nextQuotedValue('\'');
    } else if (p == PEEKED_DOUBLE_QUOTED) {
      result = nextQuotedValue('"');
    } else if (p == PEEKED_BUFFERED) {
      result = peekedString;
      peekedString = null;
    } else if (p == PEEKED_LONG) {
      result = Long.toString(peekedLong);
    } else if (p == PEEKED_NUMBER) {
      result = new String(buffer, pos, peekedNumberLength);
      pos += peekedNumberLength;
    } else {
      throw unexpectedTokenError("a string");
    }
    peeked = PEEKED_NONE;
    pathIndices[stackSize - 1]++;
    return result;
  }

  /**
   * 返回下一个令牌的 {@link JsonToken#BOOLEAN boolean} 值，并使用它。
   * @throws IllegalStateException 如果下一个标记不是布尔值。
   */
  public boolean nextBoolean() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_TRUE) {
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
      return true;
    } else if (p == PEEKED_FALSE) {
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
      return false;
    }
    throw unexpectedTokenError("a boolean");
  }

  /**
   * 使用 JSON 流中的下一个标记并断言它是文字 null。
   * @throws IllegalStateException 如果下一个标记不是 JSON null。
   */
  public void nextNull() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_NULL) {
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
    } else {
      throw unexpectedTokenError("null");
    }
  }

  /**
   * 返回下一个令牌的 {@link JsonToken#NUMBER double} 值，并使用它。如果下一个标记是字符串，此方法将尝试使用 {@link
   * Double#parseDouble(String)} 将其解析为双精度型。
   * @throws IllegalStateException 如果下一个标记既不是数字也不是字符串。
   * @throws NumberFormatException 如果下一个文字值无法解析为双精度值。
   * @throws MalformedJsonException 如果下一个文字值为 NaN 或 Infinity 并且该读取器不是 {@link #setStrictness(Strictness) lenient}。
   */
  public double nextDouble() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    if (p == PEEKED_LONG) {
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
      return (double) peekedLong;
    }

    if (p == PEEKED_NUMBER) {
      peekedString = new String(buffer, pos, peekedNumberLength);
      pos += peekedNumberLength;
    } else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_DOUBLE_QUOTED) {
      peekedString = nextQuotedValue(p == PEEKED_SINGLE_QUOTED ? '\'' : '"');
    } else if (p == PEEKED_UNQUOTED) {
      peekedString = nextUnquotedValue();
    } else if (p != PEEKED_BUFFERED) {
      throw unexpectedTokenError("a double");
    }

    peeked = PEEKED_BUFFERED;
    double result = Double.parseDouble(peekedString); // don't catch this NumberFormatException.
    if (strictness != Strictness.LENIENT && (Double.isNaN(result) || Double.isInfinite(result))) {
      throw syntaxError("JSON forbids NaN and infinities: " + result);
    }
    peekedString = null;
    peeked = PEEKED_NONE;
    pathIndices[stackSize - 1]++;
    return result;
  }

  /**
   * 返回下一个令牌的 {@link JsonToken#NUMBER long} 值，并使用它。如果下一个标记是字符串，此方法将尝试将其解析为长整型。如果下一个标记的数值不能由 J
   * ava {@code long} 精确表示，则此方法将抛出异常。
   * @throws IllegalStateException 如果下一个标记既不是数字也不是字符串。
   * @throws NumberFormatException 如果下一个文字值无法解析为数字或精确表示为 long。
   */
  public long nextLong() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    if (p == PEEKED_LONG) {
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
      return peekedLong;
    }

    if (p == PEEKED_NUMBER) {
      peekedString = new String(buffer, pos, peekedNumberLength);
      pos += peekedNumberLength;
    } else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_DOUBLE_QUOTED || p == PEEKED_UNQUOTED) {
      if (p == PEEKED_UNQUOTED) {
        peekedString = nextUnquotedValue();
      } else {
        peekedString = nextQuotedValue(p == PEEKED_SINGLE_QUOTED ? '\'' : '"');
      }
      validateAscii(peekedString);
      try {
        long result = Long.parseLong(peekedString);
        peeked = PEEKED_NONE;
        pathIndices[stackSize - 1]++;
        return result;
      } catch (NumberFormatException ignored) {
        // 回退到下面解析为双精度。
      }
    } else {
      throw unexpectedTokenError("a long");
    }

    peeked = PEEKED_BUFFERED;
    double asDouble = Double.parseDouble(peekedString); // don't catch this NumberFormatException.
    long result = (long) asDouble;
    if (result != asDouble) { // Make sure no precision was lost casting to 'long'.
      throw new NumberFormatException("Expected a long but was " + peekedString + locationString());
    }
    peekedString = null;
    peeked = PEEKED_NONE;
    pathIndices[stackSize - 1]++;
    return result;
  }

  /**
   * 返回直到但不包括 {@code quote} 的字符串，对沿途遇到的任何字符转义序列进行转义。开头引文应该已经读过。这会消耗结束引号，但不会将其包含在返回的字符串中。
   * @param quote ' 或 "。
   */
  private String nextQuotedValue(char quote) throws IOException {
    // 与 nextNonWhitespace 一样，它使用局部变量“p”和“l”来保存内循环字段访问。
    char[] buffer = this.buffer;
    StringBuilder builder = null;
    while (true) {
      int p = pos;
      int l = limit;
      /* the index of the first character not yet appended to the builder. */
      int start = p;
      while (p < l) {
        int c = buffer[p++];

        // In strict mode, throw an exception when meeting unescaped control characters (U+0000
        // 通过 U+001F)
        if (strictness == Strictness.STRICT && c < 0x20) {
          throw syntaxError(
              "Unescaped control characters (\\u0000-\\u001F) are not allowed in strict mode");
        } else if (c == quote) {
          pos = p;
          int len = p - start - 1;
          if (builder == null) {
            return new String(buffer, start, len);
          } else {
            builder.append(buffer, start, len);
            return builder.toString();
          }
        } else if (c == '\\') {
          pos = p;
          int len = p - start - 1;
          if (builder == null) {
            int estimatedLength = (len + 1) * 2;
            builder = new StringBuilder(Math.max(estimatedLength, 16));
          }
          builder.append(buffer, start, len);
          builder.append(readEscapeCharacter());
          p = pos;
          l = limit;
          start = p;
        } else if (c == '\n') {
          lineNumber++;
          lineStart = p;
        }
      }

      if (builder == null) {
        int estimatedLength = (p - start) * 2;
        builder = new StringBuilder(Math.max(estimatedLength, 16));
      }
      builder.append(buffer, start, p - start);
      pos = p;
      if (!fillBuffer(1)) {
        throw syntaxError("Unterminated string");
      }
    }
  }

<<<<<<< Updated upstream
  /** 将未加引号的值作为字符串返回。 */
=======
  /**
   */
>>>>>>> Stashed changes
  @SuppressWarnings("fallthrough")
  private String nextUnquotedValue() throws IOException {
    StringBuilder builder = null;
    int i = 0;

    findNonLiteralCharacter:
    while (true) {
      for (; pos + i < limit; i++) {
        switch (buffer[pos + i]) {
          case '/':
          case '\\':
          case ';':
          case '#':
          case '=':
            checkLenient(); // fall-through
          case '{':
          case '}':
          case '[':
          case ']':
          case ':':
          case ',':
          case ' ':
          case '\t':
          case '\f':
          case '\r':
          case '\n':
            break findNonLiteralCharacter;
          default:
            // 跳过要包含在字符串值中的字符
        }
      }

      // 尝试一次将整个文字加载到缓冲区中。
      if (i < buffer.length) {
        if (fillBuffer(i + 1)) {
          continue;
        } else {
          break;
        }
      }

      // use a StringBuilder when the value is too long. This is too long to be a number!
      if (builder == null) {
        builder = new StringBuilder(Math.max(i, 16));
      }
      builder.append(buffer, pos, i);
      pos += i;
      i = 0;
      if (!fillBuffer(1)) {
        break;
      }
    }

    String result =
        (builder == null) ? new String(buffer, pos, i) : builder.append(buffer, pos, i).toString();
    pos += i;
    return result;
  }

  private void skipQuotedValue(char quote) throws IOException {
    // 与 nextNonWhitespace 一样，它使用局部变量“p”和“l”来保存内循环字段访问。
    char[] buffer = this.buffer;
    do {
      int p = pos;
      int l = limit;
      /* the index of the first character not yet appended to the builder. */
      while (p < l) {
        int c = buffer[p++];
        if (c == quote) {
          pos = p;
          return;
        } else if (c == '\\') {
          pos = p;
          char unused = readEscapeCharacter();
          p = pos;
          l = limit;
        } else if (c == '\n') {
          lineNumber++;
          lineStart = p;
        }
      }
      pos = p;
    } while (fillBuffer(1));
    throw syntaxError("Unterminated string");
  }

  @SuppressWarnings("fallthrough")
  private void skipUnquotedValue() throws IOException {
    do {
      int i = 0;
      for (; pos + i < limit; i++) {
        switch (buffer[pos + i]) {
          case '/':
          case '\\':
          case ';':
          case '#':
          case '=':
            checkLenient(); // fall-through
          case '{':
          case '}':
          case '[':
          case ']':
          case ':':
          case ',':
          case ' ':
          case '\t':
          case '\f':
          case '\r':
          case '\n':
            pos += i;
            return;
          default:
            // 跳过该字符
        }
      }
      pos += i;
    } while (fillBuffer(1));
  }

  /**
   * 返回下一个令牌的 {@link JsonToken#NUMBER int} 值，并使用它。如果下一个标记是字符串，此方法将尝试将其解析为 int。如果下一个标记的数值不能由 J
   * ava {@code int} 精确表示，则此方法将抛出异常。
   * @throws IllegalStateException 如果下一个标记既不是数字也不是字符串。
   * @throws NumberFormatException 如果下一个文字值无法解析为数字或精确表示为 int。
   */
  public int nextInt() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    int result;
    if (p == PEEKED_LONG) {
      result = (int) peekedLong;
      if (peekedLong != result) { // Make sure no precision was lost casting to 'int'.
        throw new NumberFormatException("Expected an int but was " + peekedLong + locationString());
      }
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
      return result;
    }

    if (p == PEEKED_NUMBER) {
      peekedString = new String(buffer, pos, peekedNumberLength);
      pos += peekedNumberLength;
    } else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_DOUBLE_QUOTED || p == PEEKED_UNQUOTED) {
      if (p == PEEKED_UNQUOTED) {
        peekedString = nextUnquotedValue();
      } else {
        peekedString = nextQuotedValue(p == PEEKED_SINGLE_QUOTED ? '\'' : '"');
      }
      validateAscii(peekedString);
      try {
        result = Integer.parseInt(peekedString);
        peeked = PEEKED_NONE;
        pathIndices[stackSize - 1]++;
        return result;
      } catch (NumberFormatException ignored) {
        // 回退到下面解析为双精度。
      }
    } else {
      throw unexpectedTokenError("an int");
    }

    peeked = PEEKED_BUFFERED;
    double asDouble = Double.parseDouble(peekedString); // don't catch this NumberFormatException.
    result = (int) asDouble;
    if (result != asDouble) { // Make sure no precision was lost casting to 'int'.
      throw new NumberFormatException("Expected an int but was " + peekedString + locationString());
    }
    peekedString = null;
    peeked = PEEKED_NONE;
    pathIndices[stackSize - 1]++;
    return result;
  }

  /**
   * 关闭此 JSON 读取器和底层 {@link Reader}。
   * <p> 在大多数情况下，关闭 JSON 读取器后使用它会抛出 {@link IllegalStateException}。
   */
  @Override
  public void close() throws IOException {
    peeked = PEEKED_NONE;
    stack[0] = JsonScope.CLOSED;
    stackSize = 1;
    in.close();
  }

  /**
   * 递归地跳过下一个值。此方法适用于当 JSON 令牌流包含无法识别或未处理的值时。
   * <p> 的行为取决于下一个 JSON 令牌的类型：
   * JSON 数组或对象的 <ul> <li>Start：跳过它及其所有嵌套值。 <li>Primitive 值（例如 JSON 数字）：跳过原始值。 <li>属性名称：仅跳过属性
   * 的名称，但不跳过属性的值。还必须再次调用 {@code skipValue()} 才能跳过属性值。 JSON 数组或对象的 <li>End：仅跳过此结束标记。 JSON 文档的
   *  <li>End：跳过无效，下一个标记仍然是文档的结尾。 OCAJAVA7文档
   */
  public void skipValue() throws IOException {
    int count = 0;
    do {
      int p = peeked;
      if (p == PEEKED_NONE) {
        p = doPeek();
      }

      switch (p) {
        case PEEKED_BEGIN_ARRAY:
          push(JsonScope.EMPTY_ARRAY);
          count++;
          break;
        case PEEKED_BEGIN_OBJECT:
          push(JsonScope.EMPTY_OBJECT);
          count++;
          break;
        case PEEKED_END_ARRAY:
          stackSize--;
          count--;
          break;
        case PEEKED_END_OBJECT:
          // 仅当显式跳过对象末尾时才更新，否则堆栈不更新
          // 无论如何
          if (count == 0) {
            // 释放最后一个路径名，以便可以对其进行垃圾收集
            pathNames[stackSize - 1] = null;
          }
          stackSize--;
          count--;
          break;
        case PEEKED_UNQUOTED:
          skipUnquotedValue();
          break;
        case PEEKED_SINGLE_QUOTED:
          skipQuotedValue('\'');
          break;
        case PEEKED_DOUBLE_QUOTED:
          skipQuotedValue('"');
          break;
        case PEEKED_UNQUOTED_NAME:
          skipUnquotedValue();
          // 仅当显式跳过名称时才更新，否则堆栈不会更新
          if (count == 0) {
            pathNames[stackSize - 1] = "<skipped>";
          }
          break;
        case PEEKED_SINGLE_QUOTED_NAME:
          skipQuotedValue('\'');
          // 仅当显式跳过名称时才更新，否则堆栈不会更新
          if (count == 0) {
            pathNames[stackSize - 1] = "<skipped>";
          }
          break;
        case PEEKED_DOUBLE_QUOTED_NAME:
          skipQuotedValue('"');
          // 仅当显式跳过名称时才更新，否则堆栈不会更新
          if (count == 0) {
            pathNames[stackSize - 1] = "<skipped>";
          }
          break;
        case PEEKED_NUMBER:
          pos += peekedNumberLength;
          break;
        case PEEKED_EOF:
          // 什么都不做
          return;
        default:
          // 对于所有其他代币，无需执行任何操作；令牌已被消耗
          // 底层读者
      }
      peeked = PEEKED_NONE;
    } while (count > 0);

    pathIndices[stackSize - 1]++;
  }

  private void push(int newTop) throws MalformedJsonException {
    // - 1 因为堆栈包含 EMPTY_DOCUMENT 或 NONEMPTY_DOCUMENT 作为第一个元素
    if (stackSize - 1 >= nestingLimit) {
      throw new MalformedJsonException(
          "Nesting limit " + nestingLimit + " reached" + locationString());
    }

    if (stackSize == stack.length) {
      int newLength = stackSize * 2;
      stack = Arrays.copyOf(stack, newLength);
      pathIndices = Arrays.copyOf(pathIndices, newLength);
      pathNames = Arrays.copyOf(pathNames, newLength);
    }
    stack[stackSize++] = newTop;
  }

  /**
   * {@code limit - pos >= minimum} 一次返回 true。如果在可用字符数之前数据已耗尽，则返回 false。
   */
  private boolean fillBuffer(int minimum) throws IOException {
    char[] buffer = this.buffer;
    lineStart -= pos;
    if (limit != pos) {
      limit -= pos;
      System.arraycopy(buffer, pos, buffer, 0, limit);
    } else {
      limit = 0;
    }

    pos = 0;
    int total;
    while ((total = in.read(buffer, limit, buffer.length - limit)) != -1) {
      limit += total;

      // 如果这是第一次读取，则使用可选的字节顺序标记 (BOM)（如果存在）
      if (lineNumber == 0 && lineStart == 0 && limit > 0 && buffer[0] == '\ufeff') {
        pos++;
        lineStart++;
        minimum++;
      }

      if (limit >= minimum) {
        return true;
      }
    }
    return false;
  }

  /**
   * 返回流中既不是空格也不是注释一部分的下一个字符。当它返回时，返回的字符总是在{@code buffer[pos-1]}；这意味着调用者始终可以通过递减 {@code pos} 
   * 来推回返回的字符。
   */
  private int nextNonWhitespace(boolean throwOnEof) throws IOException {
    /*
     * This code uses ugly local variables 'p' and 'l' representing the 'pos'
     * and 'limit' fields respectively. Using locals rather than fields saves
     * a few field reads for each whitespace character in a pretty-printed
     * document, resulting in a 5% speedup. We need to flush 'p' to its field
     * before any (potentially indirect) call to fillBuffer() and reread both
     * 'p' and 'l' after any (potentially indirect) call to the same method.
     */
    char[] buffer = this.buffer;
    int p = pos;
    int l = limit;
    while (true) {
      if (p == l) {
        pos = p;
        if (!fillBuffer(1)) {
          break;
        }
        p = pos;
        l = limit;
      }

      int c = buffer[p++];
      if (c == '\n') {
        lineNumber++;
        lineStart = p;
        continue;
      } else if (c == ' ' || c == '\r' || c == '\t') {
        continue;
      }

      if (c == '/') {
        pos = p;
        if (p == l) {
          pos--; // push back '/' so it's still in the buffer when this method returns
          boolean charsLoaded = fillBuffer(2);
          pos++; // consume the '/' again
          if (!charsLoaded) {
            return c;
          }
        }

        checkLenient();
        char peek = buffer[pos];
        switch (peek) {
          case '*':
            // 跳过 /* c 风格注释 */
            pos++;
            if (!skipTo("*/")) {
              throw syntaxError("Unterminated comment");
            }
            p = pos + 2;
            l = limit;
            continue;

          case '/':
            // 跳过 // 行尾注释
            pos++;
            skipToEndOfLine();
            p = pos;
            l = limit;
            continue;

          default:
            return c;
        }
      } else if (c == '#') {
        pos = p;
        /*
         * Skip a # hash end-of-line comment. The JSON RFC doesn't
         * specify this behaviour, but it's required to parse
         * existing documents. See http://b/2571423.
         */
        checkLenient();
        skipToEndOfLine();
        p = pos;
        l = limit;
      } else {
        pos = p;
        return c;
      }
    }
    if (throwOnEof) {
      throw new EOFException("End of input" + locationString());
    } else {
      return -1;
    }
  }

  private void checkLenient() throws MalformedJsonException {
    if (strictness != Strictness.LENIENT) {
      throw syntaxError(
          "Use JsonReader.setStrictness(Strictness.LENIENT) to accept malformed JSON");
    }
  }

  /**
   * 将位置前进到下一个换行符之后。如果该行以“\r\n”终止，则调用者必须将“\n”用作空格。
   */
  private void skipToEndOfLine() throws IOException {
    while (pos < limit || fillBuffer(1)) {
      char c = buffer[pos++];
      if (c == '\n') {
        lineNumber++;
        lineStart = pos;
        break;
      } else if (c == '\r') {
        break;
      }
    }
  }

  /**
   * @param toFind 要搜索的字符串。不得包含换行符。
   */
  private boolean skipTo(String toFind) throws IOException {
    int length = toFind.length();
    outer:
    for (; pos + length <= limit || fillBuffer(length); pos++) {
      if (buffer[pos] == '\n') {
        lineNumber++;
        lineStart = pos + 1;
        continue;
      }
      for (int c = 0; c < length; c++) {
        if (buffer[pos + c] != toFind.charAt(c)) {
          continue outer;
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + locationString();
  }

  String locationString() {
    int line = lineNumber + 1;
    int column = pos - lineStart + 1;
    return " at line " + line + " column " + column + " path " + getPath();
  }

  private String getPath(boolean usePreviousPath) {
    StringBuilder result = new StringBuilder().append('$');
    for (int i = 0; i < stackSize; i++) {
      int scope = stack[i];
      switch (scope) {
        case JsonScope.EMPTY_ARRAY:
        case JsonScope.NONEMPTY_ARRAY:
          int pathIndex = pathIndices[i];
          // 如果索引是最后一个路径元素，则它指向下一个数组元素；必须减少
          if (usePreviousPath && pathIndex > 0 && i == stackSize - 1) {
            pathIndex--;
          }
          result.append('[').append(pathIndex).append(']');
          break;
        case JsonScope.EMPTY_OBJECT:
        case JsonScope.DANGLING_NAME:
        case JsonScope.NONEMPTY_OBJECT:
          result.append('.');
          if (pathNames[i] != null) {
            result.append(pathNames[i]);
          }
          break;
        case JsonScope.NONEMPTY_DOCUMENT:
        case JsonScope.EMPTY_DOCUMENT:
        case JsonScope.CLOSED:
          break;
        default:
          throw new AssertionError("Unknown scope value: " + scope);
      }
    }
    return result.toString();
  }

  /**
   * 将 <i>dot-notation</i> 中的 <a href="https://goessner.net/articles/JsonPath/">JSONPath</a>
   * 返回到 JSON 文档中的下一个（或当前）位置。这意味着：
   * <ul> <li> 对于 JSON 数组，路径指向下一个元素的索引（即使没有其他元素）。 <li>对于 JSON 对象，路径指向最后一个属性，或者指向当前属性（如果其名称已被使
   * 用）。 </ul>
   * <p> 此方法可用于在使用 </i> 值之前向异常消息 <i> 添加附加上下文，例如当 {@linkplain #peek() peeked} 令牌意外时。
   */
  public String getPath() {
    return getPath(false);
  }

  /**
   * 将 <i>dot-notation</i> 中的 <a href="https://goessner.net/articles/JsonPath/">JSONPath</a>
   * 返回到 JSON 文档中的上一个（或当前）位置。这意味着：
   * <ul> <li> 对于 JSON 数组，路径指向前一个元素的索引。<br> 如果尚未使用任何元素，则它使用索引 0（即使没有元素）。 <li>对于 JSON 对象，路径指向最
   * 后一个属性，或者指向当前属性（如果其名称已被使用）。 </ul>
   * <p> 此方法可用于在使用 </i> 值后向异常消息 <i> 添加附加上下文。
   */
  public String getPreviousPath() {
    return getPath(true);
  }

  /**
   * 对紧跟在反斜杠后面的一个或多个字符所标识的字符进行取消转义。反斜杠“\”应该已经被读取。这支持 Unicode 转义符“u000A”和双字符转义符“\n”。
   * @throws MalformedJsonException 如果转义序列格式错误
   */
  @SuppressWarnings("fallthrough")
  private char readEscapeCharacter() throws IOException {
    if (pos == limit && !fillBuffer(1)) {
      throw syntaxError("Unterminated escape sequence");
    }

    char escaped = buffer[pos++];
    switch (escaped) {
      case 'u':
        if (pos + 4 > limit && !fillBuffer(4)) {
          throw syntaxError("Unterminated escape sequence");
        }
        // 等价于 Integer.parseInt(stringPool.get(buffer, pos, 4), 16);
        int result = 0;
        for (int i = pos, end = i + 4; i < end; i++) {
          char c = buffer[i];
          result <<= 4;
          if (c >= '0' && c <= '9') {
            result += (c - '0');
          } else if (c >= 'a' && c <= 'f') {
            result += (c - 'a' + 10);
          } else if (c >= 'A' && c <= 'F') {
            result += (c - 'A' + 10);
          } else {
            throw syntaxError("Malformed Unicode escape \\u" + new String(buffer, pos, 4));
          }
        }
        pos += 4;
        return (char) result;

      case 't':
        return '\t';

      case 'b':
        return '\b';

      case 'n':
        return '\n';

      case 'r':
        return '\r';

      case 'f':
        return '\f';

      case '\n':
        if (strictness == Strictness.STRICT) {
          throw syntaxError("Cannot escape a newline character in strict mode");
        }
        lineNumber++;
        lineStart = pos;
      // 跌倒

      case '\'':
        if (strictness == Strictness.STRICT) {
          throw syntaxError("Invalid escaped character \"'\" in strict mode");
        }
      case '"':
      case '\\':
      case '/':
        return escaped;
      default:
        // 当以上情况都不匹配时抛出错误
        throw syntaxError("Invalid escape sequence");
    }
  }

  /**
   * 抛出一个新的 {@link MalformedJsonException}，其中包含给定的消息和有关当前位置的信息。
   */
  private MalformedJsonException syntaxError(String message) throws MalformedJsonException {
    throw new MalformedJsonException(
        message + locationString() + "\nSee " + TroubleshootingGuide.createUrl("malformed-json"));
  }

  private IllegalStateException unexpectedTokenError(String expected) throws IOException {
    JsonToken peeked = peek();
    String troubleshootingId =
        peeked == JsonToken.NULL ? "adapter-not-null-safe" : "unexpected-json-structure";
    return new IllegalStateException(
        "Expected "
            + expected
            + " but was "
            + peek()
            + locationString()
            + "\nSee "
            + TroubleshootingGuide.createUrl(troubleshootingId));
  }

<<<<<<< Updated upstream
  /** 若存在 non-execute 前缀则消费之。 */
=======
  /**
   */
>>>>>>> Stashed changes
  private void consumeNonExecutePrefix() throws IOException {
    // 通过前导空白快进
    int unused = nextNonWhitespace(true);
    pos--;

    if (pos + 5 > limit && !fillBuffer(5)) {
      return;
    }

    int p = pos;
    char[] buf = buffer;
    if (buf[p] != ')'
        || buf[p + 1] != ']'
        || buf[p + 2] != '}'
        || buf[p + 3] != '\''
        || buf[p + 4] != '\n') {
      return; // not a security token!
    }

    // 我们消耗了一个安全令牌！
    pos += 5;
  }

  private void validateAscii(String s) throws MalformedJsonException {
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) > 127) {
        throw syntaxError("String contains non-ASCII characters: " + s);
      }
    }
  }

  static {
    JsonReaderInternalAccess.INSTANCE =
        new JsonReaderInternalAccess() {
          @Override
          public void promoteNameToValue(JsonReader reader) throws IOException {
            if (reader instanceof JsonTreeReader) {
              ((JsonTreeReader) reader).promoteNameToValue();
              return;
            }
            int p = reader.peeked;
            if (p == PEEKED_NONE) {
              p = reader.doPeek();
            }
            if (p == PEEKED_DOUBLE_QUOTED_NAME) {
              reader.peeked = PEEKED_DOUBLE_QUOTED;
            } else if (p == PEEKED_SINGLE_QUOTED_NAME) {
              reader.peeked = PEEKED_SINGLE_QUOTED;
            } else if (p == PEEKED_UNQUOTED_NAME) {
              reader.peeked = PEEKED_UNQUOTED;
            } else {
              throw reader.unexpectedTokenError("a name");
            }
          }
        };
  }
}
