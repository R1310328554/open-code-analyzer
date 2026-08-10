/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.authorization.client.util.crypto;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>轻量级 DER/ASN.1 解码器，用于解析 ECDSA 签名等二进制结构。
 *
 * <p>仅支持授权客户端所需的 SEQUENCE 与 INTEGER 标签，不支持不定长编码。
 *
 * @author rmartinc
 */
class ASN1Decoder {

    private final ByteArrayInputStream is;
    private final int limit;
    private int count;

    /** 从字节数组构造解码器。 */
    ASN1Decoder(byte[] bytes) {
        is = new ByteArrayInputStream(bytes);
        count = 0;
        limit = bytes.length;
    }

    /** 工厂方法：创建 ASN.1 解码器。 */
    public static ASN1Decoder create(byte[] bytes) {
        return new ASN1Decoder(bytes);
    }

    /** 读取 DER SEQUENCE，返回各子元素原始字节列表。 */
    public List<byte[]> readSequence() throws IOException {
        int tag = readTag();
        int tagNo = readTagNumber(tag);
        if (tagNo != ASN1Encoder.SEQUENCE) {
            throw new IOException("Invalid Sequence tag " + tagNo);
        }
        int length = readLength();
        List<byte[]> result = new ArrayList<>();
        while (length > 0) {
            byte[] bytes = readNext();
            result.add(bytes);
            length = length - bytes.length;
        }
        return result;
    }

    /** 读取 DER INTEGER 并转为 {@link BigInteger}。 */
    public BigInteger readInteger() throws IOException {
        int tag = readTag();
        int tagNo = readTagNumber(tag);
        if (tagNo != ASN1Encoder.INTEGER) {
            throw new IOException("Invalid Integer tag " + tagNo);
        }
        int length = readLength();
        byte[] bytes = read(length);
        return new BigInteger(bytes);
    }

    /** 读取下一个 ASN.1 元素（含标签与长度）的完整编码字节。 */
    byte[] readNext() throws IOException {
        mark();
        int tag = readTag();
        readTagNumber(tag);
        int length = readLength();
        length += reset();
        return read(length);
    }

    /** 读取单字节 ASN.1 标签。 */
    int readTag() throws IOException {
        int tag = read();
        if (tag < 0) {
            throw new EOFException("EOF found inside tag value.");
        }
        return tag;
    }

    /** 从标签字节解析标签号（含多字节高标签号形式）。 */
    int readTagNumber(int tag) throws IOException {
        int tagNo = tag & 0x1f;

        //
        // 带标签对象：标签号在低 5 位，或位于内容起始处
        //
        if (tagNo == 0x1f) {
            tagNo = 0;

            int b = read();

            // X.690-0207 8.1.2.4.2
            // "c) bits 7 to 1 of the first subsequent octet shall not all be zero."
            if ((b & 0x7f) == 0) // Note: -1 will pass
            {
                throw new IOException("corrupted stream - invalid high tag number found");
            }

            while ((b >= 0) && ((b & 0x80) != 0)) {
                tagNo |= (b & 0x7f);
                tagNo <<= 7;
                b = read();
            }

            if (b < 0) {
                throw new EOFException("EOF found inside tag value.");
            }

            tagNo |= (b & 0x7f);
        }

        return tagNo;
    }

    /** 读取 DER 长度字段（不支持不定长 0x80 编码）。 */
    int readLength() throws IOException {
        int length = read();
        if (length < 0) {
            throw new EOFException("EOF found when length expected");
        }

        if (length == 0x80) {
            throw new IOException("Indefinite-length encoding not supported in DER");
        }

        if (length > 127) {
            int size = length & 0x7f;

            // Note: The invalid long form "0xff" (see X.690 8.1.3.5c) will be caught here
            if (size > 4) {
                throw new IOException("DER length more than 4 bytes: " + size);
            }

            length = 0;
            for (int i = 0; i < size; i++) {
                int next = read();

                if (next < 0) {
                    throw new EOFException("EOF found reading length");
                }

                length = (length << 8) + next;
            }

            if (length < 0) {
                throw new IOException("corrupted stream - negative length found");
            }

            if (length >= limit) // after all we must have read at least 1 byte
            {
                throw new IOException("corrupted stream - out of bounds length found");
            }
        }

        return length;
    }

    /** 从输入流读取指定长度的原始字节。 */
    byte[] read(int length) throws IOException {
        byte[] bytes = new byte[length];
        int totalBytesRead = 0;

        while (totalBytesRead < length) {
            int bytesRead = is.read(bytes, totalBytesRead, length - totalBytesRead);
            if (bytesRead == -1) {
                throw new IOException(String.format("EOF found reading %d bytes", length));
            }
            totalBytesRead += bytesRead;
        }
        count += length;
        return bytes;
    }

    /** 标记当前读取位置，供 {@link #reset()} 回退。 */
    void mark() {
        count = 0;
        is.mark(is.available());
    }

    /** 回退到 mark 位置并返回已读字节数。 */
    int reset() {
        int tmp = count;
        is.reset();
        return tmp;
    }

    /** 读取单字节并累计已读计数。 */
    int read() {
        int tmp = is.read();
        if (tmp >= 0) {
            count++;
        }
        return tmp;
    }
}
