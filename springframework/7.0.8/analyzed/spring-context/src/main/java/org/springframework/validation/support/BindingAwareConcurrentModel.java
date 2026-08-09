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

package org.springframework.validation.support;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BindingResult;

/**
 * {@link ConcurrentModel} 的子类，当对应目标属性通过常规 {@link Map} 操作被替换时，
 * 自动移除关联的 {@link BindingResult} 对象。
 *
 * <p>Spring WebFlux 向处理器方法暴露的 model 类，
 * 通常通过将 {@link org.springframework.ui.Model} 接口声明为参数类型来使用。
 * 用户代码一般无需自行创建；若确有需要，处理器方法也可返回
 * 普通 {@code java.util.Map}（通常为 {@code java.util.ConcurrentMap}）作为预定 model。
 *
 * @author Rossen Stoyanchev
 * @since 5.0
 * @see BindingResult
 * @see BindingAwareModelMap
 */
@SuppressWarnings("serial")
public class BindingAwareConcurrentModel extends ConcurrentModel {

	@Override
	public @Nullable Object put(String key, @Nullable Object value) {
		removeBindingResultIfNecessary(key, value);
		return super.put(key, value);
	}

	private void removeBindingResultIfNecessary(String key, @Nullable Object value) {
		if (!key.startsWith(BindingResult.MODEL_KEY_PREFIX)) {
			String resultKey = BindingResult.MODEL_KEY_PREFIX + key;
			BindingResult result = (BindingResult) get(resultKey);
			if (result != null && result.getTarget() != value) {
				remove(resultKey);
			}
		}
	}

}