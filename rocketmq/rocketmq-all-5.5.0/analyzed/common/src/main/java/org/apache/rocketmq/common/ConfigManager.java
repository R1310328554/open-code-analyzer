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
package org.apache.rocketmq.common;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 配置持久化抽象基类：JSON 编码/解码、主文件与 .bak 备份的原子读写。
 */
public abstract class ConfigManager {
    /** 配置管理日志器。 */
    private static final Logger log = LoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);

    /** 从主配置文件加载；失败或空文件时尝试 .bak 备份。 */
    public boolean load() {
        String fileName = null;
        try {
            fileName = this.configFilePath();
            String jsonString = MixAll.file2String(fileName);

            if (null == jsonString || jsonString.length() == 0) {
                // 删除无效主配置文件
                Files.deleteIfExists(Paths.get(fileName));
                return this.loadBak();
            } else {
                this.decode(jsonString);
                log.info("load " + fileName + " OK");
                return true;
            }
        } catch (Exception e) {
            log.error("load " + fileName + " failed, and try to load backup file", e);
            try {
                if (fileName != null) {
                    // delete invalid file
                    Files.deleteIfExists(Paths.get(fileName));
                }
            } catch (Throwable t) {
                log.error("load " + fileName + " failed, and delete invalid file errr", e);
            }
            return this.loadBak();
        }
    }

    /** 从 .bak 备份文件加载配置。 */
    private boolean loadBak() {
        String fileName = null;
        try {
            fileName = this.configFilePath() + ".bak";
            String jsonString = MixAll.file2String(fileName);
            if (jsonString != null && jsonString.length() > 0) {
                this.decode(jsonString);
                log.info("load " + fileName + " OK");
                return true;
            }
        } catch (Exception e) {
            log.error("load " + fileName + " Failed", e);
            return false;
        }

        return true;
    }

    /** 按 Topic 持久化（当前为 stub，委托 persist()）。 */
    public synchronized <T> void persist(String topicName, T t) {
        // 预留扩展
        this.persist();
    }

    /** 批量持久化（当前为 stub，委托 persist()）。 */
    public synchronized <T> void persist(Map<String, T> m) {
        // stub for future
        this.persist();
    }

    /** 将 encode 结果原子写入配置文件（先备份再写入并 fsync）。 */
    public synchronized void persist() {
        String jsonString = this.encode(true);
        if (jsonString != null) {
            try {
                // 备份现有配置文件
                String config = configFilePath();
                String backup = config + ".bak";
                File configFile = new File(config);
                File bakFile = new File(backup);

                if (configFile.exists()) {
                    // 原子移动为 .bak
                    Files.move(configFile.toPath(), bakFile.toPath(), StandardCopyOption.ATOMIC_MOVE);

                    // fsync 目录确保备份可见
                    MixAll.fsyncDirectory(Paths.get(bakFile.getParent()));
                }

                File dir = new File(configFile.getParent());
                if (!dir.exists()) {
                    Files.createDirectories(dir.toPath());
                }

                try (RandomAccessFile randomAccessFile = new RandomAccessFile(config, "rw")) {
                    randomAccessFile.write(jsonString.getBytes(StandardCharsets.UTF_8));
                    randomAccessFile.getChannel().force(true);
                    // fsync 目录确保新配置可见
                    MixAll.fsyncDirectory(Paths.get(configFile.getParent()));
                }
            } catch (Throwable t) {
                log.error("Failed to persist", t);
            }
        }
    }

    /** 停止配置管理（默认可直接返回 true）。 */
    public boolean stop() {
        return true;
    }

    /** 关闭并调用 stop()。 */
    public void shutdown() {
        stop();
    }

    /** 配置文件路径。 */
    public abstract String configFilePath();

    /** 编码为 JSON 字符串（默认格式）。 */
    public abstract String encode();

    /** 编码为 JSON，可选美化格式。 */
    public abstract String encode(final boolean prettyFormat);

    /** 从 JSON 字符串解码配置。 */
    public abstract void decode(final String jsonString);
}
