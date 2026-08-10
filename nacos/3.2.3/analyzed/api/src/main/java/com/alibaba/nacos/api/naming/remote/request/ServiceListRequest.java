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

package com.alibaba.nacos.api.naming.remote.request;

/**
 * 命名服务列表分页查询远程请求。
 *
 * <p>按命名空间与分组查询服务名列表，支持 {@link #pageNo}/{@link #pageSize} 分页及 {@link #selector} 过滤表达式。</p>
 *
 * @author xiweng.yy
 */
public class ServiceListRequest extends AbstractNamingRequest {
    
    /** 页码（从 1 起）。 */
    private int pageNo;
    
    /** 每页条数。 */
    private int pageSize;
    
    /** 服务选择器/过滤表达式（可为 {@code null}）。 */
    private String selector;
    
    /** 无参构造，供序列化使用。 */
    public ServiceListRequest() {
    }
    
    /**
     * 构造分页服务列表请求。
     *
     * @param namespace 命名空间 ID
     * @param groupName 分组名
     * @param pageNo    页码
     * @param pageSize  每页条数
     */
    public ServiceListRequest(String namespace, String groupName, int pageNo, int pageSize) {
        super(namespace, "", groupName);
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }
    
    /** 返回页码。 */
    public int getPageNo() {
        return pageNo;
    }
    
    /** 设置页码。 */
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }
    
    /** 返回每页条数。 */
    public int getPageSize() {
        return pageSize;
    }
    
    /** 设置每页条数。 */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    /** 返回服务选择器表达式。 */
    public String getSelector() {
        return selector;
    }
    
    /** 设置服务选择器表达式。 */
    public void setSelector(String selector) {
        this.selector = selector;
    }
}
