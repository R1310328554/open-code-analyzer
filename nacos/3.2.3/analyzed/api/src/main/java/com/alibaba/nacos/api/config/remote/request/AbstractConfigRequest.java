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

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.remote.request.Request;

/**
 * 配置模块远程请求的抽象基类，所有配置相关客户端请求应继承本类。
 *
 * <p>统一携带 dataId、group、tenant 三元组及模块标识。</p>
 *
 * @author liuzunfei
 * @version $Id: ConfigCommonRequest.java, v 0.1 2020年07月13日 9:05 PM liuzunfei Exp $
 */
public abstract class AbstractConfigRequest extends Request {
    
    /** 配置 Data ID。 */
    private String dataId;
    
    /** 配置分组。 */
    private String group;
    
    /** 命名空间（租户）ID。 */
    private String tenant;
    
    /** 获取配置 Data ID。 */
    public String getDataId() {
        return dataId;
    }
    
    /** 设置配置 Data ID。 */
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
    
    /** 返回配置模块标识。 */
    @Override
    public String getModule() {
        return Constants.Config.CONFIG_MODULE;
    }
}
