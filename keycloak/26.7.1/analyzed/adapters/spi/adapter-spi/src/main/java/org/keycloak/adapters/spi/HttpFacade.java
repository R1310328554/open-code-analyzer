/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.adapters.spi;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.security.cert.X509Certificate;

/**
 * 适配器 SPI 中的 HTTP 请求/响应抽象门面。
 *
 * <p>屏蔽 Servlet、Undertow、Vert.x 等容器差异，使核心适配器逻辑与具体 Web 栈解耦。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface HttpFacade {
    /** 返回当前 HTTP 请求视图。 */
    Request getRequest();

    /** 返回当前 HTTP 响应视图。 */
    Response getResponse();

    /** 返回客户端 TLS 证书链（若存在）。 */
    X509Certificate[] getCertificateChain();

    /** HTTP 请求只读视图。 */
    interface Request {

        /** 返回 HTTP 方法（GET、POST 等）。 */
        String getMethod();
        /**
         * 返回含查询参数的完整请求 URI。
         *
         * @return 完整 URI 字符串
         */
        String getURI();

        /**
         * 返回相对于上下文根的路径。
         *
         * @return 相对路径
         */
        String getRelativePath();

        /**
         * 当前连接是否使用 HTTPS。
         *
         * @return 若为 HTTPS 则返回 {@code true}
         */
        boolean isSecure();

        /**
         * 获取查询字符串或表单中的首个同名参数值。
         *
         * @param param 参数名
         * @return 参数值，若无则返回 {@code null}
         */
        String getFirstParam(String param);
        /** 获取指定查询参数的值。 */
        String getQueryParamValue(String param);
        /** 按名称获取 Cookie。 */
        Cookie getCookie(String cookieName);
        /** 获取首个同名请求头。 */
        String getHeader(String name);
        /** 获取全部同名请求头。 */
        List<String> getHeaders(String name);
        /** 返回请求体输入流。 */
        InputStream getInputStream();
        /** 返回请求体输入流，可选缓冲。 */
        InputStream getInputStream(boolean buffered);

        /** 返回客户端远程地址。 */
        String getRemoteAddr();
        /** 在请求上附加认证错误属性。 */
        void setError(AuthenticationError error);
        /** 在请求上附加登出错误属性。 */
        void setError(LogoutError error);
    }

    /** HTTP 响应可变视图。 */
    interface Response {
        /** 设置 HTTP 状态码。 */
        void setStatus(int status);
        /** 追加响应头（允许多值）。 */
        void addHeader(String name, String value);
        /** 设置响应头（覆盖已有值）。 */
        void setHeader(String name, String value);
        /** 清除指定 Cookie。 */
        void resetCookie(String name, String path);
        /** 设置 Cookie。 */
        void setCookie(String name, String value, String path, String domain, int maxAge, boolean secure, boolean httpOnly);
        /** 返回响应体输出流。 */
        OutputStream getOutputStream();
        /** 发送带状态码的错误响应。 */
        void sendError(int code);
        /** 发送带状态码与消息的错误响应。 */
        void sendError(int code, String message);

        /**
         * 若响应尚未结束则完成并刷新输出。
         */
        void end();
    }

    /** HTTP Cookie 值对象。 */
    public class Cookie {
        protected String name;
        protected String value;
        protected int version;
        protected String domain;
        protected String path;

        /** 构造 Cookie 实例。 */
        public Cookie(String name, String value, int version, String domain, String path) {
            this.name = name;
            this.value = value;
            this.version = version;
            this.domain = domain;
            this.path = path;
        }

        /** 返回 Cookie 名称。 */
        public String getName() {
            return name;
        }

        /** 返回 Cookie 值。 */
        public String getValue() {
            return value;
        }

        /** 返回 Cookie 版本号。 */
        public int getVersion() {
            return version;
        }

        /** 返回 Cookie 域。 */
        public String getDomain() {
            return domain;
        }

        /** 返回 Cookie 路径。 */
        public String getPath() {
            return path;
        }
    }
}
