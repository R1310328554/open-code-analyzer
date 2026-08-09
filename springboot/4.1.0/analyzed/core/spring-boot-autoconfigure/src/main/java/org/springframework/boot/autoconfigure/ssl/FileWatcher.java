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

package org.springframework.boot.autoconfigure.ssl;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.core.log.LogMessage;
import org.springframework.util.Assert;

/**
 * 监视文件与目录变更并在检测到变化后触发回调。
 * <p>
 * 基于 {@link WatchService} 实现，支持静默期（quiet period）防抖，
 * 用于 SSL bundle 证书/密钥文件的热重载。
 *
 * @author Moritz Halbritter
 * @author Phillip Webb
 */
class FileWatcher implements Closeable {

	private static final Log logger = LogFactory.getLog(FileWatcher.class);

	private final Duration quietPeriod;

	private final Object lock = new Object();

	private @Nullable WatcherThread thread;

	/**
	 * 创建新的 {@link FileWatcher} 实例。
	 * @param quietPeriod 触发动作前需无文件变更的静默时长
	 */
	FileWatcher(Duration quietPeriod) {
		Assert.notNull(quietPeriod, "'quietPeriod' must not be null");
		this.quietPeriod = quietPeriod;
	}

	/**
	 * 监视给定文件或目录的变更。
	 * @param paths 要监视的文件或目录
	 * @param action 检测到变更时执行的动作
	 */
	void watch(Set<Path> paths, Runnable action) {
		Assert.notNull(paths, "'paths' must not be null");
		Assert.notNull(action, "'action' must not be null");
		if (paths.isEmpty()) {
			return;
		}
		synchronized (this.lock) {
			try {
				if (this.thread == null) {
					this.thread = new WatcherThread();
					this.thread.start();
				}
				this.thread.register(new Registration(getRegistrationPaths(paths), action));
			}
			catch (IOException ex) {
				throw new UncheckedIOException("Failed to register paths for watching: " + paths, ex);
			}
		}
	}

	/**
	 * 获取指定 {@link Path} 应注册监视的全部 {@link Path 路径}。
	 * <p>
	 * 若路径为符号链接，需监视链接本身及其解析链路上的相关路径，
	 * 而不仅是链接最终指向的文件。例如下列目录结构中 {@code keystore.jks}：<pre>
	 * +- stores
	 * |  +─ keystore.jks
	 * +- <em>data</em> -&gt; stores
	 * +─ <em>keystore.jks</em> -&gt; data/keystore.jks
	 * </pre> 结果路径包括：
	 * <p>
	 * <ul>
	 * <li>{@code keystore.jks}</li>
	 * <li>{@code data/keystore.jks}</li>
	 * <li>{@code data}</li>
	 * <li>{@code stores/keystore.jks}</li>
	 * </ul>
	 * @param paths 源路径集合
	 * @return 应注册的全部 {@link Path} 实例
	 * @throws IOException I/O 错误时抛出
	 */
	private Set<Path> getRegistrationPaths(Set<Path> paths) throws IOException {
		Set<Path> result = new HashSet<>();
		for (Path path : paths) {
			collectRegistrationPaths(path, result);
		}
		return Collections.unmodifiableSet(result);
	}

	private void collectRegistrationPaths(Path path, Set<Path> result) throws IOException {
		path = path.toAbsolutePath();
		result.add(path);
		Path parent = path.getParent();
		if (parent != null && Files.isSymbolicLink(parent)) {
			result.add(parent);
			collectRegistrationPaths(resolveSiblingSymbolicLink(parent).resolve(path.getFileName()), result);
		}
		else if (Files.isSymbolicLink(path)) {
			collectRegistrationPaths(resolveSiblingSymbolicLink(path), result);
		}
	}

	private Path resolveSiblingSymbolicLink(Path path) throws IOException {
		return path.resolveSibling(Files.readSymbolicLink(path));
	}

	@Override
	public void close() throws IOException {
		synchronized (this.lock) {
			if (this.thread != null) {
				this.thread.close();
				this.thread.interrupt();
				try {
					this.thread.join();
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				this.thread = null;
			}
		}
	}

	/**
	 * 用于检测文件变更的监视线程。
	 */
	private class WatcherThread extends Thread implements Closeable {

		private final WatchService watchService = FileSystems.getDefault().newWatchService();

		private final Map<WatchKey, List<Registration>> registrations = new ConcurrentHashMap<>();

		private volatile boolean running = true;

		WatcherThread() throws IOException {
			setName("ssl-bundle-watcher");
			setDaemon(true);
			setUncaughtExceptionHandler(this::onThreadException);
		}

		private void onThreadException(Thread thread, Throwable throwable) {
			logger.error("Uncaught exception in file watcher thread", throwable);
		}

		void register(Registration registration) throws IOException {
			Set<Path> directories = new HashSet<>();
			for (Path path : registration.paths()) {
				if (!Files.isRegularFile(path) && !Files.isDirectory(path)) {
					throw new IOException("'%s' is neither a file nor a directory".formatted(path));
				}
				Path directory = Files.isDirectory(path) ? path : path.getParent();
				directories.add(directory);
			}
			for (Path directory : directories) {
				WatchKey watchKey = register(directory);
				this.registrations.computeIfAbsent(watchKey, (key) -> new CopyOnWriteArrayList<>()).add(registration);
			}
		}

		private WatchKey register(Path directory) throws IOException {
			logger.debug(LogMessage.format("Registering '%s'", directory));
			return directory.register(this.watchService, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
		}

		@Override
		public void run() {
			logger.debug("Watch thread started");
			Set<Runnable> actions = new HashSet<>();
			while (this.running) {
				try {
					long timeout = FileWatcher.this.quietPeriod.toMillis();
					WatchKey key = this.watchService.poll(timeout, TimeUnit.MILLISECONDS);
					if (key == null) {
						actions.forEach(this::runSafely);
						actions.clear();
					}
					else {
						accumulate(key, actions);
						key.reset();
					}
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				catch (ClosedWatchServiceException ex) {
					logger.debug("File watcher has been closed");
					this.running = false;
				}
			}
			logger.debug("Watch thread stopped");
		}

		private void runSafely(Runnable action) {
			try {
				action.run();
			}
			catch (Throwable ex) {
				logger.error("Unexpected SSL reload error", ex);
			}
		}

		private void accumulate(WatchKey key, Set<Runnable> actions) {
			List<Registration> registrations = this.registrations.get(key);
			Path directory = (Path) key.watchable();
			for (WatchEvent<?> event : key.pollEvents()) {
				Path file = directory.resolve((Path) event.context());
				Assert.state(registrations != null, "'registrations' must not be null");
				for (Registration registration : registrations) {
					if (registration.manages(file)) {
						actions.add(registration.action());
					}
				}
			}
		}

		@Override
		public void close() throws IOException {
			this.running = false;
			this.watchService.close();
		}

	}

	/**
	 * 单次监视注册。
	 *
	 * @param paths 已注册的路径
	 * @param action 变更时执行的动作
	 */
	private record Registration(Set<Path> paths, Runnable action) {

		boolean manages(Path file) {
			Path absolutePath = file.toAbsolutePath();
			return this.paths.contains(absolutePath) || isInDirectories(absolutePath);
		}

		private boolean isInDirectories(Path file) {
			return this.paths.stream().filter(Files::isDirectory).anyMatch(file::startsWith);
		}

	}

}
