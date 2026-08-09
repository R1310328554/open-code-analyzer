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
package io.netty.buffer;

/**
 * 仅暴露父缓冲区子区域的派生视图。
 * 建议使用 {@link ByteBuf#slice()} 与 {@link ByteBuf#slice(int, int)}，而非直接调用构造函数。
 *
 * @deprecated 请勿使用。
 */
@Deprecated
public class SlicedByteBuf extends AbstractUnpooledSlicedByteBuf {

    /** 切片逻辑长度（与父缓冲区独立存储）。 */
    private int length;

    /** @param buffer 父缓冲区；@param index 起始偏移；@param length 切片长度 */
    public SlicedByteBuf(ByteBuf buffer, int index, int length) {
        super(buffer, index, length);
    }

    @Override
    final void initLength(int length) {
        this.length = length;
    }

    @Override
    final int length() {
        return length;
    }

    @Override
    public int capacity() {
        return length;
    }
}
