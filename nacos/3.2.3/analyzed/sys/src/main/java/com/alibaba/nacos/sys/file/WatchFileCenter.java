/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.sys.file;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.executor.ExecutorFactory;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.alibaba.nacos.common.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 统一文件变更监听管理中心。
 *
 * <p>每个目录对应一个 {@link WatchService} 与守护线程，变更时构造 {@link FileChangeEvent} 分发给已注册 {@link FileWatcher}；监听目录数受 {@code nacos.watch-file.max-dirs} 限制。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class WatchFileCenter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WatchFileCenter.class);
    
    /** 可同时监听的目录数量上限（默认 16，可通过 JVM 属性调整）。 */
    private static final int MAX_WATCH_FILE_JOB =
        Integer.getInteger("nacos.watch-file.max-dirs", 16);
    
    private static final Map<String, WatchDirJob> MANAGER = new HashMap<>(MAX_WATCH_FILE_JOB);
    
    private static final FileSystem FILE_SYSTEM = FileSystems.getDefault();
    
    private static final AtomicBoolean CLOSED = new AtomicBoolean(false);
    
    static {
        ThreadUtils.addShutdownHook(WatchFileCenter::shutdown);
    }
    
    /** 当前已注册监听的目录计数。 */
    @SuppressWarnings("checkstyle:StaticVariableName")
    private static int NOW_WATCH_JOB_CNT = 0;
    
    /**
     * 在指定目录注册 {@link FileWatcher}，目录首次注册时启动监听线程。
     *
     * @param paths   directory
     * @param watcher {@link FileWatcher}
     * @return register is success
     * @throws NacosException NacosException
     */
    public static synchronized boolean registerWatcher(final String paths, FileWatcher watcher)
        throws NacosException {
        checkState();
        if (NOW_WATCH_JOB_CNT == MAX_WATCH_FILE_JOB) {
            return false;
        }
        WatchDirJob job = MANAGER.get(paths);
        if (job == null) {
            job = new WatchDirJob(paths);
            job.start();
            MANAGER.put(paths, job);
            NOW_WATCH_JOB_CNT++;
        }
        job.addSubscribe(watcher);
        return true;
    }
    
    /** 注销目录下全部监听器并关闭对应 {@link WatchService}。 */
    public static synchronized boolean deregisterAllWatcher(final String path) {
        WatchDirJob job = MANAGER.get(path);
        if (job != null) {
            job.shutdown();
            MANAGER.remove(path);
            NOW_WATCH_JOB_CNT--;
            return true;
        }
        return false;
    }
    
    /** 关闭全部监听任务并释放资源（JVM 退出钩子也会调用）。 */
    public static void shutdown() {
        if (!CLOSED.compareAndSet(false, true)) {
            return;
        }
        LOGGER.warn("[WatchFileCenter] start close");
        for (Map.Entry<String, WatchDirJob> entry : MANAGER.entrySet()) {
            LOGGER.warn("[WatchFileCenter] start to shutdown this watcher which is watch : "
                + entry.getKey());
            try {
                entry.getValue().shutdown();
            } catch (Throwable e) {
                LOGGER.error("[WatchFileCenter] shutdown has error : ", e);
            }
        }
        MANAGER.clear();
        NOW_WATCH_JOB_CNT = 0;
        LOGGER.warn("[WatchFileCenter] already closed");
    }
    
    /**
     * Deregister {@link FileWatcher} in this directory.
     *
     * @param path    directory
     * @param watcher {@link FileWatcher}
     * @return deregister is success
      * <p>Nacos sys 模块：环境变量与部署类型、重复 Bean 抑制、操作系统指标、properties 加载、文件监听、组件扫描排除及 Console 模块状态；详见上方类说明。</p>
     */
    public static synchronized boolean deregisterWatcher(final String path,
        final FileWatcher watcher) {
        WatchDirJob job = MANAGER.get(path);
        if (job != null) {
            job.watchers.remove(watcher);
            return true;
        }
        return false;
    }
    
    static class WatchDirJob extends Thread {
        
        private final ExecutorService callBackExecutor;
        
        private final String paths;
        
        private final WatchService watchService;
        
        private volatile boolean watch = true;
        
        private final Set<FileWatcher> watchers = new ConcurrentHashSet<>();
        
        public WatchDirJob(String paths) throws NacosException {
            // AOT 编译场景下监听线程须为守护线程
            setDaemon(true);
            setName(paths);
            this.paths = paths;
            final Path p = Paths.get(paths);
            if (!p.toFile().isDirectory()) {
                throw new IllegalArgumentException("Must be a file directory : " + paths);
            }
            
            this.callBackExecutor = ExecutorFactory.newSingleExecutorService(
                new NameThreadFactory("com.alibaba.nacos.sys.file.watch-" + paths));
            
            try {
                WatchService service = FILE_SYSTEM.newWatchService();
                p.register(service, StandardWatchEventKinds.OVERFLOW,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
                this.watchService = service;
            } catch (Throwable ex) {
                throw new NacosException(NacosException.SERVER_ERROR, ex);
            }
        }
        
        void addSubscribe(final FileWatcher watcher) {
            watchers.add(watcher);
        }
        
        void shutdown() {
            watch = false;
            
            // 关闭 WatchService 避免监听线程阻塞无法退出（issue #9393）
            try {
                watchService.close();
            } catch (IOException ignore) {
            }
            
            ThreadUtils.shutdownThreadPool(this.callBackExecutor);
        }
        
        @Override
        public void run() {
            while (watch && !this.isInterrupted()) {
                try {
                    final WatchKey watchKey = watchService.take();
                    final List<WatchEvent<?>> events = watchKey.pollEvents();
                    watchKey.reset();
                    if (callBackExecutor.isShutdown()) {
                        return;
                    }
                    if (events.isEmpty()) {
                        continue;
                    }
                    callBackExecutor.execute(() -> {
                        for (WatchEvent<?> event : events) {
                            WatchEvent.Kind<?> kind = event.kind();
                            
                            // 操作系统事件溢出时遍历目录补偿通知
                            if (StandardWatchEventKinds.OVERFLOW.equals(kind)) {
                                eventOverflow();
                            } else {
                                eventProcess(event.context());
                            }
                        }
                    });
                } catch (InterruptedException | ClosedWatchServiceException ignore) {
                    Thread.interrupted();
                } catch (Throwable ex) {
                    LOGGER.error("An exception occurred during file listening : ", ex);
                }
            }
        }
        
        private void eventProcess(Object context) {
            final FileChangeEvent fileChangeEvent =
                FileChangeEvent.builder().paths(paths).context(context).build();
            final String str = String.valueOf(context);
            for (final FileWatcher watcher : watchers) {
                if (watcher.interest(str)) {
                    Runnable job = () -> watcher.onChange(fileChangeEvent);
                    Executor executor = watcher.executor();
                    if (executor == null) {
                        try {
                            job.run();
                        } catch (Throwable ex) {
                            LOGGER.error("File change event callback error : ", ex);
                        }
                    } else {
                        executor.execute(job);
                    }
                }
            }
        }
        
        private void eventOverflow() {
            File dir = Paths.get(paths).toFile();
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                // 溢出补偿仅处理文件，跳过子目录
                if (file.isDirectory()) {
                    continue;
                }
                eventProcess(file.getName());
            }
        }
        
    }
    
    private static void checkState() {
        if (CLOSED.get()) {
            throw new IllegalStateException("WatchFileCenter already shutdown");
        }
    }
}
