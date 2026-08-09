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

package org.springframework.jms.config;

import java.util.Locale;

import jakarta.jms.Session;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;

/* ===== [OCA 中文解析] =====
class JmsListenerContainerParser — 意图说明

class `JmsListenerContainerParser`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-jms/src/main/java/org/springframework/jms/config/JmsListenerContainerParser.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Parser for the JMS {@code <listener-container>} element.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 2.5
 */
class JmsListenerContainerParser extends AbstractListenerContainerParser {

	// [OCA] 字段 `CONTAINER_TYPE_ATTRIBUTE`：类成员状态。
	private static final String CONTAINER_TYPE_ATTRIBUTE = "container-type";

	// [OCA] 字段 `CONTAINER_CLASS_ATTRIBUTE`：类成员状态。
	private static final String CONTAINER_CLASS_ATTRIBUTE = "container-class";

	// [OCA] 字段 `CONNECTION_FACTORY_ATTRIBUTE`：类成员状态。
	private static final String CONNECTION_FACTORY_ATTRIBUTE = "connection-factory";

	// [OCA] 字段 `TASK_EXECUTOR_ATTRIBUTE`：类成员状态。
	private static final String TASK_EXECUTOR_ATTRIBUTE = "task-executor";

	// [OCA] 字段 `ERROR_HANDLER_ATTRIBUTE`：类成员状态。
	private static final String ERROR_HANDLER_ATTRIBUTE = "error-handler";

	// [OCA] 字段 `CACHE_ATTRIBUTE`：类成员状态。
	private static final String CACHE_ATTRIBUTE = "cache";

	// [OCA] 字段 `RECEIVE_TIMEOUT_ATTRIBUTE`：类成员状态。
	private static final String RECEIVE_TIMEOUT_ATTRIBUTE = "receive-timeout";

	// [OCA] 字段 `RECOVERY_INTERVAL_ATTRIBUTE`：类成员状态。
	private static final String RECOVERY_INTERVAL_ATTRIBUTE = "recovery-interval";

	// [OCA] 字段 `BACK_OFF_ATTRIBUTE`：类成员状态。
	private static final String BACK_OFF_ATTRIBUTE = "back-off";


	@Override
	protected @Nullable RootBeanDefinition createContainerFactory(String factoryId, Element containerEle, ParserContext parserContext,
			PropertyValues commonContainerProperties, PropertyValues specificContainerProperties) {

		RootBeanDefinition factoryDef = new RootBeanDefinition();

		String containerType = containerEle.getAttribute(CONTAINER_TYPE_ATTRIBUTE);
		String containerClass = containerEle.getAttribute(CONTAINER_CLASS_ATTRIBUTE);
		if (StringUtils.hasLength(containerClass)) {
			return null;  // not supported
		}
		else if (!StringUtils.hasLength(containerType) || containerType.startsWith("default")) {
			factoryDef.setBeanClassName("org.springframework.jms.config.DefaultJmsListenerContainerFactory");
		}
		else if (containerType.startsWith("simple")) {
			factoryDef.setBeanClassName("org.springframework.jms.config.SimpleJmsListenerContainerFactory");
		}

		factoryDef.getPropertyValues().addPropertyValues(commonContainerProperties);
		factoryDef.getPropertyValues().addPropertyValues(specificContainerProperties);

		return factoryDef;
	}

	@Override
	protected RootBeanDefinition createContainer(Element containerEle, Element listenerEle, ParserContext parserContext,
			PropertyValues commonContainerProperties, PropertyValues specificContainerProperties) {

		RootBeanDefinition containerDef = new RootBeanDefinition();
		containerDef.setSource(parserContext.extractSource(containerEle));
		containerDef.getPropertyValues().addPropertyValues(commonContainerProperties);
		containerDef.getPropertyValues().addPropertyValues(specificContainerProperties);

		String containerType = containerEle.getAttribute(CONTAINER_TYPE_ATTRIBUTE);
		String containerClass = containerEle.getAttribute(CONTAINER_CLASS_ATTRIBUTE);
		if (StringUtils.hasLength(containerClass)) {
			containerDef.setBeanClassName(containerClass);
		}
		else if (!StringUtils.hasLength(containerType) || containerType.startsWith("default")) {
			containerDef.setBeanClassName("org.springframework.jms.listener.DefaultMessageListenerContainer");
		}
		else if (containerType.startsWith("simple")) {
			containerDef.setBeanClassName("org.springframework.jms.listener.SimpleMessageListenerContainer");
		}
		else {
			parserContext.getReaderContext().error(
					"Invalid 'container-type' attribute: only \"default\" and \"simple\" supported.", containerEle);
		}

		// Parse listener specific settings
		parseListenerConfiguration(listenerEle, parserContext, containerDef.getPropertyValues());

		return containerDef;
	}

