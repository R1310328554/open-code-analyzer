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

/**
 * 查询配置内容的远程请求。
 *
 * <p>客户端向 Nacos 服务端拉取指定 dataId、group、tenant 下的配置正文及元数据。</p>
 *
 * @author liuzunfei
 * @version $Id: ConfigQueryRequest.java, v 0.1 2020年07月13日 9:06 PM liuzunfei Exp $
 */
public class ConfigQueryRequest extends AbstractConfigRequest {
    
    /** 配置标签，用于灰度或特殊版本标识。 */
    private String tag;
    
    /**
     * 构建配置查询请求。
     *
     * @param dataId 配置 Data ID
     * @param group  配置分组
     * @param tenant 命名空间（tenant）
     * @return 配置查询请求实例
     */
    public static ConfigQueryRequest build(String dataId, String group, String tenant) {
        ConfigQueryRequest request = new ConfigQueryRequest();
        request.setDataId(dataId);
        request.setGroup(group);
        request.setTenant(tenant);
        return request;
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
    
    /** 请求头是否标记为通知型查询（而非主动拉取）。 */
    public boolean isNotify() {
        String notify = getHeader(Constants.Config.NOTIFY_HEADER, Boolean.FALSE.toString());
        return Boolean.parseBoolean(notify);
    }
}
