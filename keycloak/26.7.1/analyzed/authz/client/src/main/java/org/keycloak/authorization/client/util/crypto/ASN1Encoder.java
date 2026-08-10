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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * <p>轻量级 DER/ASN.1 编码器，用于构建 ECDSA 签名等二进制结构。
 *
 * @author rmartinc
 */
class ASN1Encoder {

    /** ASN.1 INTEGER 标签。 */
    static final int INTEGER = 0x02;
    /** ASN.1 SEQUENCE 标签（不含 CONSTRUCTED 位）。 */
    static final int SEQUENCE = 0x10;
    /** CONSTRUCTED 位，与 SEQUENCE 组合表示构造类型。 */
    static final int CONSTRUCTED = 0x20;

    private final ByteArrayOutputStream os;

    private ASN1Encoder() {
        this.os = new ByteArrayOutputStream();
    }

    /** 工厂方法：创建空 ASN.1 编码器。 */
    static public ASN1Encoder create() {
        return new ASN1Encoder();
    }

    /** 编码 INTEGER 值。 */
    public ASN1Encoder write(BigInteger value) throws IOException {
        writeEncoded(INTEGER, value.toByteArray());
        return this;
    }

    /** 将多个子编码器内容串联为 DER SEQUENCE。 */
    public ASN1Encoder writeDerSeq(ASN1Encoder... objects) throws IOException {
        writeEncoded(CONSTRUCTED | SEQUENCE, concatenate(objects));
        return this;
    }

    /** 返回已编码字节的副本。 */
    public byte[] toByteArray() {
        return os.toByteArray();
    }

    /** 写入带标签与 DER 长度的 TLV 结构。 */
    void writeEncoded(int tag, byte[] bytes) throws IOException {
        write(tag);
        writeLength(bytes.length);
        write(bytes);
    }

    /** 写入 DER 长度字段（短形式或长形式）。 */
    void writeLength(int length) throws IOException {
        if (length > 127) {
            int size = 1;
            int val = length;

            while ((val >>>= 8) != 0) {
                size++;
            }

            write((byte) (size | 0x80));

            for (int i = (size - 1) * 8; i >= 0; i -= 8) {
                write((byte) (length >> i));
            }
        } else {
            write((byte) length);
        }
    }

    void write(byte[] bytes) throws IOException {
        os.write(bytes);
    }

    void write(int b) throws IOException {
        os.write(b);
    }

    /** 将多个编码器输出拼接为单一字节数组。 */
    byte[] concatenate(ASN1Encoder... objects) throws IOException {
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        for (ASN1Encoder object : objects) {
            tmp.write(object.toByteArray());
        }
        return tmp.toByteArray();
    }
}
