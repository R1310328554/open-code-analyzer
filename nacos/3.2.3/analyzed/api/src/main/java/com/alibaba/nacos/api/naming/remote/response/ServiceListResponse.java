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
import com.alibaba.nacos.api.remote.response.ResponseCode;

import java.util.List;

/**
 * 服务列表查询响应。
 *
 * <p>返回指定命名空间/分组下的服务名列表及总数，供客户端分页浏览或批量操作。</p>
 *
 * @author xiweng.yy
 */
public class ServiceListResponse extends Response {
    
    /** 服务总数。 */
    private int count;
    
    /** 当前页服务名列表。 */
    private List<String> serviceNames;
    
    /** 无参构造，供序列化框架使用。 */
    public ServiceListResponse() {
    }
    
    /** 私有构造，封装成功响应中的计数与服务名列表。 */
    private ServiceListResponse(int count, List<String> serviceNames, String message) {
        this.count = count;
        this.serviceNames = serviceNames;
    }
    
    /** 构建携带服务列表的成功响应。 */
    public static ServiceListResponse buildSuccessResponse(int count, List<String> serviceNames) {
        return new ServiceListResponse(count, serviceNames, "success");
    }
    
    /**
     * 构建失败响应。
     *
     * @param message 错误消息
     * @return 失败响应
     */
    public static ServiceListResponse buildFailResponse(String message) {
        ServiceListResponse result = new ServiceListResponse();
        result.setErrorInfo(ResponseCode.FAIL.getCode(), message);
        return result;
    }
    
    /** 返回服务总数。 */
    public int getCount() {
        return count;
    }
    
    /** 设置服务总数。 */
    public void setCount(int count) {
        this.count = count;
    }
    
    /** 返回服务名列表。 */
    public List<String> getServiceNames() {
        return serviceNames;
    }
    
    /** 设置服务名列表。 */
    public void setServiceNames(List<String> serviceNames) {
        this.serviceNames = serviceNames;
    }
}
