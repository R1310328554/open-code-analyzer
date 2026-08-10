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

import com.alibaba.nacos.common.utils.HttpMethod;
import com.alibaba.nacos.common.utils.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpTrace;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

import java.net.URI;

/**
 * Base http method.
 * <p>HTTP 动词与 Apache HttpClient 5 {@link org.apache.hc.client5.http.classic.methods.HttpUriRequestBase} 的映射枚举；含带请求体的 GET/DELETE 大参数变体。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public enum BaseHttpMethod {
    
    /**
     * get request.
     * <p>标准 GET 请求。</p>
     */
    GET(HttpMethod.GET) {
        
        @Override
        protected HttpUriRequestBase createRequest(String url) {
            return new HttpGet(url);
        }
    },
    
    GET_LARGE(HttpMethod.GET_LARGE) {
        
        @Override
        protected HttpUriRequestBase createRequest(String url) {
            return new HttpGetWithEntity(url);
        }
    },
    
    /**
     * post request.
     * <p>POST 请求。</p>
     */
    POST(HttpMethod.POST) {
        
        @Override
        protected HttpUriRequestBase createRequest(String url) {
            return new HttpPost(url);
        }
    },
    
    /**
     * put request.
     * <p>PUT 请求。</p>
     */
    PUT(HttpMethod.PUT) {
        
        @Override
        protected HttpUriRequestBase createRequest(String url) {
            return new HttpPut(url);
        }
    },
    
    /**
     * delete request.
     * <p>标准 DELETE 请求。</p>
     */
    DELETE(HttpMethod.DELETE) {
        
        @Override
        protected HttpUriRequestBase createRequest(String url) {
            return new HttpDelete(url);
        }
    },
    
    /**
     * delete Large request.
     * <p>带请求体的大参数 DELETE 请求。</p>
     */
    DELETE_LARGE(HttpMethod.DELETE_LARGE) {
        
        @Override
        protected HttpUriRequestBase createRequest(String url) {
            return new HttpDeleteWithEntity(url);
        }
    },
    
    /**
     * head request.
     * <p>HEAD 请求。</p>
     */
    HEAD(HttpMethod.HEAD) {
        
        @Override
        protected HttpUriRequestBase createRequest(String url) {
            return new HttpHead(url);
        }
    },
    
    /**
     * trace request.
     * <p>TRACE 请求。</p>
     */
    TRACE(HttpMethod.TRACE) {
        
        @Override
        protected HttpUriRequestBase createRequest(String url) {
            return new HttpTrace(url);
        }
    },
    
    /**
     * patch request.
     * <p>PATCH 请求。</p>
     */
    PATCH(HttpMethod.PATCH) {
        
        @Override
        protected HttpUriRequestBase createRequest(String url) {
            return new HttpPatch(url);
        }
    },
    
    /**
     * options request.
     * <p>OPTIONS 请求（当前实现复用 HttpTrace）。</p>
     */
    OPTIONS(HttpMethod.OPTIONS) {
        
        @Override
        protected HttpUriRequestBase createRequest(String url) {
            return new HttpTrace(url);
        }
    };
    
    /** 对应 {@link com.alibaba.nacos.common.utils.HttpMethod} 中的方法名字符串 */
    private String name;
    
    /** @param name HTTP 方法名 */
    BaseHttpMethod(String name) {
        this.name = name;
    }
    
    /** 根据 URL 创建对应的 HttpClient 请求对象 */
    public HttpUriRequestBase init(String url) {
        return createRequest(url);
    }
    
    protected HttpUriRequestBase createRequest(String url) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Value of {@link BaseHttpMethod}.
     * <p>按方法名（忽略大小写）解析枚举值。</p>
     *
     * @param name method name
     * @return {@link BaseHttpMethod}
     */
    public static BaseHttpMethod sourceOf(String name) {
        for (BaseHttpMethod method : BaseHttpMethod.values()) {
            if (StringUtils.equalsIgnoreCase(name, method.name)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unsupported http method : " + name);
    }
    
    /**
     * get Large implemented.
     * <p>
     * Mainly used for GET request parameters are relatively large, can not be placed on the URL, so it needs to be
     * placed in the body.
     * </p>
     * <p>带实体体的 GET：参数过大无法放在 URL 查询串时放入请求体。</p>
     */
    public static class HttpGetWithEntity extends HttpUriRequestBase {
        
        public static final String METHOD_NAME = "GET";
        
        public HttpGetWithEntity(String url) {
            super(METHOD_NAME, URI.create(url));
        }
    }
    
    /**
     * delete Large implemented.
     * <p>
     * Mainly used for DELETE request parameters are relatively large, can not be placed on the URL, so it needs to be
     * placed in the body.
     * </p>
     * <p>带实体体的 DELETE：大参数放入请求体而非 URL。</p>
     */
    public static class HttpDeleteWithEntity extends HttpUriRequestBase {
        
        public static final String METHOD_NAME = "DELETE";
        
        public HttpDeleteWithEntity(String url) {
            super(METHOD_NAME, URI.create(url));
        }
    }
    
}
