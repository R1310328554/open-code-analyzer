/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec.rtsp;

import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.internal.ObjectUtil;

import java.util.Locale;

/**
 * RTSP 协议版本常量与解析工具。
 * <p>目前仅定义 {@link #RTSP_1_0}；{@link #valueOf(String)} 对已知版本返回单例，其余构造新实例。
 */
public final class RtspVersions {

    /**
     * RTSP/1.0 标准版本（RFC 2326）。
     */
    public static final HttpVersion RTSP_1_0 = new HttpVersion("RTSP", 1, 0, true);

    /**
     * 解析 RTSP 版本字符串，返回匹配的 {@link HttpVersion} 实例。
     * <p>若 {@code text} 等于 {@code "RTSP/1.0"}（大小写不敏感），返回 {@link #RTSP_1_0}；
     * 否则构造新的 {@link HttpVersion}。
     *
     * @param text RTSP 版本字符串，如 {@code "RTSP/1.0"}
     */
    public static HttpVersion valueOf(String text) {
        ObjectUtil.checkNotNull(text, "text");

        if (text.isEmpty()) {
            throw new IllegalArgumentException("text must not be empty");
        }
        // 必须指定 Locale.US，避免土耳其语等 locale 将 'i' 转为 'İ' 导致匹配失败
        String upper = text.toUpperCase(Locale.US);
        if ("RTSP/1.0".equals(upper)) {
            return RTSP_1_0;
        }

        return new HttpVersion(upper, true);
    }

    private RtspVersions() {
    }
}
