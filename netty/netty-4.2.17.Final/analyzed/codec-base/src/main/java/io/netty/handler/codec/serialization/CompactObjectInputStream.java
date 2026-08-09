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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

/**
 * 精简版 {@link ObjectInputStream}，配合 {@link CompactObjectOutputStream} 使用。
 * <p>
 * 支持“瘦”类描述符（仅类名），并通过 {@link ClassResolver} 加载类。
 */
class CompactObjectInputStream extends ObjectInputStream {

    /** 反序列化时解析类名的解析器。 */
    /** 反序列化时解析类名的解析器。 */
    private final ClassResolver classResolver;

    CompactObjectInputStream(InputStream in, ClassResolver classResolver) throws IOException {
        super(in);
        this.classResolver = classResolver;
    }

    @Override
    protected void readStreamHeader() throws IOException {
        int version = readByte() & 0xFF;
        if (version != STREAM_VERSION) {
            throw new StreamCorruptedException(
                    "Unsupported version: " + version);
        }
    }

    @Override
    protected ObjectStreamClass readClassDescriptor()
            throws IOException, ClassNotFoundException {
        int type = read();
        if (type < 0) {
            throw new EOFException();
        }
        switch (type) {
        case CompactObjectOutputStream.TYPE_FAT_DESCRIPTOR:
            // 完整类描述符，走标准 JDK 格式
            return super.readClassDescriptor();
        case CompactObjectOutputStream.TYPE_THIN_DESCRIPTOR:
            // 瘦描述符：仅类名，由 ClassResolver 解析
            String className = readUTF();
            Class<?> clazz = classResolver.resolve(className);
            return ObjectStreamClass.lookupAny(clazz);
        default:
            throw new StreamCorruptedException(
                    "Unexpected class descriptor type: " + type);
        }
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        Class<?> clazz;
        try {
            clazz = classResolver.resolve(desc.getName());
        } catch (ClassNotFoundException ignored) {
            // 自定义解析失败时回退到 JDK 默认逻辑
            clazz = super.resolveClass(desc);
        }

        return clazz;
    }

}
