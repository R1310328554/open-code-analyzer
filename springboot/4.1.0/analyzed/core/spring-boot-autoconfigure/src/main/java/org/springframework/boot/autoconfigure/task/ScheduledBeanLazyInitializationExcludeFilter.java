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

package org.springframework.boot.autoconfigure.task;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.LazyInitializationExcludeFilter;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.util.ClassUtils;

/**
 * 检测标注 {@link Scheduled} 或 {@link Schedules} 的 Bean 方法的
 * {@link LazyInitializationExcludeFilter}。
 * <p>
 * 在启用懒初始化时，含 {@code @Scheduled} 方法的 Bean 需立即初始化，
 * 以便调度注解处理器能正确注册定时任务。
 *
 * @author Stephane Nicoll
 */
class ScheduledBeanLazyInitializationExcludeFilter implements LazyInitializationExcludeFilter {

	private final Set<Class<?>> nonAnnotatedClasses = ConcurrentHashMap.newKeySet(64);

	ScheduledBeanLazyInitializationExcludeFilter() {
		// Ignore AOP infrastructure such as scoped proxies.
		this.nonAnnotatedClasses.add(AopInfrastructureBean.class);
		this.nonAnnotatedClasses.add(TaskScheduler.class);
		this.nonAnnotatedClasses.add(ScheduledExecutorService.class);
	}

	@Override
	public boolean isExcluded(String beanName, BeanDefinition beanDefinition, Class<?> beanType) {
		return hasScheduledTask(beanType);
	}

	private boolean hasScheduledTask(Class<?> type) {
		Class<?> targetType = ClassUtils.getUserClass(type);
		if (!this.nonAnnotatedClasses.contains(targetType)
				&& AnnotationUtils.isCandidateClass(targetType, Arrays.asList(Scheduled.class, Schedules.class))) {
			Map<Method, Set<Scheduled>> annotatedMethods = MethodIntrospector.selectMethods(targetType,
					(MethodIntrospector.MetadataLookup<Set<Scheduled>>) (method) -> {
						Set<Scheduled> scheduledAnnotations = AnnotatedElementUtils
							.getMergedRepeatableAnnotations(method, Scheduled.class, Schedules.class);
						return (!scheduledAnnotations.isEmpty() ? scheduledAnnotations : null);
					});
			if (annotatedMethods.isEmpty()) {
				this.nonAnnotatedClasses.add(targetType);
			}
			return !annotatedMethods.isEmpty();
		}
		return false;
	}

}
