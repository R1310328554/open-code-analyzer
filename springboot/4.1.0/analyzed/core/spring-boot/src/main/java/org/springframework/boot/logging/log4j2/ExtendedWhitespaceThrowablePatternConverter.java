/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.logging.log4j2;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.ExtendedThrowablePatternConverter;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.ThrowablePatternConverter;
import org.jspecify.annotations.Nullable;

/**
 * 在堆栈跟踪周围添加额外空白字符的 {@link ThrowablePatternConverter}。
 *
 * @author Vladimir Tsanev
 * @author Phillip Webb
 * @since 1.3.0
 */
@Plugin(name = "ExtendedWhitespaceThrowablePatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "xwEx", "xwThrowable", "xwException" })
public final class ExtendedWhitespaceThrowablePatternConverter extends LogEventPatternConverter {

	private final ExtendedThrowablePatternConverter delegate;

	private final String separator;

	private ExtendedWhitespaceThrowablePatternConverter(Configuration configuration, @Nullable String[] options) {
		super("WhitespaceExtendedThrowable", "throwable");
		this.delegate = ExtendedThrowablePatternConverter.newInstance(configuration, options);
		this.separator = this.delegate.getOptions().getSeparator();
	}

	@Override
	public void format(LogEvent event, StringBuilder buffer) {
		if (event.getThrown() != null) {
			buffer.append(this.separator);
			this.delegate.format(event, buffer);
			buffer.append(this.separator);
		}
	}

	@Override
	public boolean handlesThrowable() {
		return true;
	}

	/**
	 * 创建类的新实例。Log4J2 要求提供此方法。
	 *
	 * @param configuration 当前配置
	 * @param options 模式选项，可为 null；若首元素为 "short" 则仅格式化 throwable 首行
	 * @return 新的 {@code WhitespaceThrowablePatternConverter}
	 */
	public static ExtendedWhitespaceThrowablePatternConverter newInstance(Configuration configuration,
			@Nullable String[] options) {
		return new ExtendedWhitespaceThrowablePatternConverter(configuration, options);
	}

}
