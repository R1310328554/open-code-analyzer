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

package org.springframework.scheduling.support;

import java.lang.reflect.Method;

import io.micrometer.observation.Observation;

import org.springframework.util.ClassUtils;

/**
 * 在{@link ScheduledTaskObservationDocumentation#TASKS_SCHEDULED_EXECUTION 调度任务执行}
 * 期间保存观测元数据收集信息的上下文。
 *
 * @author Brian Clozel
 * @since 6.1
 */
public class ScheduledTaskObservationContext extends Observation.Context {

	private final Class<?> targetClass;

	private final Method method;

	private boolean complete;


	/**
	 * 根据目标对象与待调用方法，为任务创建新的观测上下文。
	 * @param target 任务执行时调用的目标对象
	 * @param method 任务执行时调用的方法
	 */
	public ScheduledTaskObservationContext(Object target, Method method) {
		this.targetClass = ClassUtils.getUserClass(target);
		this.method = method;
	}


	/**
	 * 返回目标对象的类型。
	 */
	public Class<?> getTargetClass() {
		return this.targetClass;
	}

	/**
	 * 返回任务执行时调用的方法。
	 */
	public Method getMethod() {
		return this.method;
	}

	/**
	 * 返回任务执行是否已完成。
	 * <p>若观测已结束而任务未完成，表示执行期间发生了
	 * {@link #getError() 错误}或任务被取消。
	 */
	public boolean isComplete() {
		return this.complete;
	}

	/**
	 * 设置任务执行是否已完成。
	 */
	public void setComplete(boolean complete) {
		this.complete = complete;
	}

}
