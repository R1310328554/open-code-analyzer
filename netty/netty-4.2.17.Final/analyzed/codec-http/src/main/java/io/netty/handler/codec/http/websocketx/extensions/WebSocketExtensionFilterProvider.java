/*
 * Copyright 2019 The Netty Project
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
package io.netty.handler.codec.http.websocketx.extensions;

/**
 * 扩展过滤器提供者：为指定 {@link WebSocketExtension} 分别提供编码侧与解码侧过滤器。
 * <p>握手阶段通过 {@link WebSocketExtensionFilterProvider#DEFAULT} 或自定义实现注入 pipeline。
 */
public interface WebSocketExtensionFilterProvider {

    /** 默认实现：编码与解码均使用 {@link WebSocketExtensionFilter#NEVER_SKIP} */
    WebSocketExtensionFilterProvider DEFAULT = new WebSocketExtensionFilterProvider() {
        @Override
        public WebSocketExtensionFilter encoderFilter() {
            return WebSocketExtensionFilter.NEVER_SKIP;
        }

        @Override
        public WebSocketExtensionFilter decoderFilter() {
            return WebSocketExtensionFilter.NEVER_SKIP;
        }
    };

    /** 返回出站 {@link WebSocketExtensionEncoder} 使用的扩展过滤器 */

    WebSocketExtensionFilter encoderFilter();

    /** 返回入站 {@link WebSocketExtensionDecoder} 使用的扩展过滤器 */

    WebSocketExtensionFilter decoderFilter();

}
