/*
 * Copyright 2014 The Netty Project
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

package io.netty.handler.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.util.CharsetUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a PEM file and converts it into a list of DERs so that they are imported into a {@link KeyStore} easily.
 *
 * <p>用正则解析 PEM 文本，提取证书链与 PKCS#8 私钥的 Base64 体并解码为 DER {@link ByteBuf}，
 * 供 {@link SslContextBuilder} 等无需 KeyStore 的路径使用。</p>
 */
final class PemReader {

    /** 证书 BEGIN 行匹配（兼容多种 CERTIFICATE 变体）。 */
    private static final Pattern CERT_HEADER = Pattern.compile(
            "-+BEGIN\\s[^-\\r\\n]*CERTIFICATE[^-\\r\\n]*-+(?:\\s|\\r|\\n)+");
    private static final Pattern CERT_FOOTER = Pattern.compile(
            "-+END\\s[^-\\r\\n]*CERTIFICATE[^-\\r\\n]*-+(?:\\s|\\r|\\n)*");
    private static final Pattern KEY_HEADER = Pattern.compile(
            "-+BEGIN\\s[^-\\r\\n]*PRIVATE\\s+KEY[^-\\r\\n]*-+(?:\\s|\\r|\\n)+");
    private static final Pattern KEY_FOOTER = Pattern.compile(
            "-+END\\s[^-\\r\\n]*PRIVATE\\s+KEY[^-\\r\\n]*-+(?:\\s|\\r|\\n)*");
    private static final Pattern BODY = Pattern.compile("[a-z0-9+/=][a-z0-9+/=\\r\\n]*", Pattern.CASE_INSENSITIVE);

    /** 从文件读取并解析全部 X.509 证书 DER。 */
    static ByteBuf[] readCertificates(File file) throws CertificateException {
        try (InputStream in = new FileInputStream(file)) {
            return readCertificates(in);
        } catch (IOException e) {
            throw new CertificateException("could not find certificate file: " + file);
        }
    }

    /** 从输入流顺序扫描 PEM，每段证书解码为一个 DER {@link ByteBuf}。 */
    static ByteBuf[] readCertificates(InputStream in) throws CertificateException {
        String content;
        try {
            content = readContent(in);
        } catch (IOException e) {
            throw new CertificateException("failed to read certificate input stream", e);
        }

        List<ByteBuf> certs = new ArrayList<ByteBuf>();
        Matcher m = CERT_HEADER.matcher(content);
        int start = 0;
        try {
            for (;;) {
                if (!m.find(start)) {
                    break;
                }

                // Android 上 usePattern 会重置 Matcher 位置，须保存 start（见 Google issue 293206296）
                start = m.end();
                m.usePattern(BODY);
                if (!m.find(start)) {
                    break;
                }

                ByteBuf base64 = Unpooled.copiedBuffer(m.group(0), CharsetUtil.US_ASCII);
                try {
                    start = m.end();
                    m.usePattern(CERT_FOOTER);
                    if (!m.find(start)) {
                        // 证书块不完整，停止解析
                        break;
                    }
                    ByteBuf der = Base64.decode(base64);
                    certs.add(der);
                } finally {
                    base64.release();
                }

                start = m.end();
                m.usePattern(CERT_HEADER);
            }
        } catch (Throwable e) {
            for (ByteBuf cert : certs) {
                cert.release();
            }
            throw e;
        }

        if (certs.isEmpty()) {
            throw new CertificateException("found no certificates in input stream");
        }

        return certs.toArray(new ByteBuf[0]);
    }

    /** 从文件读取 PKCS#8 私钥 DER。 */
    static ByteBuf readPrivateKey(File file) throws KeyException {
        try (InputStream in = new FileInputStream(file)) {
            return readPrivateKey(in);
        } catch (IOException e) {
            throw new KeyException("could not find key file: " + file);
        }
    }

    /** 解析首个 PRIVATE KEY PEM 块并 Base64 解码为 DER。 */
    static ByteBuf readPrivateKey(InputStream in) throws KeyException {
        String content;
        try {
            content = readContent(in);
        } catch (IOException e) {
            throw new KeyException("failed to read key input stream", e);
        }
        int start = 0;
        Matcher m = KEY_HEADER.matcher(content);
        if (!m.find(start)) {
            throw keyNotFoundException();
        }
        start = m.end();
        m.usePattern(BODY);
        if (!m.find(start)) {
            throw keyNotFoundException();
        }

        ByteBuf base64 = Unpooled.copiedBuffer(m.group(0), CharsetUtil.US_ASCII);
        try {
            start = m.end();
            m.usePattern(KEY_FOOTER);
            if (!m.find(start)) {
                // 私钥块缺少 END 行
                throw keyNotFoundException();
            }
            return Base64.decode(base64);
        } finally {
            base64.release();
        }
    }

    private static KeyException keyNotFoundException() {
        return new KeyException("could not find a PKCS #8 private key in input stream" +
                " (see https://netty.io/wiki/sslcontextbuilder-and-private-key.html for more information)");
    }

    private static String readContent(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        for (;;) {
            int ret = in.read(buf);
            if (ret < 0) {
                break;
            }
            out.write(buf, 0, ret);
        }
        return out.toString(CharsetUtil.US_ASCII.name());
    }

    private PemReader() { }
}
