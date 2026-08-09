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

package org.springframework.boot.autoconfigure.preinitialize;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationListener;
import org.springframework.core.NativeDetector;
import org.springframework.core.Ordered;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * 在后台线程中触发耗时任务早期初始化的 {@link ApplicationListener}。
 * <p>
 * 将 {@link #IGNORE_BACKGROUNDPREINITIALIZER_PROPERTY_NAME} 系统属性设为 {@code true} 可禁用此机制。
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Artsiom Yudovin
 * @author Sebastien Deleuze
 * @see BackgroundPreinitializer
 */
class BackgroundPreinitializingApplicationListener implements ApplicationListener<SpringApplicationEvent>, Ordered {

	/**
	 * 指示 Spring Boot 如何运行预初始化的系统属性。设为 {@code true} 时不进行预初始化，
	 * 各项在需要时于前台初始化；为 {@code false}（默认）时，预初始化在后台独立线程中运行。
	 */
	public static final String IGNORE_BACKGROUNDPREINITIALIZER_PROPERTY_NAME = "spring.backgroundpreinitializer.ignore";

	private static final AtomicBoolean started = new AtomicBoolean();

	private static final CountDownLatch complete = new CountDownLatch(1);

	private final SpringFactoriesLoader factoriesLoader;

	private final boolean enabled;

	BackgroundPreinitializingApplicationListener() {
		this(SpringFactoriesLoader.forDefaultResourceLocation());
	}

	BackgroundPreinitializingApplicationListener(SpringFactoriesLoader factoriesLoader) {
		this.factoriesLoader = factoriesLoader;
		this.enabled = !NativeDetector.inNativeImage()
				&& !Boolean.getBoolean(IGNORE_BACKGROUNDPREINITIALIZER_PROPERTY_NAME)
				&& Runtime.getRuntime().availableProcessors() > 1;
	}

	@Override
	public int getOrder() {
		return LoggingApplicationListener.DEFAULT_ORDER + 1;
	}

	@Override
	public void onApplicationEvent(SpringApplicationEvent event) {
		if (!this.enabled) {
			return;
		}
		if (event instanceof ApplicationEnvironmentPreparedEvent && started.compareAndSet(false, true)) {
			preinitialize();
		}
		if ((event instanceof ApplicationReadyEvent || event instanceof ApplicationFailedEvent) && started.get()) {
			try {
				complete.await();
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private void preinitialize() {
		Runner runner = new Runner(this.factoriesLoader.load(BackgroundPreinitializer.class));
		try {
			Thread thread = new Thread(runner, "background-preinit");
			thread.start();
		}
		catch (Exception ex) {
			// This will fail on Google App Engine where creating threads is
			// prohibited. We can safely continue but startup will be slightly slower
			// as the initialization will now happen on the main thread.
			complete.countDown();
		}
	}

	/**
	 * 调用 {@link BackgroundPreinitializer} 实例的运行器线程。
	 *
	 * @param preinitializers 预初始化器列表
	 */
	record Runner(List<BackgroundPreinitializer> preinitializers) implements Runnable {

		@Override
		public void run() {
			for (BackgroundPreinitializer preinitializer : this.preinitializers) {
				try {
					preinitializer.preinitialize();
				}
				catch (Throwable ex) {
				}
			}
			complete.countDown();
		}

	}

}