	@Override
	/* ===== [OCA 中文解析] =====
方法 parseSpecificContainerProperties — 意图与阅读要点

方法 `parseSpecificContainerProperties` 复杂度较高（CCN≈25, NLOC≈94）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	protected MutablePropertyValues parseSpecificContainerProperties(Element containerEle, ParserContext parserContext) {
		MutablePropertyValues properties = new MutablePropertyValues();

		boolean isSimpleContainer = containerEle.getAttribute(CONTAINER_TYPE_ATTRIBUTE).startsWith("simple");

		String connectionFactoryBeanName = "connectionFactory";
		if (containerEle.hasAttribute(CONNECTION_FACTORY_ATTRIBUTE)) {
			connectionFactoryBeanName = containerEle.getAttribute(CONNECTION_FACTORY_ATTRIBUTE);
			if (!StringUtils.hasText(connectionFactoryBeanName)) {
				parserContext.getReaderContext().error(
						"Listener container 'connection-factory' attribute contains empty value.", containerEle);
			}
		}
		if (StringUtils.hasText(connectionFactoryBeanName)) {
			properties.add("connectionFactory", new RuntimeBeanReference(connectionFactoryBeanName));
		}

		String taskExecutorBeanName = containerEle.getAttribute(TASK_EXECUTOR_ATTRIBUTE);
		if (StringUtils.hasText(taskExecutorBeanName)) {
			properties.add("taskExecutor", new RuntimeBeanReference(taskExecutorBeanName));
		}

		String errorHandlerBeanName = containerEle.getAttribute(ERROR_HANDLER_ATTRIBUTE);
		if (StringUtils.hasText(errorHandlerBeanName)) {
			properties.add("errorHandler", new RuntimeBeanReference(errorHandlerBeanName));
		}

		String destinationResolverBeanName = containerEle.getAttribute(DESTINATION_RESOLVER_ATTRIBUTE);
		if (StringUtils.hasText(destinationResolverBeanName)) {
			properties.add("destinationResolver", new RuntimeBeanReference(destinationResolverBeanName));
		}

		String cache = containerEle.getAttribute(CACHE_ATTRIBUTE);
		if (StringUtils.hasText(cache)) {
			if (isSimpleContainer) {
				if (!("auto".equals(cache) || "consumer".equals(cache))) {
					parserContext.getReaderContext().warning(
							"'cache' attribute not actively supported for listener container of type \"simple\". " +
							"Effective runtime behavior will be equivalent to \"consumer\" / \"auto\".", containerEle);
				}
			}
			else {
				properties.add("cacheLevelName", "CACHE_" + cache.toUpperCase(Locale.ROOT));
			}
		}

		Integer acknowledgeMode = parseAcknowledgeMode(containerEle, parserContext);
		if (acknowledgeMode != null) {
			if (acknowledgeMode == Session.SESSION_TRANSACTED) {
				properties.add("sessionTransacted", Boolean.TRUE);
			}
			else {
				properties.add("sessionAcknowledgeMode", acknowledgeMode);
			}
		}

		String transactionManagerBeanName = containerEle.getAttribute(TRANSACTION_MANAGER_ATTRIBUTE);
		if (StringUtils.hasText(transactionManagerBeanName)) {
			if (isSimpleContainer) {
				parserContext.getReaderContext().error(
						"'transaction-manager' attribute not supported for listener container of type \"simple\".", containerEle);
			}
			else {
				properties.add("transactionManager", new RuntimeBeanReference(transactionManagerBeanName));
			}
		}

		String concurrency = containerEle.getAttribute(CONCURRENCY_ATTRIBUTE);
		if (StringUtils.hasText(concurrency)) {
			properties.add("concurrency", concurrency);
		}

		String prefetch = containerEle.getAttribute(PREFETCH_ATTRIBUTE);
		if (StringUtils.hasText(prefetch)) {
			if (!isSimpleContainer) {
				properties.add("maxMessagesPerTask", prefetch);
			}
		}

		String phase = containerEle.getAttribute(PHASE_ATTRIBUTE);
		if (StringUtils.hasText(phase)) {
			properties.add("phase", phase);
		}

		String receiveTimeout = containerEle.getAttribute(RECEIVE_TIMEOUT_ATTRIBUTE);
		if (StringUtils.hasText(receiveTimeout)) {
			if (!isSimpleContainer) {
				properties.add("receiveTimeout", receiveTimeout);
			}
		}

		String backOffBeanName = containerEle.getAttribute(BACK_OFF_ATTRIBUTE);
		if (StringUtils.hasText(backOffBeanName)) {
			if (!isSimpleContainer) {
				properties.add("backOff", new RuntimeBeanReference(backOffBeanName));
			}
		}
		else { // No need to consider this if back-off is set
			String recoveryInterval = containerEle.getAttribute(RECOVERY_INTERVAL_ATTRIBUTE);
			if (StringUtils.hasText(recoveryInterval)) {
				if (!isSimpleContainer) {
					properties.add("recoveryInterval", recoveryInterval);
				}
			}
		}

		return properties;
	}

}
