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
package com.alibaba.csp.sentinel.eagleeye;

/**
 * EagleEye 统计日志构建器基类，提供日志路径、单文件大小、备份数量与字段分隔符等通用配置。
 *
 * @param <T> 具体构建器类型，用于链式调用
 */
class BaseLoggerBuilder<T extends BaseLoggerBuilder<T>> {

    protected final String loggerName;

    protected String filePath = null;

    protected long maxFileSize = 1024;

    protected char entryDelimiter = '|';

    protected int maxBackupIndex = 3;

    BaseLoggerBuilder(String loggerName) {
        this.loggerName = loggerName;
    }

    /**
     * 设置 EagleEye 日志目录下的相对日志文件路径。
     */
    public T logFilePath(String logFilePath) {
        return configLogFilePath(logFilePath, EagleEye.EAGLEEYE_LOG_DIR);
    }

    /**
     * 设置应用日志目录下的相对日志文件路径。
     */
    public T appFilePath(String appFilePath) {
        return configLogFilePath(appFilePath, EagleEye.APP_LOG_DIR);
    }

    /**
     * 设置基础日志目录下的相对日志文件路径。
     */
    public T baseLogFilePath(String baseLogFilePath) {
        return configLogFilePath(baseLogFilePath, EagleEye.BASE_LOG_DIR);
    }

    @SuppressWarnings("unchecked")
    private T configLogFilePath(String filePathToConfig, String basePath) {
        EagleEyeCoreUtils.checkNotNullEmpty(filePathToConfig, "filePath");
        if (filePathToConfig.charAt(0) != '/') {
            filePathToConfig = basePath + filePathToConfig;
        }
        this.filePath = filePathToConfig;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T configLogFilePath(String filePath) {
        EagleEyeCoreUtils.checkNotNullEmpty(filePath, "filePath");
        this.filePath = filePath;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    /**
     * 设置单个日志文件的最大大小（MB）。
     */
    public T maxFileSizeMB(long maxFileSizeMB) {
        if (maxFileSize < 10) {
            throw new IllegalArgumentException("Invalid maxFileSizeMB");
        }
        this.maxFileSize = maxFileSizeMB * 1024 * 1024;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    /**
     * 设置滚动保留的历史日志文件数量。
     */
    public T maxBackupIndex(int maxBackupIndex) {
        if (maxBackupIndex < 1) {
            throw new IllegalArgumentException("");
        }
        this.maxBackupIndex = maxBackupIndex;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    /**
     * 设置统计条目字段之间的分隔符。
     */
    public T entryDelimiter(char entryDelimiter) {
        this.entryDelimiter = entryDelimiter;
        return (T)this;
    }

    String getLoggerName() {
        return loggerName;
    }
}
