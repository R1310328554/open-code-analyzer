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

package com.alibaba.nacos.api.config.remote.request;

/**
 * 删除配置的远程请求。
 *
 * <p>客户端请求 Nacos 服务端移除指定 dataId、group、tenant 下的配置项。</p>
 *
 * @author liuzunfei
 * @version $Id: ConfigRemoveRequest.java, v 0.1 2020年07月16日 4:31 PM liuzunfei Exp $
 */
public class ConfigRemoveRequest extends AbstractConfigRequest {
    
    /** 配置标签，用于灰度或特殊版本标识。 */
    String tag;
    
    /** 无参构造，供序列化或框架实例化使用。 */
    public ConfigRemoveRequest() {
        
    }
    
    /**
     * 构造带完整定位信息的删除请求。
     *
     * @param dataId 配置 Data ID
     * @param group  配置分组
     * @param tenant 命名空间（tenant）
     * @param tag    配置标签
     */
    public ConfigRemoveRequest(String dataId, String group, String tenant, String tag) {
        super.setDataId(dataId);
        super.setGroup(group);
        super.setTenant(tenant);
        this.tag = tag;
    }
    
    /**
     * 获取配置标签。
     *
     * @return 标签值
     */
    public String getTag() {
        return tag;
    }
    
    /**
     * 设置配置标签。
     *
     * @param tag 标签值
     */
    public void setTag(String tag) {
        this.tag = tag;
    }
    
}
