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

package org.springframework.boot.info;

import java.lang.ProcessHandle.Info;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.PlatformManagedObject;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.Nullable;

import org.springframework.util.ClassUtils;

/**
 * 应用进程的信息。
 *
 * @author Jonatan Ivanov
 * @author Andrey Litvitski
 * @since 3.3.0
 */
public class ProcessInfo {

	private static final String VIRTUAL_THREAD_SCHEDULER_CLASS = "jdk.management.VirtualThreadSchedulerMXBean";

	private static final boolean VIRTUAL_THREAD_SCHEDULER_CLASS_PRESENT = ClassUtils
		.isPresent(VIRTUAL_THREAD_SCHEDULER_CLASS, null);

	private static final Runtime runtime = Runtime.getRuntime();

	private final long pid;

	private final long parentPid;

	private final @Nullable String owner;

	private final @Nullable Instant startTime;

	private final ZoneId timezone;

	private final Locale locale;

	private final String workingDirectory;

	public ProcessInfo() {
		ProcessHandle process = ProcessHandle.current();
		this.pid = process.pid();
		this.parentPid = process.parent().map(ProcessHandle::pid).orElse(-1L);
		this.owner = process.info().user().orElse(null);
		this.startTime = process.info().startInstant().orElse(null);
		this.timezone = ZoneId.systemDefault();
		this.locale = Locale.getDefault();
		this.workingDirectory = Path.of(".").toAbsolutePath().normalize().toString();
	}

	/**
	 * 进程可用的处理器数量。该值可能在多次调用间变化，
	 * 尤其在可通过 cgroup 等隔离资源的（容器化）环境中。
	 *
	 * @return {@link Runtime#availableProcessors()} 的结果
	 * @see Runtime#availableProcessors()
	 */
	public int getCpus() {
		return runtime.availableProcessors();
	}

	/**
	 * 进程的内存信息。可提供当前内存使用与用户或 JVM ergonomics 所选限制
	 * （堆与非堆的 init、max、committed、used）的详情。若未显式设置限制，
	 * 运行时可能难以确定这些值，尤其在容器化或 cgroup 隔离环境中。
	 * 此外，这些值可指示 JVM 是否能调整堆大小（stop-the-world）。
	 *
	 * @return 堆与非堆内存信息
	 * @since 3.4.0
	 * @see MemoryMXBean#getHeapMemoryUsage()
	 * @see MemoryMXBean#getNonHeapMemoryUsage()
	 * @see MemoryUsage
	 */
	public MemoryInfo getMemory() {
		return new MemoryInfo();
	}

