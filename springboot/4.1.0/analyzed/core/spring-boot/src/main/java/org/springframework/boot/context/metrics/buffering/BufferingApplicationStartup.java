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

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.metrics.buffering.StartupTimeline.TimelineEvent;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;
import org.springframework.util.Assert;

/**
 * 缓冲 {@link StartupStep 启动步骤} 并记录时间戳与处理时间的 {@link ApplicationStartup} 实现。
 * <p>
 * {@link #startRecording() 开始录制} 后，步骤会缓冲至配置的
 * {@link #BufferingApplicationStartup(int) 容量}；超出后不再记录新步骤。
 * <p>
 * 保持缓冲区较小的方式：
 * <ul>
 * <li>配置 {@link #addFilter(Predicate) 过滤器}，仅记录相关步骤。</li>
 * <li>{@link #drainBufferedTimeline() 排空} 已缓冲步骤。</li>
 * </ul>
 *
 * @author Brian Clozel
 * @author Phillip Webb
 * @since 2.4.0
 */
public class BufferingApplicationStartup implements ApplicationStartup {

	private final int capacity;

	private final Clock clock;

	private Instant startTime;

	private final AtomicInteger idSeq = new AtomicInteger();

	private Predicate<StartupStep> filter = (step) -> true;

	private final AtomicReference<@Nullable BufferedStartupStep> current = new AtomicReference<>();

	private final AtomicInteger estimatedSize = new AtomicInteger();

	private final ConcurrentLinkedQueue<TimelineEvent> events = new ConcurrentLinkedQueue<>();

	/**
	 * 创建容量受限的缓冲 {@link ApplicationStartup} 并开始录制步骤。
	 *
	 * @param capacity 配置的容量；达到后不再记录新步骤
	 */
	public BufferingApplicationStartup(int capacity) {
		this(capacity, Clock.systemDefaultZone());
	}

	BufferingApplicationStartup(int capacity, Clock clock) {
		this.capacity = capacity;
		this.clock = clock;
		this.startTime = clock.instant();
	}

	/**
	 * 开始录制步骤并标记 {@link StartupTimeline} 的起点。
	 * 构造器已隐式调用此方法，但在尚未录制步骤时可重置。
	 *
	 * @throws IllegalStateException 若已录制 {@link StartupStep} 后再次调用
	 */
	public void startRecording() {
		Assert.state(this.events.isEmpty(), "Cannot restart recording once steps have been buffered");
		this.startTime = this.clock.instant();
	}

	/**
	 * 向现有过滤器列表添加谓词过滤器。
	 * <p>
	 * 不匹配所有过滤器的 {@link StartupStep 步骤} 不会被记录。
	 *
	 * @param filter 要添加的谓词过滤器
	 */
	public void addFilter(Predicate<StartupStep> filter) {
		this.filter = this.filter.and(filter);
	}

	@Override
	public StartupStep start(String name) {
		int id = this.idSeq.getAndIncrement();
		Instant start = this.clock.instant();
		while (true) {
			BufferedStartupStep current = this.current.get();
			BufferedStartupStep parent = getLatestActive(current);
			BufferedStartupStep next = new BufferedStartupStep(parent, name, id, start, this::record);
			if (this.current.compareAndSet(current, next)) {
				return next;
			}
		}
	}

	private void record(BufferedStartupStep step) {
		if (this.filter.test(step) && this.estimatedSize.get() < this.capacity) {
			this.estimatedSize.incrementAndGet();
			this.events.add(new TimelineEvent(step, this.clock.instant()));
		}
		while (true) {
			BufferedStartupStep current = this.current.get();
			BufferedStartupStep next = getLatestActive(current);
			if (this.current.compareAndSet(current, next)) {
				return;
			}
		}
	}

	private @Nullable BufferedStartupStep getLatestActive(@Nullable BufferedStartupStep step) {
		while (step != null && step.isEnded()) {
			step = step.getParent();
		}
		return step;
	}

	/**
	 * 以当前缓冲步骤的快照形式返回 {@link StartupTimeline 时间线}。
	 * <p>
	 * 不会从缓冲区移除步骤；对应的可移除操作见 {@link #drainBufferedTimeline()}。
	 *
	 * @return 当前缓冲步骤的快照
	 */
	public StartupTimeline getBufferedTimeline() {
		return new StartupTimeline(this.startTime, new ArrayList<>(this.events));
	}

	/**
	 * 从缓冲区取出步骤并返回 {@link StartupTimeline 时间线}。
	 * <p>
	 * 会从缓冲区移除步骤；只读对应方法见 {@link #getBufferedTimeline()}。
	 *
	 * @return 从缓冲区排空后的步骤
	 */
	public StartupTimeline drainBufferedTimeline() {
		List<TimelineEvent> events = new ArrayList<>();
		Iterator<TimelineEvent> iterator = this.events.iterator();
		while (iterator.hasNext()) {
			events.add(iterator.next());
			iterator.remove();
		}
		this.estimatedSize.set(0);
		return new StartupTimeline(this.startTime, events);
	}

}
