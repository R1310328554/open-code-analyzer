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
package com.alibaba.csp.sentinel.slots.logger;

import com.alibaba.csp.sentinel.eagleeye.EagleEye;
import com.alibaba.csp.sentinel.eagleeye.StatLogger;
import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * 阻断异常日志工具类，将流控/降级等阻断事件写入 sentinel-block.log。
 */
public class EagleEyeLogUtil {

    public static final String FILE_NAME = "sentinel-block.log";

    private static StatLogger statLogger;

    static {
        String path = LogBase.getLogBaseDir() + FILE_NAME;

        statLogger = EagleEye.statLoggerBuilder("sentinel-block-log")
            .intervalSeconds(1)
            .entryDelimiter('|')
            .keyDelimiter(',')
            .valueDelimiter(',')
            .maxEntryCount(6000)
            .configLogFilePath(path)
            .maxFileSizeMB(300)
            .maxBackupIndex(3)
            .buildSingleton();
    }

    /**
     * 记录一次阻断事件。
     *
     * @param resource      资源名
     * @param exceptionName 异常类名
     * @param ruleLimitApp  规则限流应用
     * @param origin        调用来源
     * @param ruleId        规则 ID
     * @param count         阻断数量
     */
    public static void log(String resource, String exceptionName, String ruleLimitApp, String origin, Long ruleId, int count) {
        String ruleIdString = StringUtil.EMPTY;
        if (ruleId != null) {
            ruleIdString = String.valueOf(ruleId);
        }
        statLogger.stat(resource, exceptionName, ruleLimitApp, origin, ruleIdString).count(count);
    }
}
