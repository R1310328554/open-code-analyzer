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

/**
 * 批量实例操作远程响应。
 *
 * <p>继承 {@link InstanceResponse}，用于批量注册/注销等操作的 gRPC 应答；{@link #type} 标识具体操作类型。</p>
 *
 * @author <a href="mailto:chenhao26@xiaomi.com">chenhao26</a>
 */
public class BatchInstanceResponse extends InstanceResponse {
    
    /** 无参构造，表示默认成功响应。 */
    public BatchInstanceResponse() {
        super();
    }
    
    /**
     * 构造带操作类型的批量实例响应。
     *
     * @param type 批量操作类型
     */
    public BatchInstanceResponse(String type) {
        super(type);
    }
}
