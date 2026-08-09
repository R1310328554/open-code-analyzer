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

import java.util.Locale;

import org.jspecify.annotations.Nullable;

/**
 * 用于确定当前 {@link Locale} 的策略接口。
 *
 * <p>{@code LocaleContext} 实例可通过 {@link LocaleContextHolder} 与线程关联。
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see LocaleContextHolder#getLocale()
 * @see TimeZoneAwareLocaleContext
 */
public interface LocaleContext {

	/**
	 * 返回当前 {@link Locale}，可为固定值或动态确定，取决于实现策略。
	 * @return 当前 Locale；若未关联特定 Locale 则返回 {@code null}
	 */
	@Nullable Locale getLocale();

}
