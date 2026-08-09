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

import io.netty.buffer.ByteBuf;

import java.io.IOException;

/**
 * multipart 普通表单字段（非文件）的 {@link HttpData} 接口。
 */
public interface Attribute extends HttpData {
    /**
     * 以字符串形式返回字段值。
     */
    String getValue() throws IOException;

    /**
     * 设置字段的字符串值。
     */
    void setValue(String value) throws IOException;

    @Override
    Attribute copy();

    @Override
    Attribute duplicate();

    @Override
    Attribute retainedDuplicate();

    @Override
    Attribute replace(ByteBuf content);

    @Override
    Attribute retain();

    @Override
    Attribute retain(int increment);

    @Override
    Attribute touch();

    @Override
    Attribute touch(Object hint);
}
