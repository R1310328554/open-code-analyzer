/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.transport.heartbeat.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 将 Socket 输入流中的原始 HTTP 响应解析为 {@link SimpleHttpResponse}。
 * </p>
 * <p>
 * 朴素实现：必须存在 {@code Content-Length}，否则丢弃 body；不支持 chunked/deflate。
 * </p>
 *
 * @author leyou
 */
public class SimpleHttpResponseParser {

    /** 允许的最大响应体 4MB。 */
    private static final int MAX_BODY_SIZE = 1024 * 1024 * 4;
    /** 读缓冲，按 maxSize 分配。 */
    private byte[] buf;

    public SimpleHttpResponseParser(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize must > 0");
        }
        this.buf = new byte[maxSize];
    }

    public SimpleHttpResponseParser() {
        this(1024 * 4);
    }

    /**
     * 从输入流增量读取并解析 HTTP 响应。
     *
     * @param in 输入流
     * @return 解析完成的响应，流提前结束时可能为 null
     * @throws IOException IO 错误
     */
    public SimpleHttpResponse parse(InputStream in) throws IOException {
        int bg = 0;
        int len;
        String statusLine = null;
        Map<String, String> headers = new HashMap<String, String>();
        Charset charset = Charset.forName("utf-8");
        int contentLength = -1;
        SimpleHttpResponse response;
        while (true) {
            if (bg >= buf.length) {
                throw new IndexOutOfBoundsException("buf index out of range: " + bg + ", buf.length=" + buf.length);
            }
            if ((len = in.read(buf, bg, buf.length - bg)) > 0) {
                bg += len;
                len = bg;
                int idx;
                int parseBg = 0;
                while ((idx = indexOfCRLF(parseBg, len)) >= 0) {
                    String line = new String(buf, parseBg, idx - parseBg, charset);
                    parseBg = idx + 2;
                    if (statusLine == null) {
                        statusLine = line;
                    } else {
                        if (line.isEmpty()) {
                            // 无 Content-Length 时曾考虑读剩余字节（已注释）
                            //if (contentLength == -1) {
                            //    contentLength = MAX_BODY_SIZE;
                            //}

                            // 解析 HTTP 正文
                            // 无 Content-Length 则丢弃 body 直接返回
                            response = new SimpleHttpResponse(statusLine, headers);
                            if (contentLength <= 0) {
                                return response;
                            }
                            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
                            // Content-Length 与已读长度不一致
                            if (contentLength < len - parseBg) {
                                throw new IllegalStateException("Invalid content length: " + contentLength);
                            }
                            out.write(buf, parseBg, len - parseBg);
                            if (out.size() > MAX_BODY_SIZE) {
                                throw new IllegalStateException(
                                    "Request body is too big, limit size is " + MAX_BODY_SIZE);
                            }
                            int cap = Math.min(contentLength - out.size(), buf.length);
                            while (cap > 0 && (len = in.read(buf, 0, cap)) > 0) {
                                out.write(buf, 0, len);
                                cap = Math.min(contentLength - out.size(), buf.length);
                            }
                            response.setBody(out.toByteArray());
                            return response;
                        } else if (!line.trim().isEmpty()) {
                            // 解析单行响应头
                            int idx2 = line.indexOf(":");
                            String key = line.substring(0, idx2).trim();
                            String value = line.substring(idx2 + 1).trim();
                            headers.put(key, value);
                            if ("Content-Length".equalsIgnoreCase(key)) {
                                contentLength = Integer.parseInt(value);
                            }
                        }
                    }
                }
                // 未消费字节前移，继续读下一行
                if (parseBg != 0) {
                    System.arraycopy(buf, parseBg, buf, 0, len - parseBg);
                }
                bg = len - parseBg;
            } else {
                break;
            }
        }
        return null;
    }

    /**
     * 在 buf[bg..ed) 中查找 \r\n 分隔符位置。
     *
     * @param bg 起始偏移
     * @param ed 结束偏移
     * @return CRLF 起始索引，未找到返回 -1
     */
    private int indexOfCRLF(int bg, int ed) {
        if (ed - bg < 2) {
            return -1;
        }
        for (int i = bg; i < ed - 1; i++) {
            if (buf[i] == '\r' && buf[i + 1] == '\n') {
                return i;
            }
        }
        return -1;
    }
}