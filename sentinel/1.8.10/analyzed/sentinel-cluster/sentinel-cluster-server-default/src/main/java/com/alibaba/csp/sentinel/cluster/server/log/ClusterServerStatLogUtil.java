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
package com.alibaba.csp.sentinel.cluster.server.log;

import com.alibaba.csp.sentinel.eagleeye.EagleEye;
import com.alibaba.csp.sentinel.eagleeye.StatLogger;
import com.alibaba.csp.sentinel.log.LogBase;

/**
 * 集群令牌服务端统计日志工具，基于 EagleEye {@link StatLogger} 写入 sentinel-server.log。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterServerStatLogUtil {

    private static final String FILE_NAME = "sentinel-server.log";

    private static StatLogger statLogger;

    static {
        String path = LogBase.getLogBaseDir() + FILE_NAME;

        statLogger = EagleEye.statLoggerBuilder("sentinel-cluster-server-record")
            .intervalSeconds(1)
            .entryDelimiter('|')
            .keyDelimiter(',')
            .valueDelimiter(',')
            .maxEntryCount(5000)
            .configLogFilePath(path)
            .maxFileSizeMB(300)
            .maxBackupIndex(3)
            .buildSingleton();
    }

    /** 记录一条计数为 1 的统计日志。
     *
     * @param msg 日志消息键
     */
    public static void log(String msg) {
        statLogger.stat(msg).count();
    }

    /** 记录指定计数的统计日志。
     *
     * @param msg 日志消息键
     * @param count 计数值
     */
    public static void log(String msg, int count) {
        statLogger.stat(msg).count(count);
    }

    private ClusterServerStatLogUtil() {}
}
