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

import static com.google.gson.stream.JsonScope.DANGLING_NAME;
import static com.google.gson.stream.JsonScope.EMPTY_ARRAY;
import static com.google.gson.stream.JsonScope.EMPTY_DOCUMENT;
import static com.google.gson.stream.JsonScope.EMPTY_OBJECT;
import static com.google.gson.stream.JsonScope.NONEMPTY_ARRAY;
import static com.google.gson.stream.JsonScope.NONEMPTY_DOCUMENT;
import static com.google.gson.stream.JsonScope.NONEMPTY_OBJECT;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.FormattingStyle;
import com.google.gson.Strictness;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * 将 JSON (<a href="https://www.ietf.org/rfc/rfc8259.txt">RFC 8259</a>) 编码值写入流，一次一个令牌。该流包括文
 * 字值（字符串、数字、布尔值和空值）以及对象和数组的开始和结束分隔符。
 * <h2>编码 JSON</h2>
 * 要将数据编码为 JSON，请创建一个新的 {@code JsonWriter}。当您遍历结构体的内容、根据需要嵌套数组和对象时，调用编写器上的方法：
 * <ul> <li>要写入<strong>数组</strong>，首先调用{@link #beginArray()}。使用适当的 {@link #value}
 * 方法或通过嵌套其他数组和对象来写入数组的每个元素。最后使用 {@link #endArray()} 关闭数组。
 * <li>要写入<strong>对象</strong>，首先调用{@link #beginObject()}。通过交替调用 {@link #name}
 * 和属性值来写入对象的每个属性。使用适当的 {@link #value} 方法或通过嵌套其他对象或数组来写入属性值。最后使用 {@link #endObject()}
 * 关闭该对象。 </ul>
 * <h2>配置</h2>
 * 可以使用以下方法自定义该编写器的行为：
 * <ul> <li>{@link #setFormattingStyle(FormattingStyle)}，默认为 {@link
 * FormattingStyle#COMPACT} <li>{@link #setHtmlSafe(boolean)}，默认情况下 JSON 输出中 HTML 字符不会转义
 * <li>{@link #setStrictness(Strictness)}，默认为 {@link Strictness#LEGACY_STRICT} <li>{@link
 * #setSerializeNulls(boolean)}，默认情况{@code null} 序列化为 </ul>
 * {@link com.google.gson.Gson} 类内部使用的 {@code JsonWriter} 实例的默认配置有所不同，并且可以使用各种 {@link
 * com.google.gson.GsonBuilder} 方法进行调整。
 * <h2>示例</h2>
 * 假设我们想要对如下消息流进行编码：
 * <pre>{@code [ { "id": 912345678901, "text": "如何在 Java 中传输 JSON？", "geo": null, "user":
 * { "name": "json_newb", "followers_count": 41 } }, { "id": 912345678902, "text":
 * "@json_newb 只需使用JsonWriter!", "geo": [50.454722, -104.606667], "user": { "name":
 * "jesse", "followers_count": 2 } } ] }</pre>
 * 这段代码对上面的结构进行了编码：
 * <pre>{@code public void writeJsonStream(OutputStream out, List<Message> messages) 抛出
 * IOException { JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
 * writer.setIndent(" "); writeMessagesArray（作者，消息）； writer.close(); }
 * public void writeMessagesArray(JsonWriter writer, List<Message> messages) 抛出
 * IOException { writer.beginArray(); for (消息消息:消息) { writeMessage(作者, 消息); }
 * writer.endArray(); }
 * public void writeMessage(JsonWriter writer, Message message) 抛出 IOException {
 * writer.beginObject(); writer.name("id").value(message.getId());
 * writer.name("text").value(message.getText()); if (message.getGeo() != null) {
 * writer.name("geo"); writeDoublesArray(writer, message.getGeo()); } else {
 * writer.name("geo").nullValue(); } writer.name("用户"); writeUser(writer,
 * message.getUser()); writer.endObject(); }
 * public void writeUser(JsonWriter writer, User user) 抛出 IOException {
 * writer.beginObject(); writer.name("name").value(user.getName());
 * writer.name("followers_count").value(user.getFollowersCount()); writer.endObject(); }
 * public void writeDoublesArray(JsonWriter writer, List<Double> doubles) 抛出 IOException {
 * writer.beginArray(); for (双精度值: 双精度) { writer.value(value); } writer.endArray(); }
 * }</pre>
 * <p>Each {@code JsonWriter} 可用于写入单个 JSON 流。此类的实例不是线程安全的。导致格式错误的 JSON 字符串的调用将会失败并显示
 * {@link IllegalStateException}。
 * @author Jesse Wilson
 * @since 1.6
 */
public class JsonWriter implements Closeable, Flushable {

  // Syntax as defined by https://datatracker.ietf.org/doc/html/rfc8259#section-6
  private static final Pattern VALID_JSON_NUMBER_PATTERN =
      Pattern.compile("-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][-+]?[0-9]+)?");

  /*
   * From RFC 8259, "All Unicode characters may be placed within the
   * quotation marks except for the characters that must be escaped:
   * quotation mark, reverse solidus, and the control characters
   * (U+0000 through U+001F)."
   *
   * We also escape '\u2028' and '\u2029', which JavaScript interprets as
   * newline characters. This prevents eval() from failing with a syntax
   * error. http://code.google.com/p/google-gson/issues/detail?id=341
   */
  private static final String[] REPLACEMENT_CHARS;
  private static final String[] HTML_SAFE_REPLACEMENT_CHARS;

  static {
    REPLACEMENT_CHARS = new String[128];
    for (int i = 0; i <= 0x1f; i++) {
      REPLACEMENT_CHARS[i] = String.format("\\u%04x", i);
    }
    REPLACEMENT_CHARS['"'] = "\\\"";
    REPLACEMENT_CHARS['\\'] = "\\\\";
    REPLACEMENT_CHARS['\t'] = "\\t";
    REPLACEMENT_CHARS['\b'] = "\\b";
    REPLACEMENT_CHARS['\n'] = "\\n";
    REPLACEMENT_CHARS['\r'] = "\\r";
    REPLACEMENT_CHARS['\f'] = "\\f";
    HTML_SAFE_REPLACEMENT_CHARS = REPLACEMENT_CHARS.clone();
    HTML_SAFE_REPLACEMENT_CHARS['<'] = "\\u003c";
    HTML_SAFE_REPLACEMENT_CHARS['>'] = "\\u003e";
    HTML_SAFE_REPLACEMENT_CHARS['&'] = "\\u0026";
    HTML_SAFE_REPLACEMENT_CHARS['='] = "\\u003d";
    HTML_SAFE_REPLACEMENT_CHARS['\''] = "\\u0027";
  }

  /** JSON 输出目标。 */
  private final Writer out;

  private int[] stack = new int[32];
  private int stackSize = 0;

  {
    push(EMPTY_DOCUMENT);
  }

  private FormattingStyle formattingStyle;
  // 这些字段缓存从格式化样式派生的数据，以避免必须
  // 每次写东西时重新评估它
  private String formattedColon;
  private String formattedComma;
  private boolean usesEmptyNewlineAndIndent;

  private Strictness strictness = Strictness.LEGACY_STRICT;

  private boolean htmlSafe;

  private String deferredName;

  private boolean serializeNulls = true;

  /**
   * 创建一个新实例，将 JSON 编码流写入 {@code out}。为了获得最佳性能，请确保 {@link Writer} 已缓冲；如有必要，用 {@link
   * java.io.BufferedWriter BufferedWriter} 包装。
   */
  public JsonWriter(Writer out) {
    this.out = Objects.requireNonNull(out, "out == null");
    setFormattingStyle(FormattingStyle.COMPACT);
  }

  /**
   * 设置要为编码文档中的每个缩进级别重复的缩进字符串。如果 {@code indent.isEmpty()} 编码的文档将是紧凑的。否则编码后的文档将更易于人类阅读。
   * <p>这是一种方便的方法，如果给定的缩进字符串为空，则使用 {@link FormattingStyle#COMPACT} 覆盖任何先前的 {@linkplain #setFo
   * rmattingStyle(FormattingStyle) set formatting style}；如果给定的缩进字符串不为空，则使用 {@link Formatting
   * Style#PRETTY} 覆盖任何先前的 {@linkplain #setFormattingStyle(FormattingStyle) set formatting st
   * yle}。
   * @param indent 仅包含空格的字符串。
   */
  public final void setIndent(String indent) {
    if (indent.isEmpty()) {
      setFormattingStyle(FormattingStyle.COMPACT);
    } else {
      setFormattingStyle(FormattingStyle.PRETTY.withIndent(indent));
    }
  }

  /**
   * 设置要在编码文档中使用的格式样式。
   * <p>格式样式指定例如每个缩进级别重复的缩进字符串或换行符样式，以适应各种操作系统样式。
   * @param formattingStyle 要使用的格式样式不能是 {@code null}。
   * @see #getFormattingStyle()
   * @since 2.11.0
   */
  public final void setFormattingStyle(FormattingStyle formattingStyle) {
    this.formattingStyle = Objects.requireNonNull(formattingStyle);

    this.formattedComma = ",";
    if (this.formattingStyle.usesSpaceAfterSeparators()) {
      this.formattedColon = ": ";

      // 仅在未写入换行符时添加空格
      if (this.formattingStyle.getNewline().isEmpty()) {
        this.formattedComma = ", ";
      }
    } else {
      this.formattedColon = ":";
    }

    this.usesEmptyNewlineAndIndent =
        this.formattingStyle.getNewline().isEmpty() && this.formattingStyle.getIndent().isEmpty();
  }

  /**
   * 返回作者使用的漂亮的打印样式。
   * @return 将使用的{@code FormattingStyle}。
   * @see #setFormattingStyle(FormattingStyle)
   * @since 2.11.0
   */
  public final FormattingStyle getFormattingStyle() {
    return formattingStyle;
  }

  /**
   * 设定了这位作家的严格性。
   * @deprecated 请改用 {@link #setStrictness(Strictness)}。 {@code JsonWriter.setLenient(true)} 应替换为 {@code JsonWriter.setStrictness(Strictness.LENIENT)}，{@code JsonWriter.setLenient(false)} 应替换为 {@code JsonWriter.setStrictness(Strictness.LEGACY_STRICT)}。<br> 但是，如果您以前使用过 {@code setLenient(false)}，那么您现在可能更喜欢 {@link Strictness#STRICT}。
   * @param lenient 这位作家是否应该宽容。如果为 true，则严格性设置为 {@link Strictness#LENIENT}。如果为 false，则严格性设置为 {@link Strictness#LEGACY_STRICT}。
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
   * 如果此编写器的 {@link Strictness} 等于 {@link Strictness#LENIENT}，则返回 true。
   * @see #getStrictness()
   */
  public boolean isLenient() {
    return strictness == Strictness.LENIENT;
  }

  /**
   * 配置此编写器对于 <a href="https://www.ietf.org/rfc/rfc8259.txt">RFC 8259</a>
   * 中指定的语法规则的严格程度。默认情况下，使用 {@link Strictness#LEGACY_STRICT}。
   * <dl> <dt>{@link Strictness#STRICT} &amp; {@link Strictness#LEGACY_STRICT}
   * <dd>这些行为目前是相同的。在这些严格模式下，编写器仅根据 RFC 8259 编写 JSON。 <dt>{@link Strictness#LENIENT}
   * <dd>此模式放宽了编写器的行为，以允许编写 {@link Double#isNaN() NaNs} 和 {@link Double#isInfinite()
   * infinities}。它还允许写入多个顶级值。 </dl>
   * @param strictness 这位作家的新严格。可能不是 {@code null}。
   * @see #getStrictness()
   * @since 2.11.0
   */
  public final void setStrictness(Strictness strictness) {
    this.strictness = Objects.requireNonNull(strictness);
  }

  /**
   * 返回该作者的 {@linkplain Strictness strictness}。
   * @see #setStrictness(Strictness)
   * @since 2.11.0
   */
  public final Strictness getStrictness() {
    return strictness;
  }

  /**
   * 配置此编写器以发出可安全直接包含在 HTML 和 XML 文档中的 JSON。这会在将 HTML 字符 {@code <}、{@code >}、{@code &}、{@code
   *  =} 和 {@code '} 写入流之前对其进行转义。如果没有此设置，您的 XML/HTML 编码器应将这些字符替换为相应的转义序列。
   * @see #isHtmlSafe()
   */
  public final void setHtmlSafe(boolean htmlSafe) {
    this.htmlSafe = htmlSafe;
  }

  /**
   * 如果此作者编写的 JSON 可以安全地包含在 HTML 和 XML 文档中，则返回 true。
   * @see #setHtmlSafe(boolean)
   */
  public final boolean isHtmlSafe() {
    return htmlSafe;
  }

  /**
   * 设置对象成员的值为空时是否序列化。这对数组元素没有影响。默认为 true。
   * @see #getSerializeNulls()
   */
  public final void setSerializeNulls(boolean serializeNulls) {
    this.serializeNulls = serializeNulls;
  }

  /**
   * 如果对象成员在其值为 null 时被序列化，则返回 true。这对数组元素没有影响。默认为 true。
   * @see #setSerializeNulls(boolean)
   */
  public final boolean getSerializeNulls() {
    return serializeNulls;
  }

  /**
   * 开始对新数组进行编码。对此方法的每次调用都必须与对 {@link #endArray} 的调用配对。
   * @return 作家。
   */
  @CanIgnoreReturnValue
  public JsonWriter beginArray() throws IOException {
    writeDeferredName();
    return openScope(EMPTY_ARRAY, '[');
  }

  /**
   * 结束对当前数组的编码。
   * @return 作家。
   */
  @CanIgnoreReturnValue
  public JsonWriter endArray() throws IOException {
    return closeScope(EMPTY_ARRAY, NONEMPTY_ARRAY, ']');
  }

  /**
   * 开始对新对象进行编码。对此方法的每次调用都必须与对 {@link #endObject} 的调用配对。
   * @return 作家。
   */
  @CanIgnoreReturnValue
  public JsonWriter beginObject() throws IOException {
    writeDeferredName();
    return openScope(EMPTY_OBJECT, '{');
  }

  /**
   * 结束对当前对象的编码。
   * @return 作家。
   */
  @CanIgnoreReturnValue
  public JsonWriter endObject() throws IOException {
    return closeScope(EMPTY_OBJECT, NONEMPTY_OBJECT, '}');
  }

  /** 追加必要空白与左括号，进入新作用域。 */
  @CanIgnoreReturnValue
  private JsonWriter openScope(int empty, char openBracket) throws IOException {
    beforeValue();
    push(empty);
    out.write(openBracket);
    return this;
  }

  /** 追加必要空白与右括号，关闭当前作用域。 */
  @CanIgnoreReturnValue
  private JsonWriter closeScope(int empty, int nonempty, char closeBracket) throws IOException {
    int context = peek();
    if (context != nonempty && context != empty) {
      throw new IllegalStateException("Nesting problem.");
    }
    if (deferredName != null) {
      throw new IllegalStateException("Dangling name: " + deferredName);
    }

    stackSize--;
    if (context == nonempty) {
      newline();
    }
    out.write(closeBracket);
    return this;
  }

  private void push(int newTop) {
    if (stackSize == stack.length) {
      stack = Arrays.copyOf(stack, stackSize * 2);
    }
    stack[stackSize++] = newTop;
  }

  /** 返回栈顶元素。 */
  private int peek() {
    if (stackSize == 0) {
      throw new IllegalStateException("JsonWriter is closed.");
    }
    return stack[stackSize - 1];
  }

  /** 用给定值替换栈顶元素。 */
  private void replaceTop(int topOfStack) {
    stack[stackSize - 1] = topOfStack;
  }

  /**
   * 对属性名称进行编码。
   * @param name 即将到来的值的名称。可能不是 {@code null}。
   * @return 作家。
   */
  @CanIgnoreReturnValue
  public JsonWriter name(String name) throws IOException {
    Objects.requireNonNull(name, "name == null");
    if (deferredName != null) {
      throw new IllegalStateException("Already wrote a name, expecting a value.");
    }
    int context = peek();
    if (context != EMPTY_OBJECT && context != NONEMPTY_OBJECT) {
      throw new IllegalStateException("Please begin an object before writing a name.");
    }
    deferredName = name;
    return this;
  }

  private void writeDeferredName() throws IOException {
    if (deferredName != null) {
      beforeName();
      string(deferredName);
      deferredName = null;
    }
  }

  /**
   * 编码 {@code value}。
   * @param value 文字字符串值，或 null 来编码空文字。
   * @return 作家。
   */
  @CanIgnoreReturnValue
  public JsonWriter value(String value) throws IOException {
    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    beforeValue();
    string(value);
    return this;
  }

  /**
   * 编码 {@code value}。
   * @return 作家。
   */
  @CanIgnoreReturnValue
  public JsonWriter value(boolean value) throws IOException {
    writeDeferredName();
    beforeValue();
    out.write(value ? "true" : "false");
    return this;
  }

  /**
   * 编码 {@code value}。
   * @return 作家。
   * @since 2.7
   */
  @CanIgnoreReturnValue
  public JsonWriter value(Boolean value) throws IOException {
    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    beforeValue();
    out.write(value ? "true" : "false");
    return this;
  }

  /**
   * 编码 {@code value}。
   * @param value 有限值，或者如果是 {@link #setStrictness(Strictness) lenient}，则也是 {@link Float#isNaN() NaN} 或 {@link Float#isInfinite() infinity}。
   * @return 作家。
   * @throws IllegalArgumentException 如果值为 NaN 或 Infinity 并且该编写器不是 {@link #setStrictness(Strictness) lenient}。
   * @since 2.9.1
   */
  @CanIgnoreReturnValue
  public JsonWriter value(float value) throws IOException {
    writeDeferredName();
    if (strictness != Strictness.LENIENT && (Float.isNaN(value) || Float.isInfinite(value))) {
      throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }
    beforeValue();
    out.append(Float.toString(value));
    return this;
  }

  /**
   * 编码 {@code value}。
   * @param value 有限值，或者如果是 {@link #setStrictness(Strictness) lenient}，则也是 {@link Double#isNaN() NaN} 或 {@link Double#isInfinite() infinity}。
   * @return 作家。
   * @throws IllegalArgumentException 如果值为 NaN 或 Infinity 并且该编写器不是 {@link #setStrictness(Strictness) lenient}。
   */
  @CanIgnoreReturnValue
  public JsonWriter value(double value) throws IOException {
    writeDeferredName();
    if (strictness != Strictness.LENIENT && (Double.isNaN(value) || Double.isInfinite(value))) {
      throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }
    beforeValue();
    out.append(Double.toString(value));
    return this;
  }

  /**
   * 编码 {@code value}。
   * @return 作家。
   */
  @CanIgnoreReturnValue
  public JsonWriter value(long value) throws IOException {
    writeDeferredName();
    beforeValue();
    out.write(Long.toString(value));
    return this;
  }

  /**
   * 编码 {@code value}。该值是通过直接将 {@link Number#toString()} 结果写入 JSON 来写入的。实现必须确保结果代表有效的 JSON
   * 数字。
   * @param value 有限值，或者如果是 {@link #setStrictness(Strictness) lenient}，则也是 {@link Double#isNaN() NaN} 或 {@link Double#isInfinite() infinity}。
   * @return 作家。
   * @throws IllegalArgumentException 如果值为 NaN 或 Infinity 并且该作者不是 {@link #setStrictness(Strictness) lenient}；或者如果 {@code toString()} 结果不是有效的 JSON 数字。
   */
  @CanIgnoreReturnValue
  public JsonWriter value(Number value) throws IOException {
    if (value == null) {
      return nullValue();
    }

    writeDeferredName();
    String string = value.toString();
    Class<? extends Number> numberClass = value.getClass();

    if (!alwaysCreatesValidJsonNumber(numberClass)) {
      // 在将字符串直接写入 JSON 输出之前验证该字符串是否有效
      if (string.equals("-Infinity") || string.equals("Infinity") || string.equals("NaN")) {
        if (strictness != Strictness.LENIENT) {
          throw new IllegalArgumentException("Numeric values must be finite, but was " + string);
        }
      } else if (numberClass != Float.class
          && numberClass != Double.class
          && !VALID_JSON_NUMBER_PATTERN.matcher(string).matches()) {
        throw new IllegalArgumentException(
            "String created by " + numberClass + " is not a valid JSON number: " + string);
      }
    }

    beforeValue();
    out.append(string);
    return this;
  }

  /**
   * 编码 {@code null}。
   * @return 作家。
   */
  @CanIgnoreReturnValue
  public JsonWriter nullValue() throws IOException {
    if (deferredName != null) {
      if (serializeNulls) {
        writeDeferredName();
      } else {
        deferredName = null;
        return this; // skip the name and the value
      }
    }
    beforeValue();
    out.write("null");
    return this;
  }

  /**
   * 将 {@code value} 直接写入写入器，无需引用或转义。这可能不受所有实现的支持，如果不支持，则会抛出 {@code
   * UnsupportedOperationException}。
   * @param value 文字字符串值，或 null 来编码空文字。
   * @return 作家。
   * @throws UnsupportedOperationException 如果该编写器不支持写入原始 JSON 值。
   * @since 2.4
   */
  @CanIgnoreReturnValue
  public JsonWriter jsonValue(String value) throws IOException {
    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    beforeValue();
    out.append(value);
    return this;
  }

  /**
   * 确保所有缓冲数据写入底层 {@link Writer} 并刷新该写入器。
   */
  @Override
  public void flush() throws IOException {
    if (stackSize == 0) {
      throw new IllegalStateException("JsonWriter is closed.");
    }
    out.flush();
  }

  /**
   * 刷新并关闭此编写器和底层 {@link Writer}。
   * @throws IOException 如果 JSON 文档不完整。
   */
  @Override
  public void close() throws IOException {
    out.close();

    int size = stackSize;
    if (size > 1 || (size == 1 && stack[size - 1] != NONEMPTY_DOCUMENT)) {
      throw new IOException("Incomplete document");
    }
    stackSize = 0;
  }

  /** 判断 {@code c} 的 {@code toString()} 是否始终产生合法 JSON 数字。 */
  private static boolean alwaysCreatesValidJsonNumber(Class<? extends Number> c) {
    // 不包括 Float 或 Double，因为它们的值可以是 NaN 或 Infinity
    // 不包括 LazilyParsedNumber，因为它可能包含格式错误的字符串
    return c == Integer.class
        || c == Long.class
        || c == Byte.class
        || c == Short.class
        || c == BigDecimal.class
        || c == BigInteger.class
        || c == AtomicInteger.class
        || c == AtomicLong.class;
  }

  private void string(String value) throws IOException {
    String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
    out.write('\"');
    int last = 0;
    int length = value.length();
    for (int i = 0; i < length; i++) {
      char c = value.charAt(i);
      String replacement;
      if (c < 128) {
        replacement = replacements[c];
        if (replacement == null) {
          continue;
        }
      } else if (c == '\u2028') {
        replacement = "\\u2028";
      } else if (c == '\u2029') {
        replacement = "\\u2029";
      } else {
        continue;
      }
      if (last < i) {
        out.write(value, last, i - last);
      }
      out.write(replacement);
      last = i + 1;
    }
    if (last < length) {
      out.write(value, last, length - last);
    }
    out.write('\"');
  }

  private void newline() throws IOException {
    if (usesEmptyNewlineAndIndent) {
      return;
    }

    out.write(formattingStyle.getNewline());
    for (int i = 1, size = stackSize; i < size; i++) {
      out.write(formattingStyle.getIndent());
    }
  }

  /**
   * 在名称前插入任何必要的分隔符和空格。还调整堆栈以期望名称的值。
   */
  private void beforeName() throws IOException {
    int context = peek();
    if (context == NONEMPTY_OBJECT) { // first in object
      out.write(formattedComma);
    } else if (context != EMPTY_OBJECT) { // not in an object!
      throw new IllegalStateException("Nesting problem.");
    }
    newline();
    replaceTop(DANGLING_NAME);
  }

  /**
   * 在文字值、内联数组或内联对象之前插入任何必要的分隔符和空格。还调整堆栈以期望右括号或另一个元素。
   */
  @SuppressWarnings("fallthrough")
  private void beforeValue() throws IOException {
    switch (peek()) {
      case NONEMPTY_DOCUMENT:
        if (strictness != Strictness.LENIENT) {
          throw new IllegalStateException("JSON must have only one top-level value.");
        }
      // 跌倒
      case EMPTY_DOCUMENT: // first in document
        replaceTop(NONEMPTY_DOCUMENT);
        break;

      case EMPTY_ARRAY: // first in array
        replaceTop(NONEMPTY_ARRAY);
        newline();
        break;

      case NONEMPTY_ARRAY: // another in array
        out.append(formattedComma);
        newline();
        break;

      case DANGLING_NAME: // value for name
        out.append(formattedColon);
        replaceTop(NONEMPTY_OBJECT);
        break;

      default:
        throw new IllegalStateException("Nesting problem.");
    }
  }
}
