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
package io.netty.handler.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;

/**
 * A marker interface for PEM encoded values.
 *
 * <p>标记 PEM 编码内容的 {@link ByteBufHolder} 接口；实现类通过 {@link #isSensitive()} 区分私钥等
 * 敏感材料与证书，以便释放时决定是否清零 {@link ByteBuf}。</p>
 */
interface PemEncoded extends ByteBufHolder {

    /**
     * Returns {@code true} if the PEM encoded value is considered
     * sensitive information such as a private key.
     *
     * <p>敏感 PEM（如私钥）返回 {@code true}，{@code deallocate} 时会擦除底层字节。</p>
     */
    boolean isSensitive();

    @Override
    PemEncoded copy();

    @Override
    PemEncoded duplicate();

    @Override
    PemEncoded retainedDuplicate();

    @Override
    PemEncoded replace(ByteBuf content);

    @Override
    PemEncoded retain();

    @Override
    PemEncoded retain(int increment);

    @Override
    PemEncoded touch();

    @Override
    PemEncoded touch(Object hint);
}
