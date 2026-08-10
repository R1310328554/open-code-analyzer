/*
 * Copyright 2024 The Netty Project
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
package io.netty.util.internal;

import org.jetbrains.annotations.NotNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 带读取字节数上限的 {@link FilterInputStream} 包装器。
 * <p>累计已读字节超过 {@code maxBytesRead} 时抛出 {@link IOException}，用于限制不可信输入源的消耗。</p>
 */
public final class BoundedInputStream extends FilterInputStream {

    /** 允许读取的最大字节数。 */
    private final int maxBytesRead;
    /** 已累计读取的字节数。 */
    private int numRead;

    /**
     * @param in 底层输入流
     * @param maxBytesRead 最大可读字节数，必须为正
     */
    public BoundedInputStream(@NotNull InputStream in, int maxBytesRead) {
        super(in);
        this.maxBytesRead = ObjectUtil.checkPositive(maxBytesRead, "maxRead");
    }

    /** 默认上限 8 KiB。 */
    public BoundedInputStream(@NotNull InputStream in) {
        this(in, 8 * 1024);
    }

    @Override
    public int read() throws IOException {
        checkMaxBytesRead();

        int b = super.read();
        if (b != -1) {
            numRead++;
        }
        return b;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        checkMaxBytesRead();

        // 单次读取量不超过剩余配额
        // Calculate the maximum number of bytes that we should try to read.
        int num = Math.min(len, maxBytesRead - numRead + 1);

        int b = super.read(buf, off, num);

        if (b != -1) {
            numRead += b;
        }
        return b;
    }

    /** 超出上限时抛出 IOException。 */
    private void checkMaxBytesRead() throws IOException {
        if (numRead > maxBytesRead) {
            throw new IOException("Maximum number of bytes read: " + numRead);
        }
    }
}
