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

import io.micrometer.context.ThreadLocalAccessor;
import org.jspecify.annotations.Nullable;

/**
 * 将 {@link LocaleContextHolder} 适配为 {@link ThreadLocalAccessor} 契约，
 * 协助 Micrometer Context Propagation 库传播 {@link LocaleContext}。
 *
 * @author Tadaya Tsuyukubo
 * @since 6.2
 */
public class LocaleContextThreadLocalAccessor implements ThreadLocalAccessor<LocaleContext> {

	/**
	 * 本访问器在 {@link io.micrometer.context.ContextRegistry} 中注册时使用的键。
	 */
	public static final String KEY = LocaleContextThreadLocalAccessor.class.getName() + ".KEY";

	@Override
	public Object key() {
		return KEY;
	}

	@Override
	public @Nullable LocaleContext getValue() {
		return LocaleContextHolder.getLocaleContext();
	}

	@Override
	public void setValue(LocaleContext value) {
		LocaleContextHolder.setLocaleContext(value);
	}

	@Override
	public void setValue() {
		LocaleContextHolder.resetLocaleContext();
	}

}
