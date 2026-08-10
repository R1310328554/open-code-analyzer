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

package com.alibaba.nacos.core.model.request;

/**
 * 更新集群成员发现（Lookup）类型的 HTTP 请求体。
 * <p>type 字段指定新的 Lookup 实现类型（如 file、address-server）。</p>
 * Update member lookup type.
 *
 * @author wuzhiguo
 */
public class LookupUpdateRequest {
    
    /** 成员发现机制类型。 */
    private String type;
    
    /** 获取 Lookup 类型。 */
    public String getType() {
        return type;
    }
    
    /** 设置 Lookup 类型。 */
    public void setType(String type) {
        this.type = type;
    }
}
