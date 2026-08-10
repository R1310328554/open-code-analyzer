/*
 * Copyright 2016 The Netty Project
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
package io.netty.handler.codec.smtp;

import io.netty.util.internal.UnstableApi;

import java.util.Collections;
import java.util.List;

/**
 * Default {@link SmtpResponse} implementation.
 * <p>服务端 SMTP 应答：三位数字状态码（100–599，如 250 就绪、354 开始 DATA）
 * 与可选的多行说明文本；details 为不可变列表，供解码器组装后交给业务层。</p>
 */
@UnstableApi
public final class DefaultSmtpResponse implements SmtpResponse {

    private final int code;
    /** 应答附带的文本行（可为空）；多行响应时解码器可能合并为列表。 */
    private final List<CharSequence> details;

    /**
     * Creates a new instance with the given smtp code and no details.
     */
    public DefaultSmtpResponse(int code) {
        this(code, (List<CharSequence>) null);
    }

    /**
     * Creates a new instance with the given smtp code and details.
     */
    public DefaultSmtpResponse(int code, CharSequence... details) {
        this(code, SmtpUtils.toUnmodifiableList(details));
    }

    DefaultSmtpResponse(int code, List<CharSequence> details) {
        if (code < 100 || code > 599) {
            throw new IllegalArgumentException("code must be 100 <= code <= 599");
        }
        this.code = code;
        if (details == null) {
            this.details = Collections.emptyList();
        } else {
            this.details = Collections.unmodifiableList(details);
        }
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public List<CharSequence> details() {
        return details;
    }

    @Override
    public int hashCode() {
        return code * 31 + details.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultSmtpResponse)) {
            return false;
        }

        if (o == this) {
            return true;
        }

        DefaultSmtpResponse other = (DefaultSmtpResponse) o;

        return code() == other.code() &&
                details().equals(other.details());
    }

    @Override
    public String toString() {
        return "DefaultSmtpResponse{" +
                "code=" + code +
                ", details=" + details +
                '}';
    }
}
