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

package com.alibaba.nacos.common.model;

import java.io.Serializable;

/**
 * Rest result.
 * <p>通用 REST 响应封装：包含业务码 code、消息 message 与泛型数据 data，提供 {@link #ok()}、{@link #isNoRight()} 等便捷判断。</p>
 * <p>TODO replaced or extend by {@link com.alibaba.nacos.api.model.v2.Result}.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class RestResult<T> implements Serializable {
    
    private static final long serialVersionUID = 6095433538316185017L;
    
    /** 业务/HTTP 状态码，0 或 200 视为成功 */
    private int code;
    
    /** 错误或提示信息 */
    private String message;
    
    /** 响应载荷 */
    private T data;
    
    public RestResult() {
    }
    
    public RestResult(int code, String message, T data) {
        this.code = code;
        this.setMessage(message);
        this.data = data;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    /** 判断请求是否成功（code 为 0 或 200） */
    public boolean ok() {
        return this.code == 0 || this.code == 200;
    }
    
    /** 判断是否为未授权/无权限（401 或 403） */
    public boolean isNoRight() {
        return this.code == 403 || this.code == 401;
    }
    
    @Override
    public String toString() {
        return "RestResult{" + "code=" + code + ", message='" + message + '\'' + ", data=" + data
            + '}';
    }
    
    public static <T> ResResultBuilder<T> builder() {
        return new ResResultBuilder<>();
    }
    
    /** 流式构建 {@link RestResult} 的内部 Builder */
    public static final class ResResultBuilder<T> {
        
        private int code;
        
        private String errMsg;
        
        private T data;
        
        private ResResultBuilder() {
        }
        
        public ResResultBuilder<T> withCode(int code) {
            this.code = code;
            return this;
        }
        
        public ResResultBuilder<T> withMsg(String errMsg) {
            this.errMsg = errMsg;
            return this;
        }
        
        public ResResultBuilder<T> withData(T data) {
            this.data = data;
            return this;
        }
        
        /**
         * Build result.
         * <p>组装并返回不可变语义上的 {@link RestResult} 实例。</p>
         *
         * @return result
         */
        public RestResult<T> build() {
            RestResult<T> restResult = new RestResult<>();
            restResult.setCode(code);
            restResult.setMessage(errMsg);
            restResult.setData(data);
            return restResult;
        }
    }
}
