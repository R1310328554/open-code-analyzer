/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.client.cli.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.keycloak.client.cli.util.IoUtil;
import org.keycloak.util.JsonSerialization;

import static org.keycloak.client.cli.util.IoUtil.printErr;

/**
 * 基于 JSON 文件的 {@link ConfigHandler} 实现。
 * <p>
 * 读写 {@code ~/.keycloak/kcadm.config} 等配置文件，写操作通过文件锁保证并发安全。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class FileConfigHandler implements ConfigHandler {

    /** 配置文件最大允许大小（10 MB），超出则覆盖重写。 */
    private static final long MAX_SIZE = 10 * 1024 * 1024;
    /** 当前使用的配置文件路径。 */
    private static String configFile;

    /** 设置全局配置文件路径。 */
    public static void setConfigFile(String filename) {
        configFile = filename;
    }

    /** 获取当前配置文件路径。 */
    public static String getConfigFile() {
        return configFile;
    }

    /** 从文件加载配置；文件不存在或为空时返回空 {@link ConfigData}。 */
    public ConfigData loadConfig() {
        // 当前为简单实现，读取时不加文件锁
        File file = new File(configFile);
        if (!file.isFile() || file.length() == 0) {
            return new ConfigData();
        }

        try {
            try (FileInputStream is = new FileInputStream(configFile)) {
                return JsonSerialization.readValue(is, ConfigData.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + configFile, e);
        }
    }

    /** 确保配置文件及其父目录存在。 */
    public static void ensureFile() {
        Path path = null;
        try {
            path = Paths.get(new File(configFile).getAbsolutePath());
            IoUtil.ensureFile(path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create config file: " + path, e);
        }
    }

    /** 加锁读-改-写配置文件：加载 JSON、应用更新、写回 prettified JSON。 */
    public void saveMergeConfig(ConfigUpdateOperation op) {
        try {
            ensureFile();

            try (RandomAccessFile file = new RandomAccessFile(new File(configFile), "rw")) {
                FileChannel fileChannel = file.getChannel();

                FileLock fileLock = null;

                // 尝试获取写锁，最多重试 10 次
                int tryCount = 0;
                do try {
                    fileLock = fileChannel.tryLock();
                    break;
                } catch (OverlappingFileLockException e) {
                    // 短暂等待后重试
                    try {
                        Thread.sleep(100);
                        continue;
                    } catch (InterruptedException e1) {
                        throw new RuntimeException("Interrupted");
                    }
                } while (tryCount++ < 10);

                if (fileLock != null) {
                    try {
                        // 从文件加载现有配置
                        ConfigData config = new ConfigData();
                        long size = file.length();
                        if (size > MAX_SIZE) {
                            printErr("Config file " + configFile + " is too big. It will be overwritten.");
                            file.setLength(0);
                        } else if (size > 0){
                            byte[] buf = new byte[(int) size];
                            file.readFully(buf);
                            config = JsonSerialization.readValue(new ByteArrayInputStream(buf), ConfigData.class);
                        }

                        // 应用更新操作
                        op.update(config);

                        // 将配置写回文件
                        byte [] content = JsonSerialization.writeValueAsPrettyString(config).getBytes("utf-8");
                        file.seek(0);
                        file.write(content);
                        file.setLength(content.length);

                    } finally {
                        fileLock.release();
                    }
                } else {
                    throw new RuntimeException("Failed to get lock on " + configFile);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save " + configFile, e);
        }
    }
}
