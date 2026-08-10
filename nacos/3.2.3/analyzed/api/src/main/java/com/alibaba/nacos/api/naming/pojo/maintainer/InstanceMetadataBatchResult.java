/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.naming.pojo.maintainer;

import java.io.Serializable;
import java.util.List;

/**
 * 实例元数据批量操作结果，包含已成功更新的实例 IP 列表。
 *
 * @author xiweng.yy
 */
public class InstanceMetadataBatchResult implements Serializable {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = -5793871911227035729L;
    
    /** 本次批量更新成功的实例 IP 列表。 */
    private List<String> updated;
    
    /** 无参构造。 */
    public InstanceMetadataBatchResult() {
    }
    
    /**
     * 指定已更新实例 IP 列表构造结果。
     *
     * @param updated 已更新的 IP 列表
     */
        this.updated = updated;
    }
    
    /** 获取已更新实例 IP 列表。 */
    public List<String> getUpdated() {
        return updated;
    }
    
    /** 设置已更新实例 IP 列表。 */
    public void setUpdated(List<String> updated) {
        this.updated = updated;
    }
}
