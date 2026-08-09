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

package org.springframework.format.datetime.standard;

import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

import org.springframework.util.StringUtils;

/**
 * 内部 {@link DateTimeFormatter} 工具类。
 *
 * @author Juergen Hoeller
 * @since 5.3.5
 */
abstract class DateTimeFormatterUtils {

	/**
	 * 为给定图案创建 {@link DateTimeFormatter}，并配置为
	 * {@linkplain ResolverStyle#STRICT 严格}解析模式。
	 * <p>注意，严格解析模式不影响解析行为。
	 * @param pattern 要使用的图案
	 * @return 新的 {@code DateTimeFormatter}
	 * @see ResolverStyle#STRICT
	 */
	static DateTimeFormatter createStrictDateTimeFormatter(String pattern) {
		// Using strict resolution to align with standard DateFormat behavior:
		// otherwise, an overflow like, for example, Feb 29 for a non-leap-year wouldn't get rejected.
		// However, with strict resolution, a year digit needs to be specified as 'u'...
		String patternToUse = StringUtils.replace(pattern, "yy", "uu");
		return DateTimeFormatter.ofPattern(patternToUse).withResolverStyle(ResolverStyle.STRICT);
	}

}
