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

import io.netty.util.ReferenceCounted;

/**
 * 可由 {@link HttpPostRequestEncoder}/{@link HttpPostRequestDecoder} 编解码的数据项接口。
 * <p>
 * 实现 {@link ReferenceCounted} 以管理 {@link ByteBuf} 引用；按 {@link HttpDataType} 区分
 * 普通属性、文件上传与编码器内部占位项。
 */
public interface InterfaceHttpData extends Comparable<InterfaceHttpData>, ReferenceCounted {
    /** multipart 数据项类型：表单属性、文件上传、编码器内部项。 */
    enum HttpDataType {
        Attribute, FileUpload, InternalAttribute
    }

    /** 返回表单字段名（multipart {@code name}）。 */

    String getName();

    /** 返回数据项类型 {@link HttpDataType}。 */

    HttpDataType getHttpDataType();

    @Override
    InterfaceHttpData retain();

    @Override
    InterfaceHttpData retain(int increment);

    @Override
    InterfaceHttpData touch();

    @Override
    InterfaceHttpData touch(Object hint);
}
