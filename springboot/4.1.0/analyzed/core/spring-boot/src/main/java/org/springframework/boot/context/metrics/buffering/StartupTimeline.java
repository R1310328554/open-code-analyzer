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

package org.springframework.boot.context.metrics.buffering;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.core.metrics.StartupStep;

/**
 * 表示 {@link BufferingApplicationStartup} 录制的 {@link StartupStep 步骤} 时间线。
 * 每个 {@link TimelineEvent} 具有起止时间及纳秒精度的持续时间。
 *
 * @author Brian Clozel
 * @since 2.4.0
 */
public class StartupTimeline {

	private final Instant startTime;

	private final List<TimelineEvent> events;

	StartupTimeline(Instant startTime, List<TimelineEvent> events) {
		this.startTime = startTime;
		this.events = Collections.unmodifiableList(events);
	}

	/**
	 * 返回此时间线的开始时间。
	 *
	 * @return 开始时间
	 */
	public Instant getStartTime() {
		return this.startTime;
	}

	/**
	 * 返回已录制的事件。
	 *
	 * @return 事件列表
	 */
	public List<TimelineEvent> getEvents() {
		return this.events;
	}

	/**
	 * 当前 {@link StartupTimeline} 上的事件。
	 * 每个事件具有起止时间、精确持续时间及关联的完整 {@link StartupStep} 信息。
	 */
	public static class TimelineEvent {

		private final BufferedStartupStep step;

		private final Instant endTime;

		private final Duration duration;

		TimelineEvent(BufferedStartupStep step, Instant endTime) {
			this.step = step;
			this.endTime = endTime;
			this.duration = Duration.between(step.getStartTime(), endTime);
		}

		/**
		 * 返回此事件的开始时间。
		 *
		 * @return 开始时间
		 */
		public Instant getStartTime() {
			return this.step.getStartTime();
		}

		/**
		 * 返回此事件的结束时间。
		 *
		 * @return 结束时间
		 */
		public Instant getEndTime() {
			return this.endTime;
		}

		/**
		 * 返回此事件的持续时间，即关联 {@link StartupStep} 的处理时间（纳秒精度）。
		 *
		 * @return 事件持续时间
		 */
		public Duration getDuration() {
			return this.duration;
		}

		/**
		 * 返回此事件对应的 {@link StartupStep} 信息。
		 *
		 * @return 步骤信息
		 */
		public StartupStep getStartupStep() {
			return this.step;
		}

	}

}