	/**
	 * 进程的虚拟线程信息，包括 mounted 线程数、排队线程数、并行度与线程池大小。
	 *
	 * @return 包含虚拟线程信息的 {@link VirtualThreadsInfo}，
	 * 若 VirtualThreadSchedulerMXBean 不可用则返回 {@code null}
	 * @since 3.5.0
	 */
	@SuppressWarnings("unchecked")
	public @Nullable VirtualThreadsInfo getVirtualThreads() {
		if (!VIRTUAL_THREAD_SCHEDULER_CLASS_PRESENT) {
			return null;
		}
		try {
			Class<PlatformManagedObject> mxbeanClass = (Class<PlatformManagedObject>) ClassUtils
				.forName(VIRTUAL_THREAD_SCHEDULER_CLASS, null);
			PlatformManagedObject mxbean = ManagementFactory.getPlatformMXBean(mxbeanClass);
			int mountedVirtualThreadCount = invokeMethod(mxbeanClass, mxbean, "getMountedVirtualThreadCount");
			long queuedVirtualThreadCount = invokeMethod(mxbeanClass, mxbean, "getQueuedVirtualThreadCount");
			int parallelism = invokeMethod(mxbeanClass, mxbean, "getParallelism");
			int poolSize = invokeMethod(mxbeanClass, mxbean, "getPoolSize");
			return new VirtualThreadsInfo(mountedVirtualThreadCount, queuedVirtualThreadCount, parallelism, poolSize);
		}
		catch (Exception ex) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T invokeMethod(Class<?> mxbeanClass, Object mxbean, String name) throws ReflectiveOperationException {
		Method method = mxbeanClass.getMethod(name);
		return (T) method.invoke(mxbean);
	}

	public long getPid() {
		return this.pid;
	}

	public long getParentPid() {
		return this.parentPid;
	}

	public @Nullable String getOwner() {
		return this.owner;
	}

	/**
	 * 进程运行时长。可用于查看进程已运行多久以及上次部署或重启距今多久。
	 *
	 * @return 自进程启动以来的时长，若不可用则返回 {@code null}
	 * @since 4.1.0
	 */
	public @Nullable Duration getUptime() {
		return (this.startTime != null) ? Duration.between(this.startTime, Instant.now()) : null;
	}

	/**
	 * 进程启动时刻。可用于查看进程何时启动以及上次部署或重启时间。
	 *
	 * @return 进程启动时间，若不可用则返回 {@code null}
	 * @since 4.1.0
	 * @see Info#startInstant()
	 */
	public @Nullable Instant getStartTime() {
		return this.startTime;
	}

	/**
	 * 进程当前时间。可用于检查是否存在时钟偏移以及进程所知当前时间是否足够准确。
	 *
	 * @return 进程当前时间
	 * @since 4.1.0
	 * @see Instant#now
	 */
	public Instant getCurrentTime() {
		return Instant.now();
	}

	/**
	 * 进程时区。有助于检测与时间、时区相关的问题。
	 *
	 * @return 进程时区
	 * @since 4.1.0
	 * @see ZoneId#systemDefault()
	 */
	public ZoneId getTimezone() {
		return this.timezone;
	}

	/**
	 * 进程 Locale。有助于检测与语言、国家/地区设置相关的问题。
	 *
	 * @return 进程 Locale
	 * @since 4.1.0
	 * @see Locale#getDefault()
	 */
	public Locale getLocale() {
		return this.locale;
	}

	/**
	 * 进程工作目录。有助于定位进程使用的文件。
	 *
	 * @return 进程工作目录的绝对路径
	 * @since 4.1.0
	 */
	public String getWorkingDirectory() {
		return this.workingDirectory;
	}

	/**
	 * 虚拟线程信息。
	 *
	 * @since 3.5.0
	 */
	public static class VirtualThreadsInfo {

		private final int mounted;

		private final long queued;

		private final int parallelism;

		private final int poolSize;

		VirtualThreadsInfo(int mounted, long queued, int parallelism, int poolSize) {
			this.mounted = mounted;
			this.queued = queued;
			this.parallelism = parallelism;
			this.poolSize = poolSize;
		}

		public int getMounted() {
			return this.mounted;
		}

		public long getQueued() {
			return this.queued;
		}

		public int getParallelism() {
			return this.parallelism;
		}

		public int getPoolSize() {
			return this.poolSize;
		}

	}

	/**
	 * 内存信息。
	 *
	 * @since 3.4.0
	 */
	public static class MemoryInfo {

		private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

		private static final List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory
			.getGarbageCollectorMXBeans();

		private final MemoryUsageInfo heap;

		private final MemoryUsageInfo nonHeap;

		private final List<GarbageCollectorInfo> garbageCollectors;

		MemoryInfo() {
			this.heap = new MemoryUsageInfo(memoryMXBean.getHeapMemoryUsage());
			this.nonHeap = new MemoryUsageInfo(memoryMXBean.getNonHeapMemoryUsage());
			this.garbageCollectors = garbageCollectorMXBeans.stream().map(GarbageCollectorInfo::new).toList();
		}

		public MemoryUsageInfo getHeap() {
			return this.heap;
		}

		public MemoryUsageInfo getNonHeap() {
			return this.nonHeap;
		}

		/**
		 * 进程的垃圾收集器信息。列出用户或 JVM ergonomics 所选当前 GC 算法。
		 * 所用 GC 算法通常取决于 {@link Runtime#availableProcessors()}（见 {@link ProcessInfo#getCpus()}）
		 * 与可用内存（见 {@link MemoryUsageInfo}），可能不易直接确定。
		 *
		 * @return {@link GarbageCollectorInfo} 的 {@link List}
		 * @since 3.5.0
		 */
		public List<GarbageCollectorInfo> getGarbageCollectors() {
			return this.garbageCollectors;
		}

		public static class MemoryUsageInfo {

			private final MemoryUsage memoryUsage;

			MemoryUsageInfo(MemoryUsage memoryUsage) {
				this.memoryUsage = memoryUsage;
			}

			public long getInit() {
				return this.memoryUsage.getInit();
			}

			public long getUsed() {
				return this.memoryUsage.getUsed();
			}

			public long getCommitted() {
				return this.memoryUsage.getCommitted();
			}

			public long getMax() {
				return this.memoryUsage.getMax();
			}

		}

		/**
		 * 垃圾收集信息。
		 *
		 * @since 3.5.0
		 */
		public static class GarbageCollectorInfo {

			private final String name;

			private final long collectionCount;

			GarbageCollectorInfo(GarbageCollectorMXBean garbageCollectorMXBean) {
				this.name = garbageCollectorMXBean.getName();
				this.collectionCount = garbageCollectorMXBean.getCollectionCount();
			}

			public String getName() {
				return this.name;
			}

			public long getCollectionCount() {
				return this.collectionCount;
			}

		}

	}

}
