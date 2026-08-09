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

package org.springframework.aop.aspectj.annotation;

import org.springframework.aop.aspectj.SimpleAspectInstanceFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;

/**
 * {@link MetadataAwareAspectInstanceFactory} 的实现，为每个 {@link #getAspectInstance()}
 * 调用创建指定方面类的新实例。
 * @author Juergen Hoeller
 * @since 2.0.4
 */
public class SimpleMetadataAwareAspectInstanceFactory extends SimpleAspectInstanceFactory
		implements MetadataAwareAspectInstanceFactory {

	/** `metadata`：该类的成员状态。 */
	private final AspectMetadata metadata;


	/**
	 * 为给定的方面类创建一个新的 SimpleMetadataAwareAspectInstanceFactory。
	 * @param aspectClass 方面类
	 * @param aspectName 方面名称
	 */
	public SimpleMetadataAwareAspectInstanceFactory(Class<?> aspectClass, String aspectName) {
		super(aspectClass);
		this.metadata = new AspectMetadata(aspectClass, aspectName);
	}


	/**
	 * 获取 Aspect Metadata（`AspectMetadata`）。
	 */
	@Override
	public final AspectMetadata getAspectMetadata() {
		return this.metadata;
	}

	/**
	 * 获取 Aspect Creation Mutex（`AspectCreationMutex`）。
	 */
	@Override
	public Object getAspectCreationMutex() {
		return this;
	}

	/**
	 * 获取 Order For Aspect Class（`OrderForAspectClass`）。
	 */
	@Override
	protected int getOrderForAspectClass(Class<?> aspectClass) {
		return OrderUtils.getOrder(aspectClass, Ordered.LOWEST_PRECEDENCE);
	}

}
