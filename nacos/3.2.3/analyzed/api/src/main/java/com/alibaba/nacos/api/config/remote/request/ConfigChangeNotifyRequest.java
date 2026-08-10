/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.remote.request.ServerRequest;

/**
 * 配置变更通知请求，由服务端主动推送给客户端。
 *
 * <p>告知指定 dataId/group/tenant 的配置已发生变更，客户端应拉取最新内容。</p>
 *
 * @author liuzunfei
 * @version $Id: ConfigChangeNotifyRequest.java, v 0.1 2020年07月14日 3:20 PM liuzunfei Exp $
 */
public class ConfigChangeNotifyRequest extends ServerRequest {
    
    /** 发生变更的配置 Data ID。 */
    String dataId;
    
    /** 配置分组。 */
    String group;
    
    /** 命名空间 ID。 */
    String tenant;
    
    /** 获取 Data ID。 */
    public String getDataId() {
        return dataId;
    }
    
    /** 设置 Data ID。 */
    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
    
    /** 获取配置分组。 */
    public String getGroup() {
        return group;
    }
    
    /** 设置配置分组。 */
    public void setGroup(String group) {
        this.group = group;
    }
    
    /** 获取命名空间 ID。 */
    public String getTenant() {
        return tenant;
    }
    
    /** 设置命名空间 ID。 */
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
    
    /**
     * 构造配置变更通知请求。
     *
     * @param dataId 配置 Data ID
     * @param group  配置分组
     * @param tenant 命名空间 ID
     * @return 填充完毕的通知请求
     */
    public static ConfigChangeNotifyRequest build(String dataId, String group, String tenant) {
        ConfigChangeNotifyRequest request = new ConfigChangeNotifyRequest();
        request.setDataId(dataId);
        request.setGroup(group);
        request.setTenant(tenant);
        return request;
    }
    
    /** 返回配置模块标识。 */
    @Override
    public String getModule() {
        return Constants.Config.CONFIG_MODULE;
    }
}
