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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.transport.endpoint.Endpoint;

/**
 * <p>
 * 极简阻塞式 HTTP 客户端，仅支持 GET/POST 与 form-urlencoded 正文。
 * Content-Type 固定为 <pre>application/x-www-form-urlencoded</pre>，参数经 {@link URLEncoder} 编码。
 * </p>
 * <p>
 * 响应封装为 {@link SimpleHttpResponse}，正文按 charset 解码为字符串。
 * </p>
 * <p>
 * 同步阻塞：调用线程等待响应或超时。
 * </p>
 * <p>
 * 实现较朴素：响应必须带 {@code Content-Length}，否则丢弃 body；不支持 chunked/deflate 等编码。
 * </p>
 *
 * @author leyou
 * @author Leo Li
 */
public class SimpleHttpClient {

    /**
     * 执行 GET 请求，查询参数拼接到 URL。
     *
     * @param request HTTP 请求
     * @return 响应实体，request 为 null 时返回 null
     * @throws IOException 连接失败或中断
     */
    public SimpleHttpResponse get(SimpleHttpRequest request) throws IOException {
        if (request == null) {
            return null;
        }
        return request(request.getEndpoint(),
            RequestMethod.GET, request.getRequestPath(), request.getParams(),
            request.getCharset(), request.getSoTimeout());
    }

    /**
     * 执行 POST 请求，参数放在请求体。
     *
     * @param request HTTP 请求
     * @return 响应实体
     * @throws IOException 连接失败或中断
     */
    public SimpleHttpResponse post(SimpleHttpRequest request) throws IOException {
        if (request == null) {
            return null;
        }
        return request(request.getEndpoint(),
            RequestMethod.POST, request.getRequestPath(),
            request.getParams(), request.getCharset(),
            request.getSoTimeout());
    }

    private SimpleHttpResponse request(Endpoint endpoint,
                                       RequestMethod type, String requestPath,
                                       Map<String, String> paramsMap, Charset charset, int soTimeout)
        throws IOException {
        Socket socket = null;
        BufferedWriter writer;
        InetSocketAddress socketAddress = new InetSocketAddress(endpoint.getHost(), endpoint.getPort());
        try {
            socket = SocketFactory.getSocket(endpoint.getProtocol());
            socket.setSoTimeout(soTimeout);
            socket.connect(socketAddress, soTimeout);

            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), charset));
            requestPath = getRequestPath(type, requestPath, paramsMap, charset);
            writer.write(getStatusLine(type, requestPath) + "\r\n");
            if (charset != null) {
                writer.write("Content-Type: application/x-www-form-urlencoded; charset=" + charset.name() + "\r\n");
            } else {
                writer.write("Content-Type: application/x-www-form-urlencoded\r\n");
            }
            writer.write("Host: " + socketAddress.getHostName() + "\r\n");
            if (type == RequestMethod.GET) {
                writer.write("Content-Length: 0\r\n");
                writer.write("\r\n");
            } else {
                // POST：先写 Content-Length 再写 body
                String params = encodeRequestParams(paramsMap, charset);
                writer.write("Content-Length: " + params.getBytes(charset).length + "\r\n");
                writer.write("\r\n");
                writer.write(params);
            }
            writer.flush();

            SimpleHttpResponse response = new SimpleHttpResponseParser().parse(socket.getInputStream());
            socket.close();
            socket = null;
            return response;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception ex) {
                    RecordLog.warn("Error when closing {} request to {} in SimpleHttpClient", type, socketAddress, ex);
                }
            }
        }
    }

    private String getRequestPath(RequestMethod type, String requestPath,
                                  Map<String, String> paramsMap, Charset charset) {
        if (type == RequestMethod.GET) {
            if (requestPath.contains("?")) {
                return requestPath + "&" + encodeRequestParams(paramsMap, charset);
            }
            return requestPath + "?" + encodeRequestParams(paramsMap, charset);
        }
        return requestPath;
    }

    private String getStatusLine(RequestMethod type, String requestPath) {
        if (type == RequestMethod.POST) {
            return "POST " + requestPath + " HTTP/1.1";
        }
        return "GET " + requestPath + " HTTP/1.1";
    }

    /**
     * 将参数 Map 编码为 application/x-www-form-urlencoded 查询串。
     *
     * @param paramsMap 键值对
     * @param charset 字符集
     * @return 编码后的参数字符串，无参数时返回空串
     */
    private String encodeRequestParams(Map<String, String> paramsMap, Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charset is not allowed to be null");
        }
        if (paramsMap == null || paramsMap.isEmpty()) {
            return "";
        }
        try {
            StringBuilder paramsBuilder = new StringBuilder();
            for (Entry<String, String> entry : paramsMap.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    continue;
                }
                paramsBuilder.append(URLEncoder.encode(entry.getKey(), charset.name()))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), charset.name()))
                    .append("&");
            }
            if (paramsBuilder.length() > 0) {
                // 去掉末尾 &
                paramsBuilder.delete(paramsBuilder.length() - 1, paramsBuilder.length());
            }
            return paramsBuilder.toString();
        } catch (Throwable e) {
            RecordLog.warn("Encode request params fail", e);
            return "";
        }
    }

    /** HTTP 方法枚举。 */
    private enum RequestMethod {
        GET,
        POST
    }

}