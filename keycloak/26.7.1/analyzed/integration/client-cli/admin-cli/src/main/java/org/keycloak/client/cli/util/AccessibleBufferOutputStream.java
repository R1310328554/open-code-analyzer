/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.client.cli.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 可读取最近写入缓冲区的 {@link FilterOutputStream}。
 * <p>
 * 每次 {@code write} 调用后缓存本次写入的字节，供调用方检查最后写入内容（如终端交互场景）。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class AccessibleBufferOutputStream extends FilterOutputStream{

    /** 最近一次 write 操作写入的字节副本。 */
    private byte[] buf;

    /**
     * 创建基于指定底层输出流的过滤器。
     *
     * @param out 底层输出流，可为 {@code null}
     */
    public AccessibleBufferOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
        buf = new byte[] {(byte) b};
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        buf = new byte[len];
        System.arraycopy(b, off, buf, 0, len);
    }

    /** 返回最近一次 write 写入的字节数组副本。 */
    public byte[] getBuffer() {
        return buf;
    }

    /** 返回缓冲区最后一个字节（0–255），无数据时返回 -1。 */
    public int getLastByte() {
        if (buf != null && buf.length > 0) {
            return 0xFF & buf[buf.length-1];
        }
        return -1;
    }
}
