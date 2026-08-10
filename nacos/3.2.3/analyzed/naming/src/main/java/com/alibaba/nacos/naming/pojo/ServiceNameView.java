/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.pojo;

import java.util.Collection;

/**
 * 服务名列表视图对象。
 *
 * <p>封装分页或过滤后的服务名集合及总数 count，供 OpenAPI 服务列表查询响应序列化。</p>
 *
 * @author xiweng.yy
 */
public class ServiceNameView {
    
    private int count;
    
    private Collection<String> services;
    
    /** 返回当前结果集中的服务数量。 */
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    /** 返回服务名集合。 */
    public Collection<String> getServices() {
        return services;
    }
    
    public void setServices(Collection<String> services) {
        this.services = services;
    }
}
