/*
 * Copyright 2023 The Netty Project
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
package io.netty.handler.codec.http;

/**
 * {@link HttpHeaders} 实例工厂接口。
 * <p>
 * 默认实现为 {@link DefaultHttpHeadersFactory}，
 * 默认实例见 {@link DefaultHttpHeadersFactory#headersFactory()}。
 * 解码器/聚合器通过工厂创建请求头与 trailing headers。
 */
public interface HttpHeadersFactory {
    /** 创建新的 {@link HttpHeaders} 实例（常规容量）。 */

    HttpHeaders newHeaders();

    /** 创建尽可能紧凑的空 {@link HttpHeaders} 实例。 */

    HttpHeaders newEmptyHeaders();
}
