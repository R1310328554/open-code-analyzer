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

package com.alibaba.nacos.api.naming.remote.response;

import com.alibaba.nacos.api.remote.response.Response;

/**
 * 单实例操作远程响应。
 *
 * <p>客户端发起实例注册/注销等请求后，服务端返回此 {@link Response}；{@link #type} 回显或标识操作类型。</p>
 *
 * @author xiweng.yy
 */
public class InstanceResponse extends Response {
    
    /** 实例操作类型。 */
    private String type;
    
    /** 无参构造，表示默认成功响应。 */
    public InstanceResponse() {
    }
    
    /**
     * 构造带操作类型的实例响应。
     *
     * @param type 操作类型
     */
    public InstanceResponse(String type) {
        this.type = type;
    }
    
    /** 设置操作类型。 */
    public void setType(String type) {
        this.type = type;
    }
    
    /** 返回操作类型。 */
    public String getType() {
        return type;
    }
}
