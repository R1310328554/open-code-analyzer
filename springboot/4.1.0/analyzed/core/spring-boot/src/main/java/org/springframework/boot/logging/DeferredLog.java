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

package org.springframework.boot.logging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 延迟 {@link Log}，用于暂存日志系统完全初始化前不应输出的消息。
 *
 * @author Phillip Webb
 * @since 1.3.0
 */
public class DeferredLog implements Log {

	private @Nullable Log destination;

	private final @Nullable Supplier<Log> destinationSupplier;

	private final Lines lines;

	/**
	 * 创建新的 {@link DeferredLog} 实例。
	 */
	public DeferredLog() {
		this.destinationSupplier = null;
		this.lines = new Lines();
	}

	/**
	 * 由 {@link DeferredLogFactory} 管理的 {@link DeferredLog} 实例。
	 *
	 * @param destination 切换后的目标日志
	 * @param lines 所有相关延迟日志共享的行缓冲
	 * @since 2.4.0
	 */
	DeferredLog(Supplier<Log> destination, Lines lines) {
		Assert.notNull(destination, "'destination' must not be null");
		this.destinationSupplier = destination;
		this.lines = lines;
	}

	@Override
	public boolean isTraceEnabled() {
		synchronized (this.lines) {
			return (this.destination == null) || this.destination.isTraceEnabled();
		}
	}

	@Override
	public boolean isDebugEnabled() {
		synchronized (this.lines) {
			return (this.destination == null) || this.destination.isDebugEnabled();
		}
	}

	@Override
	public boolean isInfoEnabled() {
		synchronized (this.lines) {
			return (this.destination == null) || this.destination.isInfoEnabled();
		}
	}

	@Override
	public boolean isWarnEnabled() {
		synchronized (this.lines) {
			return (this.destination == null) || this.destination.isWarnEnabled();
		}
	}

	@Override
	public boolean isErrorEnabled() {
		synchronized (this.lines) {
			return (this.destination == null) || this.destination.isErrorEnabled();
		}
	}

	@Override
	public boolean isFatalEnabled() {
		synchronized (this.lines) {
			return (this.destination == null) || this.destination.isFatalEnabled();
		}
	}

	@Override
	public void trace(Object message) {
		log(LogLevel.TRACE, message, null);
	}

	@Override
	public void trace(Object message, @Nullable Throwable t) {
		log(LogLevel.TRACE, message, t);
	}

	@Override
	public void debug(Object message) {
		log(LogLevel.DEBUG, message, null);
	}

	@Override
	public void debug(Object message, @Nullable Throwable t) {
		log(LogLevel.DEBUG, message, t);
	}

	@Override
	public void info(Object message) {
		log(LogLevel.INFO, message, null);
	}

	@Override
	public void info(Object message, @Nullable Throwable t) {
		log(LogLevel.INFO, message, t);
	}

	@Override
	public void warn(Object message) {
		log(LogLevel.WARN, message, null);
	}

	@Override
	public void warn(Object message, @Nullable Throwable t) {
		log(LogLevel.WARN, message, t);
	}

	@Override
	public void error(Object message) {
		log(LogLevel.ERROR, message, null);
	}

	@Override
	public void error(Object message, @Nullable Throwable t) {
		log(LogLevel.ERROR, message, t);
	}

	@Override
	public void fatal(Object message) {
		log(LogLevel.FATAL, message, null);
	}

	@Override
	public void fatal(Object message, @Nullable Throwable t) {
		log(LogLevel.FATAL, message, t);
	}

	private void log(LogLevel level, Object message, @Nullable Throwable t) {
		synchronized (this.lines) {
			if (this.destination != null) {
				level.log(this.destination, message, t);
			}
			else {
				this.lines.add(this.destinationSupplier, level, message, t);
			}
		}
	}

	void switchOver() {
		synchronized (this.lines) {
			Assert.state(this.destinationSupplier != null, "destinationSupplier hasn't been set");
			this.destination = this.destinationSupplier.get();
		}
	}

	/**
	 * 从延迟日志切换到指定目标的即时日志。
	 *
	 * @param destination 新的日志目标类
	 * @since 2.1.0
	 */
	public void switchTo(Class<?> destination) {
		switchTo(LogFactory.getLog(destination));
	}

	/**
	 * 从延迟日志切换到指定目标的即时日志。
	 *
	 * @param destination 新的日志目标
	 * @since 2.1.0
	 */
	public void switchTo(Log destination) {
		synchronized (this.lines) {
			replayTo(destination);
			this.destination = destination;
		}
	}

	/**
	 * 将延迟日志重放到指定目标。
	 *
	 * @param destination 延迟消息的目标日志类
	 */
	public void replayTo(Class<?> destination) {
		replayTo(LogFactory.getLog(destination));
	}

	/**
	 * 将延迟日志重放到指定目标。
	 *
	 * @param destination 延迟消息的目标日志
	 */
	public void replayTo(Log destination) {
		synchronized (this.lines) {
			for (Line line : this.lines) {
				line.getLevel().log(destination, line.getMessage(), line.getThrowable());
			}
			this.lines.clear();
		}
	}

	/**
	 * 当源为延迟日志时，将消息重放到目标日志。
	 *
	 * @param source 源 Logger
	 * @param destination 目标 Logger 类
	 * @return the destination 目标 Logger
	 */
	public static Log replay(Log source, Class<?> destination) {
		return replay(source, LogFactory.getLog(destination));
	}

	/**
	 * 当源为延迟日志时，将消息重放到目标日志。
	 *
	 * @param source 源 Logger
	 * @param destination 目标 Logger
	 * @return the destination 目标 Logger
	 */
	public static Log replay(Log source, Log destination) {
		if (source instanceof DeferredLog deferredLog) {
			deferredLog.replayTo(destination);
		}
		return destination;
	}

	static class Lines implements Iterable<Line> {

		private final List<Line> lines = new ArrayList<>();

		void add(@Nullable Supplier<Log> destinationSupplier, LogLevel level, Object message,
				@Nullable Throwable throwable) {
			this.lines.add(new Line(destinationSupplier, level, message, throwable));
		}

		void clear() {
			this.lines.clear();
		}

		@Override
		public Iterator<Line> iterator() {
			return this.lines.iterator();
		}

	}

	static class Line {

		private final @Nullable Supplier<Log> destinationSupplier;

		private final LogLevel level;

		private final Object message;

		private final @Nullable Throwable throwable;

		Line(@Nullable Supplier<Log> destinationSupplier, LogLevel level, Object message,
				@Nullable Throwable throwable) {
			this.destinationSupplier = destinationSupplier;
			this.level = level;
			this.message = message;
			this.throwable = throwable;
		}

		Log getDestination() {
			Assert.state(this.destinationSupplier != null, "destinationSupplier hasn't been set");
			return this.destinationSupplier.get();
		}

		LogLevel getLevel() {
			return this.level;
		}

		Object getMessage() {
			return this.message;
		}

		@Nullable Throwable getThrowable() {
			return this.throwable;
		}

	}

}
