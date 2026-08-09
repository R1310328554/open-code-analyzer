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

package org.springframework.scheduling;

import java.time.Instant;
import java.util.Date;

import org.jspecify.annotations.Nullable;

/**
 * 确定所关联任务下次执行时间的触发器对象通用接口。
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see TaskScheduler#schedule(Runnable, Trigger)
 * @see org.springframework.scheduling.support.CronTrigger
 */
public interface Trigger {

	/**
	 * 根据给定触发器上下文确定下次执行时间。
	 * <p>默认实现委托给 {@link #nextExecution(TriggerContext)}。
	 * @param triggerContext 封装上次执行时间与上次完成时间的上下文对象
	 * @return 触发器定义的下次执行时间，
	 * 若触发器不再触发则返回 {@code null}
	 * @deprecated 自 6.0 起，请改用 {@link #nextExecution(TriggerContext)}
	 */
	@Deprecated(since = "6.0")
	default @Nullable Date nextExecutionTime(TriggerContext triggerContext) {
		Instant instant = nextExecution(triggerContext);
		return (instant != null ? Date.from(instant) : null);
	}

	/**
	 * 根据给定触发器上下文确定下次执行时间。
	 * @param triggerContext 封装上次执行时间与上次完成时间的上下文对象
	 * @return 触发器定义的下次执行时间，
	 * 若触发器不再触发则返回 {@code null}
	 * @since 6.0
	 */
	@Nullable Instant nextExecution(TriggerContext triggerContext);

}
