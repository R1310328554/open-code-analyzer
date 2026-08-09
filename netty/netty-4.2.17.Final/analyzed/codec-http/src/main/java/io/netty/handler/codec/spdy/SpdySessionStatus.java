/*
 * Copyright 2013 The Netty Project
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
package io.netty.handler.codec.spdy;

import io.netty.util.internal.ObjectUtil;

/**
 * SPDY 会话级状态码及其描述，用于 GOAWAY 帧等会话终止场景。
 */
public class SpdySessionStatus implements Comparable<SpdySessionStatus> {

    /**
     * 0 OK — 正常关闭，无错误。
     */
    public static final SpdySessionStatus OK =
        new SpdySessionStatus(0, "OK");

    /**
     * 1 Protocol Error — 协议违规，会话必须终止。
     */
    public static final SpdySessionStatus PROTOCOL_ERROR =
        new SpdySessionStatus(1, "PROTOCOL_ERROR");

    /**
     * 2 Internal Error — 端点内部错误导致无法继续会话。
     */
    public static final SpdySessionStatus INTERNAL_ERROR =
        new SpdySessionStatus(2, "INTERNAL_ERROR");

    /**
     * 根据数值 code 返回对应 {@link SpdySessionStatus}。
     * 已知常量返回缓存实例，未知 code 构造新对象。
     */
    public static SpdySessionStatus valueOf(int code) {
        switch (code) {
        case 0:
            return OK;
        case 1:
            return PROTOCOL_ERROR;
        case 2:
            return INTERNAL_ERROR;
        }

        return new SpdySessionStatus(code, "UNKNOWN (" + code + ')');
    }

    private final int code;

    private final String statusPhrase;

    /**
     * 以指定 {@code code} 与 {@code statusPhrase} 创建实例。
     */
    public SpdySessionStatus(int code, String statusPhrase) {
        this.statusPhrase = ObjectUtil.checkNotNull(statusPhrase, "statusPhrase");
        this.code = code;
    }

    /**
     * 返回数值状态码。
     */
    public int code() {
        return code;
    }

    /**
     * 返回状态短语（如 {@code PROTOCOL_ERROR}）。
     */
    public String statusPhrase() {
        return statusPhrase;
    }

    @Override
    public int hashCode() {
        return code();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SpdySessionStatus)) {
            return false;
        }

        return code() == ((SpdySessionStatus) o).code();
    }

    @Override
    public String toString() {
        return statusPhrase();
    }

    @Override
    public int compareTo(SpdySessionStatus o) {
        return code() - o.code();
    }
}
