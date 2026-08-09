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
import io.netty.handler.codec.http.HttpConstants;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 混合存储 {@link Attribute}：小于 {@code limitSize} 驻内存，超出后透明切换为 {@link DiskAttribute}。
 * <p>继承 {@link AbstractMixedHttpData} 的阈值与磁盘目录配置。
 */
public class MixedAttribute extends AbstractMixedHttpData<Attribute> implements Attribute {
    /** 使用默认字符集与磁盘目录创建混合 Attribute */
    public MixedAttribute(String name, long limitSize) {
        this(name, limitSize, HttpConstants.DEFAULT_CHARSET);
    }

    public MixedAttribute(String name, long definedSize, long limitSize) {
        this(name, definedSize, limitSize, HttpConstants.DEFAULT_CHARSET);
    }

    public MixedAttribute(String name, long limitSize, Charset charset) {
        this(name, limitSize, charset, DiskAttribute.baseDirectory, DiskAttribute.deleteOnExitTemporaryFile);
    }

    public MixedAttribute(String name, long limitSize, Charset charset, String baseDir, boolean deleteOnExit) {
        this(name, 0, limitSize, charset, baseDir, deleteOnExit);
    }

    public MixedAttribute(String name, long definedSize, long limitSize, Charset charset) {
        this(name, definedSize, limitSize, charset,
                DiskAttribute.baseDirectory, DiskAttribute.deleteOnExitTemporaryFile);
    }

    public MixedAttribute(String name, long definedSize, long limitSize, Charset charset,
                          String baseDir, boolean deleteOnExit) {
        super(limitSize, baseDir, deleteOnExit,
                new MemoryAttribute(name, definedSize, charset));
    }

    public MixedAttribute(String name, String value, long limitSize) {
        this(name, value, limitSize, HttpConstants.DEFAULT_CHARSET,
                DiskAttribute.baseDirectory, DiskFileUpload.deleteOnExitTemporaryFile);
    }

    public MixedAttribute(String name, String value, long limitSize, Charset charset) {
        this(name, value, limitSize, charset,
                DiskAttribute.baseDirectory, DiskFileUpload.deleteOnExitTemporaryFile);
    }

    /** 按初始值长度选择 {@link MemoryAttribute} 或 {@link DiskAttribute}；磁盘失败时回退内存 */
    private static Attribute makeInitialAttributeFromValue(String name, String value, long limitSize, Charset charset,
                                                           String baseDir, boolean deleteOnExit) {
        if (value.length() > limitSize) {
            try {
                return new DiskAttribute(name, value, charset, baseDir, deleteOnExit);
            } catch (IOException e) {
                // 磁盘创建失败时回退为纯内存模式
                try {
                    return new MemoryAttribute(name, value, charset);
                } catch (IOException ignore) {
                    throw new IllegalArgumentException(e);
                }
            }
        } else {
            try {
                return new MemoryAttribute(name, value, charset);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    public MixedAttribute(String name, String value, long limitSize, Charset charset,
                          String baseDir, boolean deleteOnExit) {
        super(limitSize, baseDir, deleteOnExit,
                makeInitialAttributeFromValue(name, value, limitSize, charset, baseDir, deleteOnExit));
    }

    @Override
    public String getValue() throws IOException {
        return wrapped.getValue();
    }

    @Override
    public void setValue(String value) throws IOException {
        wrapped.setValue(value);
    }

    /** 超过阈值时创建 {@link DiskAttribute} 并继承 maxSize 限制 */
    @Override
    Attribute makeDiskData() {
        DiskAttribute diskAttribute = new DiskAttribute(getName(), definedLength(), baseDir, deleteOnExit);
        diskAttribute.setMaxSize(getMaxSize());
        return diskAttribute;
    }

    @Override
    public Attribute copy() {
        // 显式委托父类，保持二进制 API 兼容
        return super.copy();
    }

    @Override
    public Attribute duplicate() {
        // for binary compatibility
        return super.duplicate();
    }

    @Override
    public Attribute replace(ByteBuf content) {
        // for binary compatibility
        return super.replace(content);
    }

    @Override
    public Attribute retain() {
        // for binary compatibility
        return super.retain();
    }

    @Override
    public Attribute retain(int increment) {
        // for binary compatibility
        return super.retain(increment);
    }

    @Override
    public Attribute retainedDuplicate() {
        // for binary compatibility
        return super.retainedDuplicate();
    }

    @Override
    public Attribute touch() {
        // for binary compatibility
        return super.touch();
    }

    @Override
    public Attribute touch(Object hint) {
        // for binary compatibility
        return super.touch(hint);
    }
}
