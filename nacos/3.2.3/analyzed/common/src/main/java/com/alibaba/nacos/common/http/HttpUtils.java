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

package com.alibaba.nacos.common.http;

import com.alibaba.nacos.common.constant.HttpHeaderConsts;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.http.param.MediaType;
import com.alibaba.nacos.common.http.param.Query;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.common.utils.UuidUtils;
import com.alibaba.nacos.common.utils.VersionUtils;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.nacos.common.constant.RequestUrlConstants.HTTPS_PREFIX;
import static com.alibaba.nacos.common.constant.RequestUrlConstants.HTTP_PREFIX;

/**
 * Http utils.
 * <p>HTTP 请求/URL 工具集：初始化 Apache HttpClient5 请求头与实体、拼接 URL、编解码查询参数、构建 {@link URI}、判断超时异常，以及生成 Nacos 客户端标准请求头。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class HttpUtils {
    
    /** 检测路径中连续斜杠的正则，用于 {@link #buildUrl} 校验 */
    private static final Pattern CONTEXT_PATH_MATCH = Pattern.compile("(\\/)\\1+");
    
    /**
     * Init http header.
     *
     * @param requestBase requestBase {@link HttpUriRequestBase}
     * @param header      header
      * <p>HTTP 请求与 URL 工具；详见类级说明。</p>
     */
    public static void initRequestHeader(ClassicHttpRequest requestBase, Header header) {
        Iterator<Map.Entry<String, String>> iterator = header.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            requestBase.setHeader(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Init http entity.
     *
     * @param requestBase requestBase {@link HttpUriRequestBase}
     * @param body        body
     * @param header      request header
     * @throws Exception exception
      * <p>HTTP 请求与 URL 工具；详见类级说明。</p>
     */
    public static void initRequestEntity(ClassicHttpRequest requestBase, Object body, Header header)
        throws Exception {
        if (body == null) {
            return;
        }
        if (requestBase instanceof HttpEntityContainer) {
            HttpEntityContainer request = requestBase;
            MediaType mediaType = MediaType.valueOf(header.getValue(HttpHeaderConsts.CONTENT_TYPE));
            ContentType contentType =
                ContentType.create(mediaType.getType(), mediaType.getCharset());
            HttpEntity entity;
            if (body instanceof byte[]) {
                entity = new ByteArrayEntity((byte[]) body, contentType);
            } else {
                entity = new StringEntity(
                    body instanceof String ? (String) body : JacksonUtils.toJson(body),
                    contentType);
            }
            request.setEntity(entity);
        }
    }
    
    /**
     * Init request from entity map.
     *
     * @param requestBase requestBase {@link HttpUriRequestBase}
     * @param body        body map
     * @param charset     charset of entity
     * @throws Exception exception
      * <p>HTTP 请求与 URL 工具；详见类级说明。</p>
     */
    public static void initRequestFromEntity(ClassicHttpRequest requestBase,
        Map<String, String> body, String charset)
        throws Exception {
        if (body == null || body.isEmpty()) {
            return;
        }
        List<NameValuePair> params = new ArrayList<>(body.size());
        for (Map.Entry<String, String> entry : body.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        if (requestBase instanceof HttpEntityContainer) {
            HttpEntityContainer request = requestBase;
            HttpEntity entity = new UrlEncodedFormEntity(params, Charset.forName(charset));
            request.setEntity(entity);
        }
    }
    
    /**
     * Build URL.
     *
     * @param isHttps    whether is https
     * @param serverAddr server ip/address
     * @param subPaths   api path
     * @return URL string
      * <p>HTTP 请求与 URL 工具；详见类级说明。</p>
     */
    public static String buildUrl(boolean isHttps, String serverAddr, String... subPaths) {
        StringBuilder sb = new StringBuilder();
        if (isHttps) {
            sb.append(HTTPS_PREFIX);
        } else {
            sb.append(HTTP_PREFIX);
        }
        sb.append(serverAddr);
        String pre = null;
        for (String subPath : subPaths) {
            if (StringUtils.isBlank(subPath)) {
                continue;
            }
            Matcher matcher = CONTEXT_PATH_MATCH.matcher(subPath);
            if (matcher.find()) {
                throw new IllegalArgumentException("Illegal url path expression : " + subPath);
            }
            if (pre == null || !pre.endsWith("/")) {
                if (subPath.startsWith("/")) {
                    sb.append(subPath);
                } else {
                    sb.append('/').append(subPath);
                }
            } else {
                if (subPath.startsWith("/")) {
                    sb.append(subPath.replaceFirst("\\/", ""));
                } else {
                    sb.append(subPath);
                }
            }
            pre = subPath;
        }
        return sb.toString();
    }
    
    /**
     * Translate parameter map.
     *
     * @param parameterMap parameter map
     * @return parameter map
     * @throws Exception exception
      * <p>HTTP 请求与 URL 工具；详见类级说明。</p>
     */
    public static Map<String, String> translateParameterMap(Map<String, String[]> parameterMap)
        throws Exception {
        Map<String, String> map = new HashMap<>(16);
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue()[0]);
        }
        return map;
    }
    
    /**
     * Encoding parameters to url string.
     *
     * @param params   parameters
     * @param encoding encoding charset
     * @return url string
     * @throws UnsupportedEncodingException if encoding string is illegal
      * <p>HTTP 请求与 URL 工具；详见类级说明。</p>
     */
    public static String encodingParams(Map<String, String> params, String encoding)
        throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (null == params || params.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (StringUtils.isEmpty(entry.getValue())) {
                continue;
            }
            
            sb.append(entry.getKey()).append('=');
            sb.append(URLEncoder.encode(entry.getValue(), encoding));
            sb.append('&');
        }
        
        return sb.toString();
    }
    
    /**
     * Encoding KV list to url string.
     *
     * @param paramValues parameters
     * @param encoding    encoding charset
     * @return url string
     * @throws UnsupportedEncodingException if encoding string is illegal
      * <p>HTTP 请求与 URL 工具；详见类级说明。</p>
     */
    public static String encodingParams(List<String> paramValues, String encoding)
        throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (null == paramValues) {
            return null;
        }
        
        for (Iterator<String> iter = paramValues.iterator(); iter.hasNext();) {
            sb.append(iter.next()).append('=');
            sb.append(URLEncoder.encode(iter.next(), encoding));
            if (iter.hasNext()) {
                sb.append('&');
            }
        }
        return sb.toString();
    }
    
    public static String decode(String str, String encode) throws UnsupportedEncodingException {
        return innerDecode(null, str, encode);
    }
    
    /**
     * build URI By url and query.
     *
     * @param url   url
     * @param query query param {@link Query}
     * @return {@link URI}
      * <p>HTTP 请求与 URL 工具；详见类级说明。</p>
     */
    public static URI buildUri(String url, Query query) throws URISyntaxException {
        if (query != null && !query.isEmpty()) {
            url = url + "?" + query.toQueryUrl();
        }
        return new URI(url);
    }
    
    /**
     * HTTP request exception is a timeout exception.
     *
     * @param throwable http request throwable
     * @return boolean
      * <p>HTTP 请求与 URL 工具；详见类级说明。</p>
     */
    public static boolean isTimeoutException(Throwable throwable) {
        return throwable instanceof SocketTimeoutException
            || throwable instanceof ConnectTimeoutException
            || throwable instanceof TimeoutException
            || throwable.getCause() instanceof TimeoutException;
    }
    
    /**
     * Build header.
     *
     * @return header
      * <p>HTTP 请求与 URL 工具；详见类级说明。</p>
     */
    public static Header builderHeader(String module) {
        Header header = Header.newInstance();
        header.addParam(HttpHeaderConsts.CLIENT_VERSION_HEADER, VersionUtils.version);
        header.addParam(HttpHeaderConsts.USER_AGENT_HEADER, VersionUtils.getFullClientVersion());
        header.addParam(HttpHeaderConsts.ACCEPT_ENCODING, "gzip,deflate,sdch");
        header.addParam(HttpHeaderConsts.CONNECTION, "Keep-Alive");
        header.addParam(HttpHeaderConsts.REQUEST_ID, UuidUtils.generateUuid());
        header.addParam(HttpHeaderConsts.REQUEST_MODULE, module);
        return header;
    }
    
    private static String innerDecode(String pre, String now, String encode)
        throws UnsupportedEncodingException {
        // 数据可能被 URL 编码多次，需递归解码直至结果稳定
        if (StringUtils.equals(pre, now)) {
            return pre;
        }
        pre = now;
        now = URLDecoder.decode(now, encode);
        return innerDecode(pre, now, encode);
    }
    
}
