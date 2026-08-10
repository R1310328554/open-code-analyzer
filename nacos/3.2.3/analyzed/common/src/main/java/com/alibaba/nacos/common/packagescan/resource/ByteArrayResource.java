/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.packagescan.resource;

import com.alibaba.nacos.common.packagescan.util.AbstractAssert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Copy from https://github.com/spring-projects/spring-framework.git, with less modifications
 * <p>字节数组 {@link Resource} 实现：每次 {@link #getInputStream()} 返回新的 {@link ByteArrayInputStream}，支持多次读取。</p>
 * {@link Resource} implementation for a given byte array.
 *
 * <p>Creates a {@link ByteArrayInputStream} for the given byte array.
 *
 * <p>Useful for loading content from any given byte array,
 * without having to resort to a single-use {@link InputStreamResource}.
 * Particularly useful for creating mail attachments from local content,
 * where JavaMail needs to be able to read the stream multiple times.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @see ByteArrayInputStream
 * @see InputStreamResource
 * @since 1.2.3
 */
public class ByteArrayResource extends AbstractResource {

    /** 底层字节数组，构造后不可变 */
    private final byte[] byteArray;

    /** 资源来源描述，用于 toString/getDescription */
    private final String description;

    /**
     * Create a new {@code ByteArrayResource}.
     *
     * @param byteArray the byte array to wrap
      * <p>字节数组资源实现；详见类级说明。</p>
     */
    public ByteArrayResource(byte[] byteArray) {
        this(byteArray, "resource loaded from byte array");
    }

    /**
     * Create a new {@code ByteArrayResource} with a description.
     *
     * @param byteArray   the byte array to wrap
     * @param description where the byte array comes from
      * <p>字节数组资源实现；详见类级说明。</p>
     */
    public ByteArrayResource(byte[] byteArray, String description) {
        AbstractAssert.notNull(byteArray, "Byte array must not be null");
        this.byteArray = byteArray;
        this.description = (description != null ? description : "");
    }

    /**
     * Return the underlying byte array.
     * <p>返回包装的字节数组引用。</p>
     */
    public final byte[] getByteArray() {
        return this.byteArray;
    }

    /**
     * This implementation always returns {@code true}.
     * <p>内存中的字节数组始终视为存在。</p>
     */
    @Override
    public boolean exists() {
        return true;
    }

    /**
     * This implementation returns the length of the underlying byte array.
      * <p>字节数组资源实现；详见类级说明。</p>
     */
    @Override
    public long contentLength() {
        return this.byteArray.length;
    }

    /**
     * This implementation returns a ByteArrayInputStream for the
     * underlying byte array.
     *
     * @see ByteArrayInputStream
      * <p>字节数组资源实现；详见类级说明。</p>
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.byteArray);
    }

    /**
     * This implementation returns a description that includes the passed-in
     * {@code description}, if any.
      * <p>字节数组资源实现；详见类级说明。</p>
     */
    @Override
    public String getDescription() {
        return "Byte array resource [" + this.description + "]";
    }


    /**
     * This implementation compares the underlying byte array.
     *
     * @see Arrays#equals(byte[], byte[])
      * <p>字节数组资源实现；详见类级说明。</p>
     */
    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof ByteArrayResource
                && Arrays.equals(((ByteArrayResource) other).byteArray, this.byteArray)));
    }

    /**
     * This implementation returns the hash code based on the
     * underlying byte array.
      * <p>字节数组资源实现；详见类级说明。</p>
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.byteArray);
    }

}
