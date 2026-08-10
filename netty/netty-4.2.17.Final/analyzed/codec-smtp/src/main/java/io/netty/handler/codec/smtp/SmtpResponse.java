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

import java.util.List;

/**
 * A SMTP response
 * <p>服务端 SMTP 应答：三位数字状态码（100–599，如 250 就绪、354 开始 DATA）
 * 与可选的多行说明文本。多行响应由 {@link SmtpResponseDecoder} 按 {@code 250-} / {@code 250 } 规则合并。</p>
 */
@UnstableApi
public interface SmtpResponse {

    /**
     * Returns the response code.
     * @return 三位 SMTP 应答码，首位表示类别（2=成功、4=临时失败、5=永久失败等）。
     */
    int code();

    /**
     * Returns the details if any.
     * @return 应答附带的文本行列表；单行或无文本时可能为空列表。
     */
    List<CharSequence> details();
}
