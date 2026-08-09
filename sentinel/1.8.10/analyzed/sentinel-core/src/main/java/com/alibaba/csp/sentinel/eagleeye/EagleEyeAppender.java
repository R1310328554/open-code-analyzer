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
 * EagleEye 日志输出器抽象基类。
 * <p>定义 append、flush、rollOver、reload、close 与 cleanup 等生命周期钩子。</p>
 */
public abstract class EagleEyeAppender {

    /**
     * 追加一条日志行。
     */
    public abstract void append(String log);

    /**
     * 刷新缓冲输出。
     */
    public void flush() {
        // 默认无操作
    }

    /**
     * 滚动日志文件（按大小或策略切分）。
     */
    public void rollOver() {
        // 默认无操作
    }

    /**
     * 检测外部滚动并重新打开输出流。
     */
    public void reload() {
        // 默认无操作
    }

    /**
     * 关闭输出资源。
     */
    public void close() {
        // 默认无操作
    }

    /**
     * 清理已标记删除的历史日志文件。
     */
    public void cleanup() {
        // 默认无操作
    }

    /**
     * 返回日志输出位置（例如文件路径）。
     */
    public String getOutputLocation() {
        return null;
    }
}
