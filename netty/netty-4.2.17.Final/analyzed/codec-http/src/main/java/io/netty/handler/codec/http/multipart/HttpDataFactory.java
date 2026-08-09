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
package io.netty.handler.codec.http.multipart;

import io.netty.handler.codec.http.HttpRequest;

import java.nio.charset.Charset;

/**
 * 创建 {@link InterfaceHttpData}（{@link Attribute}、{@link FileUpload}）的工厂接口。
 * <p>
 * 负责按请求选择内存/磁盘/混合存储，并维护待清理临时文件的列表。
 */
public interface HttpDataFactory {

    /** 设置字段最大字节限制，{@code -1} 不限；超限抛 {@link HttpPostRequestDecoder.ErrorDataDecoderException}。 */

    void setMaxLimit(long max);

    /** 创建空值 {@link Attribute}。 */

    Attribute createAttribute(HttpRequest request, String name);

    /** 创建带声明 Content-Length 的空 {@link Attribute}。 */

    Attribute createAttribute(HttpRequest request, String name, long definedSize);

    /** 创建带初始字符串值的 {@link Attribute}。 */

    Attribute createAttribute(HttpRequest request, String name, String value);

    /** 创建 {@link FileUpload}，{@code size} 为请求声明的文件大小。 */

    FileUpload createFileUpload(HttpRequest request, String name, String filename,
                                String contentType, String contentTransferEncoding, Charset charset,
                                long size);

    /** 从清理列表移除数据项；临时文件仍可能被删除。 */

    void removeHttpDataFromClean(HttpRequest request, InterfaceHttpData data);

    /** 清理指定请求关联的全部临时 {@link InterfaceHttpData}。 */

    void cleanRequestHttpData(HttpRequest request);

    /** 清理所有请求的临时 {@link InterfaceHttpData}。 */

    void cleanAllHttpData();

    /**
     * @deprecated Use {@link #cleanRequestHttpData(HttpRequest)} instead.
     */
    @Deprecated
    void cleanRequestHttpDatas(HttpRequest request);

    /**
     * @deprecated Use {@link #cleanAllHttpData()} instead.
     */
    @Deprecated
    void cleanAllHttpDatas();
}
