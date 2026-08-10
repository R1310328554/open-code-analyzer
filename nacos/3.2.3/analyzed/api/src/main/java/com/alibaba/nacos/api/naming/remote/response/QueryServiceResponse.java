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

import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.api.remote.response.ResponseCode;

/**
 * Nacos 命名服务查询响应。
 *
 * <p>客户端发起服务查询请求后，服务端返回此 {@link Response}，成功时携带 {@link ServiceInfo} 实例列表与元数据。</p>
 *
 * @author xiweng.yy
 */
public class QueryServiceResponse extends Response {
    
    /** 查询到的服务详情（含实例列表）。 */
    private ServiceInfo serviceInfo;
    
    /** 无参构造，供序列化框架使用。 */
    public QueryServiceResponse() {
    }
    
    /** 私有构造，封装成功响应中的服务信息。 */
    private QueryServiceResponse(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
    
    /**
     * 构建成功响应。
     *
     * @param serviceInfo 服务详情
     * @return 服务查询响应
     */
    public static QueryServiceResponse buildSuccessResponse(ServiceInfo serviceInfo) {
        return new QueryServiceResponse(serviceInfo);
    }
    
    /**
     * 构建失败响应。
     *
     * @param message 错误消息
     * @return 服务查询响应
     */
    public static QueryServiceResponse buildFailResponse(String message) {
        QueryServiceResponse queryServiceResponse = new QueryServiceResponse();
        queryServiceResponse.setResultCode(ResponseCode.FAIL.getCode());
        queryServiceResponse.setMessage(message);
        return queryServiceResponse;
    }
    
    /** 返回服务详情。 */
    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }
    
    /** 设置服务详情。 */
    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
}
