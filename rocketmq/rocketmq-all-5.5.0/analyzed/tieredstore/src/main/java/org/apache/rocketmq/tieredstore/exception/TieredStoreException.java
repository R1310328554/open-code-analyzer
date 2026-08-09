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
package org.apache.rocketmq.tieredstore.exception;

/**
 * 分层存储运行时异常，携带 {@link TieredStoreErrorCode} 与可选 requestId/position。
 */
public class TieredStoreException extends RuntimeException {

    /** 错误码。 */
    private final TieredStoreErrorCode errorCode;
    /** 关联请求 ID（可选）。 */
    private String requestId;
    /** 出错位置偏移（默认 -1 表示未设置）。 */
    private long position = -1L;

    /** 构造带错误码与消息的异常。 */
    public TieredStoreException(TieredStoreErrorCode errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }

    /** 返回错误码。 */
    public TieredStoreErrorCode getErrorCode() {
        return errorCode;
    }

    /** 返回请求 ID。 */
    public String getRequestId() {
        return requestId;
    }

    /** 设置请求 ID。 */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /** 返回出错位置。 */
    public long getPosition() {
        return position;
    }

    /** 设置出错位置。 */
    public void setPosition(long position) {
        this.position = position;
    }

    /** 附加 requestId 与 position 的字符串表示。 */
    @Override
    public String toString() {
        StringBuilder errorStringBuilder = new StringBuilder(super.toString());
        if (requestId != null) {
            errorStringBuilder.append(" requestId: ").append(requestId);
        }
        if (position != -1L) {
            errorStringBuilder.append(", position: ").append(position);
        }
        return errorStringBuilder.toString();
    }
}
