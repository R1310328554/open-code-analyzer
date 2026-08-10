/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.api.remote.request.ServerRequest;

import static com.alibaba.nacos.api.common.Constants.Naming.NAMING_MODULE;

/**
 * 模糊订阅（Fuzzy Watch）通知请求的抽象基类。
 *
 * <p>携带同步类型 {@link #syncType}，模块固定为命名模块。</p>
 *
 * @author tanyongquan
 */
public abstract class AbstractFuzzyWatchNotifyRequest extends ServerRequest {
    
    /** 模糊订阅同步类型标识。 */
    private String syncType;
    
    /** 无参构造。 */
    public AbstractFuzzyWatchNotifyRequest() {
    }
    
    /**
     * 指定同步类型构造。
     *
     * @param syncType 同步类型
     */
        this.syncType = syncType;
    }
    
    /** 获取同步类型。 */
    public String getSyncType() {
        return syncType;
    }
    
    /** 设置同步类型。 */
    public void setSyncType(String syncType) {
        this.syncType = syncType;
    }
    
    /** 返回命名模块标识 {@link com.alibaba.nacos.api.common.Constants.Naming#NAMING_MODULE}。 */
    @Override
    public String getModule() {
        return NAMING_MODULE;
    }
}
