/*
 * Copyright 2021 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.quic;

import java.util.Objects;

/**
 * 配置
 * <a href="https://quiclog.github.io/internet-drafts/draft-marx-qlog-main-schema.html">qlog</a>
 * 日志输出路径与元数据。
 */
public final class QLogConfiguration {

    /** 日志文件路径（若不存在则创建；为目录时自动生成文件名）。 */
    private final String path;
    /** 日志标题。 */
    private final String logTitle;
    /** 日志描述。 */
    private final String logDescription;

    /**
     * 创建 qlog 配置。
     *
     * @param path              日志文件路径，文件尚不存在；若为目录则自动生成文件名。
     * @param logTitle          写入 qlog 的标题。
     * @param logDescription    写入 qlog 的描述。
     */
    public QLogConfiguration(String path, String logTitle, String logDescription) {
        this.path = Objects.requireNonNull(path, "path");
        this.logTitle = Objects.requireNonNull(logTitle, "logTitle");
        this.logDescription = Objects.requireNonNull(logDescription, "logDescription");
    }

    /**
     * 返回日志文件路径。
     *
     * @return 路径字符串。
     */
    public String path() {
        return path;
    }

    /**
     * 返回日志标题。
     *
     * @return 标题。
     */
    public String logTitle() {
        return logTitle;
    }

    /**
     * 返回日志描述。
     *
     * @return 描述。
     */
    public String logDescription() {
        return logDescription;
    }
}
