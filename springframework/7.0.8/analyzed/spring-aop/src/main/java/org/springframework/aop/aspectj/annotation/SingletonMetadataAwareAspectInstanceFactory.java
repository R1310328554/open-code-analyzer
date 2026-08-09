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

import java.io.Serializable;

import org.springframework.aop.aspectj.SingletonAspectInstanceFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;

/**
 * {@link MetadataAwareAspectInstanceFactory} 的实现，
 * 由指定单例对象支持，每次 {@link #getAspectInstance()} 调用返回同一实例。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see SimpleMetadataAwareAspectInstanceFactory
 */
@SuppressWarnings("serial")
public class SingletonMetadataAwareAspectInstanceFactory extends SingletonAspectInstanceFactory
		implements MetadataAwareAspectInstanceFactory, Serializable {

	private final AspectMetadata metadata;


	/**
	 * 为给定切面创建新的 SingletonMetadataAwareAspectInstanceFactory。
	 * @param aspectInstance 单例切面实例
	 * @param aspectName 切面名称
	 */
	public SingletonMetadataAwareAspectInstanceFactory(Object aspectInstance, String aspectName) {
		super(aspectInstance);
		this.metadata = new AspectMetadata(aspectInstance.getClass(), aspectName);
	}


	@Override
	public final AspectMetadata getAspectMetadata() {
		return this.metadata;
	}

	@Override
	public Object getAspectCreationMutex() {
		return this;
	}

	@Override
	protected int getOrderForAspectClass(Class<?> aspectClass) {
		return OrderUtils.getOrder(aspectClass, Ordered.LOWEST_PRECEDENCE);
	}

}
