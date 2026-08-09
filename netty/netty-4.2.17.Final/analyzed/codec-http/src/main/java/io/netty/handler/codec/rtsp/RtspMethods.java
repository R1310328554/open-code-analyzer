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
package io.netty.handler.codec.rtsp;

import static io.netty.util.internal.ObjectUtil.checkNonEmptyAfterTrim;

import io.netty.handler.codec.http.HttpMethod;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * RTSP 请求方法常量（RFC 2326）。
 * <p>标准方法缓存在 {@link #methodMap} 中，{@link #valueOf(String)} 按 US Locale 大写查找。
 */
public final class RtspMethods {

    /**
     * OPTIONS：查询服务器或资源支持的通信选项，不触发媒体传输。
     */
    public static final HttpMethod OPTIONS = HttpMethod.OPTIONS;

    /**
     * DESCRIBE：获取指定 URI 的 SDP 媒体描述。
     */
    public static final HttpMethod DESCRIBE = HttpMethod.valueOf("DESCRIBE");

    /**
     * ANNOUNCE：向服务器发布或实时更新 SDP 描述。
     */
    public static final HttpMethod ANNOUNCE = HttpMethod.valueOf("ANNOUNCE");

    /**
     * SETUP：为 URI 指定 RTP/RTCP 传输机制并建立会话。
     */
    public static final HttpMethod SETUP = HttpMethod.valueOf("SETUP");

    /**
     * PLAY：按 SETUP 协商的传输方式开始发送媒体数据。
     */
    public static final HttpMethod PLAY = HttpMethod.valueOf("PLAY");

    /**
     * PAUSE：临时中断媒体流传输。
     */
    public static final HttpMethod PAUSE = HttpMethod.valueOf("PAUSE");

    /**
     * TEARDOWN：停止流传输并释放 URI 关联的资源。
     */
    public static final HttpMethod TEARDOWN = HttpMethod.valueOf("TEARDOWN");

    /**
     * GET_PARAMETER：读取演示或流的参数值。
     */
    public static final HttpMethod GET_PARAMETER = HttpMethod.valueOf("GET_PARAMETER");

    /**
     * SET_PARAMETER：设置演示或流的参数值。
     */
    public static final HttpMethod SET_PARAMETER = HttpMethod.valueOf("SET_PARAMETER");

    /**
     * REDIRECT：通知客户端连接至新的服务器地址。
     */
    public static final HttpMethod REDIRECT = HttpMethod.valueOf("REDIRECT");

    /**
     * RECORD：按 SDP 描述开始录制指定范围的媒体数据。
     */
    public static final HttpMethod RECORD = HttpMethod.valueOf("RECORD");

    private static final Map<String, HttpMethod> methodMap = new HashMap<String, HttpMethod>();

    static {
        methodMap.put(DESCRIBE.toString(), DESCRIBE);
        methodMap.put(ANNOUNCE.toString(), ANNOUNCE);
        methodMap.put(GET_PARAMETER.toString(), GET_PARAMETER);
        methodMap.put(OPTIONS.toString(), OPTIONS);
        methodMap.put(PAUSE.toString(), PAUSE);
        methodMap.put(PLAY.toString(), PLAY);
        methodMap.put(RECORD.toString(), RECORD);
        methodMap.put(REDIRECT.toString(), REDIRECT);
        methodMap.put(SETUP.toString(), SETUP);
        methodMap.put(SET_PARAMETER.toString(), SET_PARAMETER);
        methodMap.put(TEARDOWN.toString(), TEARDOWN);
    }

    /**
     * 按名称返回 {@link HttpMethod}；标准 RTSP 方法返回缓存实例，否则新建。
     */
    public static HttpMethod valueOf(String name) {
        // RFC 2326 方法名为 ASCII token；必须用 Locale.US 大写，否则土耳其语 locale
        // 会把 'i' 映射为 'İ'，导致 "describe"/"redirect" 等查找失败
        name = checkNonEmptyAfterTrim(name, "name").toUpperCase(Locale.US);
        HttpMethod result = methodMap.get(name);
        if (result != null) {
            return result;
        } else {
            return HttpMethod.valueOf(name);
        }
    }

    private RtspMethods() {
    }
}
