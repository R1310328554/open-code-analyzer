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

package org.springframework.boot.context.properties.bind;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

/**
 * 在元素 {@link Binder 绑定} 过程中处理额外逻辑的回调接口。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.0.0
 */
public interface BindHandler {

	/**
	 * 默认的空操作绑定处理器。
	 */
	BindHandler DEFAULT = new BindHandler() {

	};

	/**
	 * 元素绑定开始时、尚未确定结果前调用。
	 *
	 * @param <T> Bindable 源类型
	 * @param name 正在绑定的元素名称
	 * @param target 正在绑定的项
	 * @param context 绑定上下文
	 * @return 实际用于绑定的项（可能为 {@code null}）
	 */
	default <T> @Nullable Bindable<T> onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
		return target;
	}

	/**
	 * 元素绑定成功结束时调用。实现可修改最终返回结果或执行额外校验。
	 *
	 * @param name 正在绑定的元素名称
	 * @param target 正在绑定的项
	 * @param context 绑定上下文
	 * @param result 绑定结果（永不为 {@code null}）
	 * @return 实际应使用的结果（可能为 {@code null}）
	 */
	default @Nullable Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context,
			Object result) {
		return result;
	}

	/**
	 * 元素绑定未产生绑定值、即将返回新创建实例时调用。实现可修改最终返回结果或执行额外校验。
	 *
	 * @param name 正在绑定的元素名称
	 * @param target 正在绑定的项
	 * @param context 绑定上下文
	 * @param result 新创建的实例（永不为 {@code null}）
	 * @return 实际应使用的结果（不得为 {@code null}）
	 * @since 2.2.2
	 */
	default Object onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
		return result;
	}

	/**
	 * 绑定因任何原因失败时调用（包括 {@link #onSuccess} 或 {@link #onCreate} 中的失败）。
	 * 实现可选择吞掉异常并返回替代结果。
	 *
	 * @param name 正在绑定的元素名称
	 * @param target 正在绑定的项
	 * @param context 绑定上下文
	 * @param error 错误原因（若异常未被处理则可能被重新抛出）
	 * @return 实际应使用的结果（可能为 {@code null}）
	 * @throws Exception 若绑定无效
	 */
	default @Nullable Object onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context,
			Exception error) throws Exception {
		throw error;
	}

	/**
	 * 绑定完成时调用（无论是否产生绑定值）。绑定失败时不调用此方法，
	 * 即使处理器从 {@link #onFailure} 返回了结果。
	 *
	 * @param name 正在绑定的元素名称
	 * @param target 正在绑定的项
	 * @param context 绑定上下文
	 * @param result 绑定结果（可能为 {@code null}）
	 * @throws Exception 若绑定无效
	 */
	default void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context,
			@Nullable Object result) throws Exception {
	}

}
