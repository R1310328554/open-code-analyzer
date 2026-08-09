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

package org.springframework.boot.logging.logback;

import java.io.PrintStream;
import java.util.List;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.BasicStatusManager;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.util.StatusListenerConfigHelper;

/**
 * 用于将适当的状态消息打印到 {@link System#out} 或 {@link System#err} 的
 * {@link StatusListener}。
 * 继承 {@link OnConsoleStatusListener}，避免 {@link BasicStatusManager#add(StatusListener)}
 * 重复添加同一监听器；并实现可按级别过滤的 retrospectivePrint。
 * 非 debug 模式下仅输出 WARN 及以上级别到 {@link System#err}。
 *
 * @author Dmytro Nosan
 * @author Phillip Webb
 */
final class SystemStatusListener extends OnConsoleStatusListener {

	private static final long RETROSPECTIVE_THRESHOLD = 300;

	private final boolean debug;

	private SystemStatusListener(boolean debug) {
		this.debug = debug;
		setResetResistant(false);
		setRetrospective(0);
	}

	@Override
	public void start() {
		super.start();
		retrospectivePrint();
	}

	private void retrospectivePrint() {
		if (this.context == null) {
			return;
		}
		long now = System.currentTimeMillis();
		List<Status> statusList = this.context.getStatusManager().getCopyOfStatusList();
		statusList.stream()
			.filter((status) -> getElapsedTime(status, now) < RETROSPECTIVE_THRESHOLD)
			.forEach(this::addStatusEvent);
	}

	@Override
	public void addStatusEvent(Status status) {
		if (this.debug || status.getLevel() >= Status.WARN) {
			super.addStatusEvent(status);
		}
	}

	@Override
	protected PrintStream getPrintStream() {
		return (!this.debug) ? System.err : System.out;
	}

	private static long getElapsedTime(Status status, long now) {
		return now - status.getTimestamp();
	}

	static void addTo(LoggerContext loggerContext) {
		addTo(loggerContext, false);
	}

	static void addTo(LoggerContext loggerContext, boolean debug) {
		StatusListenerConfigHelper.addOnConsoleListenerInstance(loggerContext, new SystemStatusListener(debug));
	}

}
