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
package io.netty.handler.codec.serialization;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

/**
 * 精简版 {@link ObjectOutputStream}，写入更紧凑的类描述符。
 * <p>
 * 对普通类写入“瘦”描述符（仅类名），对基本类型、数组、接口等仍写入完整描述符。
 */
class CompactObjectOutputStream extends ObjectOutputStream {

    /** 完整（fat）类描述符类型标识。 */
    /** 完整（fat）类描述符类型标识。 */
    static final int TYPE_FAT_DESCRIPTOR = 0;
    /** 精简（thin）类描述符类型标识，仅含类名。 */
    /** 精简（thin）类描述符类型标识，仅含类名。 */
    static final int TYPE_THIN_DESCRIPTOR = 1;

    CompactObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected void writeStreamHeader() throws IOException {
        writeByte(STREAM_VERSION);
    }

    @Override
    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        Class<?> clazz = desc.forClass();
        if (clazz.isPrimitive() || clazz.isArray() || clazz.isInterface() ||
            desc.getSerialVersionUID() == 0) {
            // 特殊类型或 serialVersionUID 为 0 时使用完整描述符
            write(TYPE_FAT_DESCRIPTOR);
            super.writeClassDescriptor(desc);
        } else {
            // 普通类：仅写入类名以节省空间
            write(TYPE_THIN_DESCRIPTOR);
            writeUTF(desc.getName());
        }
    }
}
