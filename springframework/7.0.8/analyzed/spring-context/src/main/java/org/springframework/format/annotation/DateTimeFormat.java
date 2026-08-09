/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.format.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明字段或方法参数应按日期或时间格式化。
 *
 * <p>格式化既适用于将日期/时间对象从字符串解析出来，也适用于将日期/时间对象打印为字符串。
 *
 * <p>支持按样式模式、ISO 日期/时间模式或自定义格式模式字符串进行格式化。
 * 可应用于 {@link java.util.Date}、{@link java.util.Calendar}、{@link Long}（毫秒时间戳）
 * 以及 JSR-310 {@code java.time} 值类型。
 *
 * <p>对于基于样式的格式化，将 {@link #style} 属性设为所需的样式模式代码。
 * 代码的第一个字符表示日期样式，第二个字符表示时间样式。
 * 使用 'S' 表示短样式，'M' 表示中等，'L' 表示长样式，'F' 表示完整样式。
 * 可通过将样式字符设为 '-' 来省略日期或时间。例如，'M-' 表示中等日期格式且不包含时间。
 * 支持的样式模式代码与 {@link java.time.format.FormatStyle} 中定义的枚举常量相对应。
 *
 * <p><strong>警告</strong>：基于样式的格式化与解析依赖与区域设置相关的模式，这些模式可能随 Java 运行时变化。
 * 具体而言，依赖日期/时间解析与格式化的应用在 JDK 20 或更高版本上运行时，
 * 可能遇到不兼容的行为变化。使用 ISO 标准化格式或您可控的具体模式，
 * 可实现可靠、与系统及区域设置无关的日期/时间解析与格式化。
 * 使用 {@linkplain #fallbackPatterns() 回退模式}也有助于解决兼容性问题。
 * 更多细节请参阅 Spring Framework wiki 中的
 * <a href="https://github.com/spring-projects/spring-framework/wiki/Date-and-Time-Formatting-with-JDK-20-and-higher">
 * Date and Time Formatting with JDK 20 and higher</a> 页面。
 *
 * <p>对于基于 ISO 的格式化，将 {@link #iso} 属性设为所需的 {@link ISO} 格式，例如 {@link ISO#DATE}。
 *
 * <p>对于自定义格式化，将 {@link #pattern} 属性设为日期时间模式，例如 {@code "yyyy/MM/dd hh:mm:ss a"}。
 *
 * <p>各属性互斥，因此每个注解实例只应设置一个属性（选择最符合您格式化需求的那个）。
 *
 * <ul>
 * <li>指定 pattern 属性时，其优先级高于 style 与 ISO 属性。</li>
 * <li>指定 {@link #iso} 属性时，其优先级高于 style 属性。</li>
 * <li>未指定任何注解属性时，默认应用样式代码为 'SS'（短日期、短时间）的基于样式的格式。</li>
 * </ul>
 *
 * <h3>时区</h3>
 * <p>使用 {@link #style} 或 {@link #pattern} 属性时，格式化 {@link java.util.Date} 值将使用
 * JVM 的 {@linkplain java.util.TimeZone#getDefault() 默认时区}。
 * 使用 {@link #iso} 属性格式化 {@link java.util.Date} 值时，将使用 {@code UTC} 作为时区。
 * 相同的时区也会应用于任何 {@linkplain #fallbackPatterns 回退模式}。
 * 若要强制一致使用 {@code UTC} 作为时区，可在启动 JVM 时添加 {@code -Duser.timezone=UTC}。
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.0
 * @see java.text.DateFormat
 * @see java.text.SimpleDateFormat
 * @see java.time.format.DateTimeFormatter
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface DateTimeFormat {

	/**
	 * 用于格式化字段或方法参数的样式模式。
	 * <p>默认为 'SS'（短日期、短时间）。若希望按不同于默认样式的通用样式格式化字段或方法参数，请设置此属性。
	 * <p>更多细节请参阅 {@linkplain DateTimeFormat 类级文档}。
	 * @see #fallbackPatterns
	 */
	String style() default "SS";

	/**
	 * 用于格式化字段或方法参数的 ISO 模式。
	 * <p>支持的 ISO 模式在 {@link ISO} 枚举中定义。
	 * <p>默认为 {@link ISO#NONE}，表示应忽略此属性。
	 * 若希望按 ISO 格式格式化字段或方法参数，请设置此属性。
	 * @see #fallbackPatterns
	 */
	ISO iso() default ISO.NONE;

	/**
	 * 用于格式化字段或方法参数的自定义模式。
	 * <p>默认为空字符串，表示未指定自定义模式。
	 * 若希望按样式或 ISO 格式无法表示的自定义日期时间模式格式化字段或方法参数，请设置此属性。
	 * <p>注意：此模式遵循原始 {@link java.text.SimpleDateFormat} 风格，
	 * 对溢出采用严格解析语义（例如，拒绝非闰年的 {@code Feb 29} 值）。
	 * 因此，'yy' 字符表示传统意义上的年份，而非 {@link java.time.format.DateTimeFormatter}
	 * 规范中的“年代年份”（即 'yy' 在经严格解析模式的 {@code DateTimeFormatter} 转换时变为 'uu'）。
	 * @see #fallbackPatterns
	 */
	String pattern() default "";

	/**
	 * 当主 {@link #pattern}、{@link #iso} 或 {@link #style} 属性解析失败时用作回退的自定义模式集合。
	 * <p>例如，若希望使用 ISO 日期格式进行解析与打印，但允许对用户输入的各种日期格式进行宽松解析，
	 * 可配置类似以下的内容。
	 * <pre style="code">
	 * {@literal @}DateTimeFormat(iso = ISO.DATE, fallbackPatterns = { "M/d/yy", "dd.MM.yyyy" })
	 * </pre>
	 * <p>回退模式仅用于解析，不用于将值打印为字符串。
	 * 打印时始终使用主 {@link #pattern}、{@link #iso} 或 {@link #style} 属性。
	 * 关于回退模式使用哪个时区的细节，请参阅 {@linkplain DateTimeFormat 类级文档}。
	 * @since 5.3.5
	 */
	String[] fallbackPatterns() default {};


	/**
	 * 常用的 ISO 日期时间格式模式。
	 */
	enum ISO {

		/**
		 * 最常用的 ISO 日期格式 {@code yyyy-MM-dd} &mdash; 例如 "2000-10-31"。
		 */
		DATE,

		/**
		 * 最常用的 ISO 时间格式 {@code HH:mm:ss.SSSXXX} &mdash; 例如 "01:30:00.000-05:00"。
		 */
		TIME,

		/**
		 * 最常用的 ISO 日期时间格式 {@code yyyy-MM-dd'T'HH:mm:ss.SSSXXX}
		 * &mdash; 例如 "2000-10-31T01:30:00.000-05:00"。
		 */
		DATE_TIME,

		/**
		 * 表示不应应用任何基于 ISO 的格式模式。
		 */
		NONE
	}

}
