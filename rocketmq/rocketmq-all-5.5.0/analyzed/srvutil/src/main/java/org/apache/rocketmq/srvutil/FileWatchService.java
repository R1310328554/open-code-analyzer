/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.srvutil;

import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import org.apache.rocketmq.common.LifecycleAwareServiceThread;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 文件变更监听服务：周期性计算 MD5，哈希变化时回调 Listener。
 * 常用于证书/配置文件热更新场景。
 */
public class FileWatchService extends LifecycleAwareServiceThread {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);
    /** 默认轮询间隔（毫秒）。 */
    private static final int DEFAULT_WATCH_INTERVAL = 500;

    /** 文件路径到上次 MD5 摘要的映射。 */
    private final Map<String, String> currentHash = new HashMap<>();
    /** 文件变更回调。 */
    private final Listener listener;
    /** 轮询间隔（毫秒）。 */
    private final int watchInterval;
    private final MessageDigest md = MessageDigest.getInstance("MD5");

    /** 使用默认轮询间隔构造监听服务。 */
    public FileWatchService(final String[] watchFiles, final Listener listener) throws Exception {
        this(watchFiles, listener, DEFAULT_WATCH_INTERVAL);
    }

    public FileWatchService(final String[] watchFiles, final Listener listener, int watchInterval) throws Exception {
        this.listener = listener;
        this.watchInterval = watchInterval;
        for (String file : watchFiles) {
            if (!Strings.isNullOrEmpty(file) && new File(file).exists()) {
                currentHash.put(file, md5Digest(file));
            }
        }
    }

    /** 返回服务线程名称。 */
    @Override
    public String getServiceName() {
        return "FileWatchService";
    }

    /** 主循环：定时比对 MD5，变化时触发 onChanged。 */
    @Override
    public void run0() {
        log.info(this.getServiceName() + " service started");

        while (!this.isStopped()) {
            try {
                this.waitForRunning(watchInterval);
                for (Map.Entry<String, String> entry : currentHash.entrySet()) {
                    String newHash = md5Digest(entry.getKey());
                    if (!newHash.equals(entry.getValue())) {
                        entry.setValue(newHash);
                        listener.onChanged(entry.getKey());
                    }
                }
            } catch (Exception e) {
                log.warn(this.getServiceName() + " service raised an unexpected exception.", e);
            }
        }
        log.info(this.getServiceName() + " service end");
    }

    /**
     * 注意：故意忽略 DELETE 事件，便于证书轮换时仍沿用旧文件哈希。
     * 文件不存在或 IO 异常时复用上次哈希，不向上抛异常。
     *
     * @param filePath 待计算 MD5 的文件绝对路径
     * @return 文件内容哈希；不存在时返回空串
     */
    private String md5Digest(String filePath) {
        Path path = Paths.get(filePath);
        if (!path.toFile().exists()) {
            // 复用上次哈希，避免短暂缺失导致误报
            return currentHash.getOrDefault(filePath, "");
        }
        byte[] raw;
        try {
            raw = Files.readAllBytes(path);
        } catch (IOException e) {
            log.info("Failed to read content of {}", filePath);
            // Reuse previous hash result
            return currentHash.getOrDefault(filePath, "");
        }
        md.update(raw);
        byte[] hash = md.digest();
        return UtilAll.bytes2string(hash);
    }

    /** 文件变更回调接口。 */
    public interface Listener {
        /**
         * 目标文件内容变更时调用
         *
         * @param path 变更文件路径
         */
        void onChanged(String path);
    }
}
