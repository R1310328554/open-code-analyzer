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

package org.springframework.context.i18n;

import java.util.TimeZone;

import org.jspecify.annotations.Nullable;

/**
 * {@link LocaleContext} 的扩展，增加对当前时区的感知能力。
 *
 * <p>在 {@link LocaleContextHolder} 中设置此变体的 LocaleContext 表示
 * 已配置时区感知的基础设施，即使当前可能尚无法提供非 null 的 TimeZone。
 *
 * @author Juergen Hoeller
 * @author Nicholas Williams
 * @since 4.0
 * @see LocaleContextHolder#getTimeZone()
 */
public interface TimeZoneAwareLocaleContext extends LocaleContext {

	/**
	 * 返回当前 TimeZone；可为固定值或动态确定，取决于实现策略。
	 * @return 当前 TimeZone，或 {@code null} 表示未关联特定时区
	 */
	@Nullable TimeZone getTimeZone();

}
