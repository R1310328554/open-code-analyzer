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

package org.springframework.boot.context;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.boot.system.ApplicationPid;
import org.springframework.boot.system.SystemProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * 将应用 PID 写入文件的 {@link ApplicationListener}。
 * 每个 JVM 仅触发一次；文件名可在运行时通过名为 {@code PIDFILE}（或 {@code pidfile}）
 * 的系统属性或环境变量，或 Spring {@link Environment} 中的 {@code spring.pid.file} 属性覆盖。
 * <p>
 * 若无法创建 PID 文件则不报告异常。可通过将系统属性或环境变量
 * {@code PID_FAIL_ON_WRITE_ERROR}（或 {@code pid_fail_on_write_error}）或
 * Spring {@link Environment} 中的 {@code spring.pid.fail-on-write-error} 设为 {@code true} 更改此行为。
 * <p>
 * 注意：仅当 {@link #setTriggerEventType(Class) triggerEventType} 设为
 * {@link ApplicationEnvironmentPreparedEvent}、{@link ApplicationReadyEvent} 或
 * {@link ApplicationPreparedEvent} 时才能访问 Spring {@link Environment}。
 *
 * @author Jakub Kubrynski
 * @author Dave Syer
 * @author Phillip Webb
 * @author Tomasz Przybyla
 * @author Madhura Bhave
 * @since 2.0.0
 */
public class ApplicationPidFileWriter implements ApplicationListener<SpringApplicationEvent>, Ordered {

	private static final Log logger = LogFactory.getLog(ApplicationPidFileWriter.class);

	private static final String DEFAULT_FILE_NAME = "application.pid";

	private static final List<Property> FILE_PROPERTIES;

	static {
		List<Property> properties = new ArrayList<>();
		properties.add(new SpringProperty("spring.pid.", "file"));
		properties.add(new SpringProperty("spring.", "pidfile"));
		properties.add(new SystemProperty("PIDFILE"));
		FILE_PROPERTIES = Collections.unmodifiableList(properties);
	}

	private static final List<Property> FAIL_ON_WRITE_ERROR_PROPERTIES;

	static {
		List<Property> properties = new ArrayList<>();
		properties.add(new SpringProperty("spring.pid.", "fail-on-write-error"));
		properties.add(new SystemProperty("PID_FAIL_ON_WRITE_ERROR"));
		FAIL_ON_WRITE_ERROR_PROPERTIES = Collections.unmodifiableList(properties);
	}

	private static final AtomicBoolean created = new AtomicBoolean();

	private int order = Ordered.HIGHEST_PRECEDENCE + 13;

	private final File file;

	private Class<? extends SpringApplicationEvent> triggerEventType = ApplicationPreparedEvent.class;

	/**
	 * 使用文件名 {@code application.pid} 创建新的 {@link ApplicationPidFileWriter} 实例。
	 */
	public ApplicationPidFileWriter() {
		this(new File(DEFAULT_FILE_NAME));
	}

	/**
	 * 使用指定文件名创建新的 {@link ApplicationPidFileWriter} 实例。
	 *
	 * @param filename 包含 PID 的文件名
	 */
	public ApplicationPidFileWriter(String filename) {
		this(new File(filename));
	}

	/**
	 * 使用指定文件创建新的 {@link ApplicationPidFileWriter} 实例。
	 *
	 * @param file 包含 PID 的文件
	 */
	public ApplicationPidFileWriter(File file) {
		Assert.notNull(file, "'file' must not be null");
		this.file = file;
	}

	/**
	 * 设置触发写入 PID 文件的应用事件类型，默认为 {@link ApplicationPreparedEvent}。
	 * 注意：若使用 {@link org.springframework.boot.context.event.ApplicationStartingEvent}
	 * 触发写入，则无法在 Spring {@link Environment} 中指定 PID 文件名。
	 *
	 * @param triggerEventType 触发事件类型
	 */
	public void setTriggerEventType(Class<? extends SpringApplicationEvent> triggerEventType) {
		Assert.notNull(triggerEventType, "'triggerEventType' must not be null");
		this.triggerEventType = triggerEventType;
	}

	@Override
	public void onApplicationEvent(SpringApplicationEvent event) {
		if (this.triggerEventType.isInstance(event) && created.compareAndSet(false, true)) {
			try {
				writePidFile(event);
			}
			catch (Exception ex) {
				String message = String.format("Cannot create pid file %s", this.file);
				if (failOnWriteError(event)) {
					throw new IllegalStateException(message, ex);
				}
				logger.warn(message, ex);
			}
		}
	}

	private void writePidFile(SpringApplicationEvent event) throws IOException {
		File pidFile = this.file;
		String override = getProperty(event, FILE_PROPERTIES);
		if (override != null) {
			pidFile = new File(override);
		}
		new ApplicationPid().write(pidFile);
		pidFile.deleteOnExit();
	}

	private boolean failOnWriteError(SpringApplicationEvent event) {
		String value = getProperty(event, FAIL_ON_WRITE_ERROR_PROPERTIES);
		return Boolean.parseBoolean(value);
	}

	private @Nullable String getProperty(SpringApplicationEvent event, List<Property> candidates) {
		for (Property candidate : candidates) {
			String value = candidate.getValue(event);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * 重置 created 标志，供测试使用。
	 */
	protected static void reset() {
		created.set(false);
	}

	/**
	 * 提供对属性值的访问。
	 */
	private interface Property {

		@Nullable String getValue(SpringApplicationEvent event);

	}

	/**
	 * 从 Spring {@link Environment} 获取的 {@link Property}。
	 */
	private static class SpringProperty implements Property {

		private final String prefix;

		private final String key;

		SpringProperty(String prefix, String key) {
			this.prefix = prefix;
			this.key = key;
		}

		@Override
		public @Nullable String getValue(SpringApplicationEvent event) {
			Environment environment = getEnvironment(event);
			if (environment == null) {
				return null;
			}
			return environment.getProperty(this.prefix + this.key);
		}

		private @Nullable Environment getEnvironment(SpringApplicationEvent event) {
			if (event instanceof ApplicationEnvironmentPreparedEvent environmentPreparedEvent) {
				return environmentPreparedEvent.getEnvironment();
			}
			if (event instanceof ApplicationPreparedEvent preparedEvent) {
				return preparedEvent.getApplicationContext().getEnvironment();
			}
			if (event instanceof ApplicationReadyEvent readyEvent) {
				return readyEvent.getApplicationContext().getEnvironment();
			}
			return null;
		}

	}

	/**
	 * 从 {@link SystemProperties} 获取的 {@link Property}。
	 */
	private static class SystemProperty implements Property {

		private final String[] properties;

		SystemProperty(String name) {
			this.properties = new String[] { name.toUpperCase(Locale.ENGLISH), name.toLowerCase(Locale.ENGLISH) };
		}

		@Override
		public @Nullable String getValue(SpringApplicationEvent event) {
			return SystemProperties.get(this.properties);
		}

	}

}
