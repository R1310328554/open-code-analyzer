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

package org.springframework.boot.autoconfigure.condition;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 检查环境中是否定义了值为列表的属性。
 * <p>
 * 通过 {@link Binder} 绑定指定属性名，若成功绑定为 {@code List<String>} 则条件匹配。
 *
 * @author Eneias Silva
 * @author Stephane Nicoll
 * @since 2.0.5
 */
public abstract class OnPropertyListCondition extends SpringBootCondition {

	private static final Bindable<List<String>> STRING_LIST = Bindable.listOf(String.class);

	private final String propertyName;

	private final Supplier<ConditionMessage.Builder> messageBuilder;

	/**
	 * 创建新实例。
	 *
	 * @param propertyName 要检查的属性名
	 * @param messageBuilder 消息构建器供应者，每次调用应提供新实例
	 */
	protected OnPropertyListCondition(String propertyName, Supplier<ConditionMessage.Builder> messageBuilder) {
		this.propertyName = propertyName;
		this.messageBuilder = messageBuilder;
	}

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		BindResult<?> property = Binder.get(context.getEnvironment()).bind(this.propertyName, STRING_LIST);
		ConditionMessage.Builder messageBuilder = this.messageBuilder.get();
		if (property.isBound()) {
			return ConditionOutcome.match(messageBuilder.found("property").items(this.propertyName));
		}
		return ConditionOutcome.noMatch(messageBuilder.didNotFind("property").items(this.propertyName));
	}

}
