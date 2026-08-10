/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.storage.ldap.idm.store.ldap;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;


/**
 * ASN.1 BER 编码解码器（精简实现，仅支持当前 LDAP 扩展控制所需特性）。
 */
public class BERDecoder {
    /** Universal tag：SEQUENCE。 */
    public static final int TAG_SEQUENCE = 0x30;

    /** Tag class：context-specific。 */
    public static final int TAG_CLASS_CONTEXT_SPECIFIC = 0x80;

    /** Tag form：primitive。 */
    public static final int TAG_FORM_PRIMITIVE = 0x00;

    /** 待解码的字节缓冲区。 */
    private ByteBuffer encoded;

    /**
     * @param encodedValue BER 编码字节数组
     */
    public BERDecoder(byte[] encodedValue) {
        this.encoded = ByteBuffer.wrap(encodedValue);
    }

    /**
     * 开始解码 SEQUENCE 元素（消费 tag 与 length）。
     *
     * @throws DecodeException tag 非 SEQUENCE 或输入不足时
     */
    public void startSequence() throws DecodeException {
        try {
            byte tag = encoded.get();
            if (tag != TAG_SEQUENCE) {
                throw new DecodeException("Expected SEQUENCE (" + TAG_SEQUENCE + ") but got " + tag);
            }
            readLength();
        } catch (BufferUnderflowException e) {
            throw new DecodeException("Unexpected end of input");
        }
    }

    /**
     * 探测下一元素 tag 是否匹配，但不消费数据；无剩余数据时返回 {@code false}。
     *
     * @param clazz tag class
     * @param form tag form
     * @param tag tag number
     */
    public boolean isNextTag(int clazz, int form, int tag) {
        // 允许空 SEQUENCE 等场景：无剩余数据则直接返回 false
        if (!encoded.hasRemaining()) {
            return false;
        }
        encoded.mark();
        try {
            int expected = clazz | form | tag;
            int unsignedTag = encoded.get() & 0xFF;
            return unsignedTag == expected;
        } finally {
            encoded.reset();
        }
    }

    /**
     * 跳过下一个 BER 元素（消费 tag、length 与 value 字节）。
     *
     * @throws DecodeException 输入不足时
     */
    public void skipElement() throws DecodeException {
        try {
            encoded.get(); // 消费 tag
            int length = readLength();
            encoded.position(encoded.position() + length);
        } catch (BufferUnderflowException e) {
            throw new DecodeException("Unexpected end of input");
        }
    }

    /**
     * 读取并返回下一个元素的 value 字节（消费 tag 与 length）。
     *
     * @throws DecodeException 输入不足时
     */
    public byte[] drainElementValue() throws DecodeException {
        try {
            encoded.get(); // 消费 tag
            int length = readLength();
            byte[] value = new byte[length];
            encoded.get(value);
            return value;
        } catch (BufferUnderflowException e) {
            throw new DecodeException("Unexpected end of input");
        }
    }

    /** 读取 BER length 字段（支持短形式与最多 4 字节的长形式）。 */
    private int readLength() throws DecodeException {
        int length = encoded.get() & 0xFF;

        // 短形式
        if ((length & 0x80) == 0) {
            if (length > encoded.remaining()) {
                throw new DecodeException("Length " + length + " exceeds remaining buffer size " + encoded.remaining());
            }
            return length;
        }

        // 长形式；numBytes == 0 为不定长形式，不支持
        int numBytes = length & 0x7F;
        if (numBytes == 0 || numBytes > 4) {
            throw new DecodeException("Cannot handle more than 4 bytes of length, got " + numBytes + " bytes");
        }

        length = 0;
        for (int i = 0; i < numBytes; i++) {
            length = (length << 8) | (encoded.get() & 0xFF);
        }

        if (length < 0 || length > encoded.remaining()) {
            throw new DecodeException("Length " + length + " exceeds remaining buffer size " + encoded.remaining());
        }

        return length;
    }

    /** BER 解码失败时抛出的受检异常。 */
    public static final class DecodeException extends IOException {
        DecodeException(String message) {
            super(message);
        }
    }

}
