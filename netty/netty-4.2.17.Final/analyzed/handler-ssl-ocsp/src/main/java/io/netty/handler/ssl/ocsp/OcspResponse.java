/*
 * Copyright 2022 The Netty Project
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
package io.netty.handler.ssl.ocsp;

import java.util.Date;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * OCSP 查询得到的证书状态摘要，包含状态枚举与更新时间窗口。
 */
public class OcspResponse {
    /** 证书状态 */
    private final Status status;
    /** 本次 OCSP 响应生效时间 */
    private final Date thisUpdate;
    /** 下次 OCSP 更新预期时间 */
    private final Date nextUpdate;

    public OcspResponse(Status status, Date thisUpdate, Date nextUpdate) {
        this.status = checkNotNull(status, "Status");
        this.thisUpdate = checkNotNull(thisUpdate, "ThisUpdate");
        this.nextUpdate = checkNotNull(nextUpdate, "NextUpdate");
    }

    /** 返回证书状态 */
    public Status status() {
        return status;
    }

    /** 返回本次更新时间 */
    public Date thisUpdate() {
        return thisUpdate;
    }

    /** 返回下次更新时间 */
    public Date nextUpdate() {
        return nextUpdate;
    }

    @Override
    public String toString() {
        return "OcspResponse{" +
                "status=" + status +
                ", thisUpdate=" + thisUpdate +
                ", nextUpdate=" + nextUpdate +
                '}';
    }

    /** OCSP 证书状态枚举 */
    public enum Status {
        /**
         * 证书有效
         */
        VALID,

        /**
         * 证书已吊销
         */
        REVOKED,

        /**
         * 证书状态未知
         */
        UNKNOWN
    }
}
