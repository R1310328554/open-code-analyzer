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

package org.springframework.boot.autoconfigure.context;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.DefaultLifecycleProcessor;

/**
 * 与应用上下文生命周期相关的 {@link EnableAutoConfiguration 自动配置}。
 * <p>
 * 注册默认的 {@link DefaultLifecycleProcessor}，
 * 并根据 {@link LifecycleProperties} 配置各关闭阶段的超时时间。
 *
 * @author Andy Wilkinson
 * @since 2.3.0
 */
@AutoConfiguration
@EnableConfigurationProperties(LifecycleProperties.class)
public final class LifecycleAutoConfiguration {

	@Bean(name = AbstractApplicationContext.LIFECYCLE_PROCESSOR_BEAN_NAME)
	@ConditionalOnMissingBean(name = AbstractApplicationContext.LIFECYCLE_PROCESSOR_BEAN_NAME,
			search = SearchStrategy.CURRENT)
	DefaultLifecycleProcessor defaultLifecycleProcessor(LifecycleProperties properties) {
		DefaultLifecycleProcessor lifecycleProcessor = new DefaultLifecycleProcessor();
		lifecycleProcessor.setTimeoutPerShutdownPhase(properties.getTimeoutPerShutdownPhase().toMillis());
		return lifecycleProcessor;
	}

}
