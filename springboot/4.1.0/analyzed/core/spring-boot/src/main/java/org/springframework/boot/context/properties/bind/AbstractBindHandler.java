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
import org.springframework.util.Assert;

/**
 * {@link BindHandler} 实现的抽象基类。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.0.0
 */
public abstract class AbstractBindHandler implements BindHandler {

	private final BindHandler parent;

	/**
	 * 创建新的绑定处理器实例。
	 */
	public AbstractBindHandler() {
		this(BindHandler.DEFAULT);
	}

	/**
	 * 使用指定父处理器创建新的绑定处理器实例。
	 *
	 * @param parent 父处理器
	 */
	public AbstractBindHandler(BindHandler parent) {
		Assert.notNull(parent, "'parent' must not be null");
		this.parent = parent;
	}

	@Override
	public <T> @Nullable Bindable<T> onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
		return this.parent.onStart(name, target, context);
	}

	@Override
	public @Nullable Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context,
			Object result) {
		return this.parent.onSuccess(name, target, context, result);
	}

	@Override
	public @Nullable Object onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context,
			Exception error) throws Exception {
		return this.parent.onFailure(name, target, context, error);
	}

	@Override
	public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context,
			@Nullable Object result) throws Exception {
		this.parent.onFinish(name, target, context, result);
	}

}
