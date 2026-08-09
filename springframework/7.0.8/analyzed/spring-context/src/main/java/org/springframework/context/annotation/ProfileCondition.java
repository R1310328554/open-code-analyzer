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

package org.springframework.context.annotation;

import org.jspecify.annotations.Nullable;

import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;

/**
 * 根据 {@link Profile @Profile} 注解的值进行匹配的 {@link Condition}。
 *
 * @author Chris Beams
 * @author Phillip Webb
 * @author Juergen Hoeller
 * @since 4.0
 */
class ProfileCondition implements Condition {

	/**
	 * 若元数据上存在 {@code @Profile}，则检查当前环境是否匹配其中任一 profile 表达式；
	 * 无该注解时默认匹配成功。
	 */
	@Override
	@SuppressWarnings("NullAway") // Reflection
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		MultiValueMap<String, @Nullable Object> attrs = metadata.getAllAnnotationAttributes(Profile.class.getName());
		if (attrs != null) {
			for (Object value : attrs.get("value")) {
				if (context.getEnvironment().matchesProfiles((String[]) value)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

}
