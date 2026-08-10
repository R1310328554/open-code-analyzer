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

package com.alibaba.nacos.common.tls;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.common.executor.ExecutorFactory;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.utils.ClassUtils;
import com.alibaba.nacos.common.utils.IoUtils;
import com.alibaba.nacos.common.utils.MD5Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TLS 证书文件变更监听器：单例模式，定时计算监听文件的 MD5，
 * 变化时回调 {@link FileChangeListener#onChanged} 以热重载 SSL 上下文。
 * 因需兼容 JDK 1.6 且避免 core 模块依赖，未使用 WatchFileCenter。
 * Certificate file update monitoring
 *
 * <p>Considering that the current client needs to support jdk 1.6 and module dependencies ,
 * the WatchFileCenter in the core module is not used
 *
 * @author wangwei
 */
public final class TlsFileWatcher {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TlsFileWatcher.class);
    
    /** 是否已启动定时扫描任务 */
    private AtomicBoolean started = new AtomicBoolean(false);
    
    /** 文件 MD5 检查间隔（分钟），来自 {@link TlsSystemConfig} */
    private final int checkInterval = TlsSystemConfig.tlsFileCheckInterval;
    
    /** 文件路径 → 上次已知 MD5 摘要 */
    private Map<String, String> fileMd5Map = new ConcurrentHashMap<>();
    
    /** 监听路径 → 变更回调 */
    private ConcurrentHashMap<String, FileChangeListener> watchFilesMap = new ConcurrentHashMap<>();
    
    /** 托管单线程调度器，执行周期性 MD5 比对 */
    private final ScheduledExecutorService service = ExecutorFactory.Managed
        .newSingleScheduledExecutorService(ClassUtils.getCanonicalName(TlsFileWatcher.class),
            new NameThreadFactory("com.alibaba.nacos.core.common.tls"));
    
    /** 单例实例，构造时自动 start */
    private static TlsFileWatcher tlsFileWatcher = new TlsFileWatcher();
    
    /** 私有构造，注册后立即启动监听 */
    private TlsFileWatcher() {
        start();
    }
    
    /** 获取全局单例 */
    public static TlsFileWatcher getInstance() {
        return tlsFileWatcher;
    }
    
    /**
     * 为存在的证书文件注册变更监听，并记录初始 MD5。
     *
     * @param fileChangeListener 变更回调
     * @param filePaths          一个或多个证书文件路径
     * @throws IOException 读取文件计算 MD5 失败时抛出
     */
    public void addFileChangeListener(FileChangeListener fileChangeListener, String... filePaths)
        throws IOException {
        for (String filePath : filePaths) {
            if (filePath != null && new File(filePath).exists()) {
                watchFilesMap.put(filePath, fileChangeListener);
                InputStream in = null;
                try {
                    in = new FileInputStream(filePath);
                    fileMd5Map.put(filePath,
                        MD5Utils.md5Hex(IoUtils.toString(in, Constants.ENCODE), Constants.ENCODE));
                } finally {
                    IoUtils.closeQuietly(in);
                }
            }
        }
    }
    
    /** 启动定时任务：MD5 变化时触发 listener 并更新缓存 */

    public void start() {
        if (started.compareAndSet(false, true)) {
            service.scheduleAtFixedRate(() -> {
                for (Map.Entry<String, FileChangeListener> item : watchFilesMap.entrySet()) {
                    String filePath = item.getKey();
                    String newHash;
                    InputStream in = null;
                    try {
                        in = new FileInputStream(filePath);
                        newHash = MD5Utils.md5Hex(IoUtils.toString(in, Constants.ENCODE),
                            Constants.ENCODE);
                    } catch (Exception exception) {
                        LOGGER.warn(
                            " service has exception when calculate the file MD5. " + exception);
                        continue;
                    } finally {
                        IoUtils.closeQuietly(in);
                    }
                    if (!newHash.equals(fileMd5Map.get(filePath))) {
                        LOGGER.info(filePath + " file hash changed, need reload ssl context");
                        fileMd5Map.put(filePath, newHash);
                        item.getValue().onChanged(filePath);
                        LOGGER.info(filePath + " onChanged success!");
                    }
                }
            }, 1, checkInterval, TimeUnit.MINUTES);
        }
    }
    
    /** 证书文件内容变更时的回调接口 */
    public interface FileChangeListener {
        
        /**
         * 文件 MD5 变更后调用，通常用于重载 SSLContext。
         *
         * @param filePath 发生变更的文件路径
         */
        void onChanged(String filePath);
    }
    
}
