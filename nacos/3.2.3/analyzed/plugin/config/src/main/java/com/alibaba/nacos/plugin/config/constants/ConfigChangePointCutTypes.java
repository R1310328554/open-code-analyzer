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

package com.alibaba.nacos.plugin.config.constants;

/**
 * 配置变更切点类型枚举。
 *
 * <p>每种类型对应一种配置变更入口（HTTP、RPC 或未知来源），用于插件声明其拦截范围。</p>
 *
 * @author liyunfei
 */
public enum ConfigChangePointCutTypes {
    
    /**
     * 通过 HTTP 发布或更新配置。
     */
    PUBLISH_BY_HTTP("publishOrUpdateByHttp"),
    /**
     * 通过 RPC 发布配置。
     */
    PUBLISH_BY_RPC("publishOrUpdateByRpc"),
    /**
     * 来源未知的配置发布。
     */
    PUBLISH_BY_UNKNOWN("publishOrUpdateByUnknown"),
    /**
     * 通过 HTTP 按 ID 删除单条配置。
     */
    REMOVE_BY_HTTP("removeSingleByHttp"),
    /**
     * 通过 RPC 删除单条配置。
     */
    REMOVE_BY_RPC("removeSingleByRpc"),
    /**
     * 来源未知的单条配置删除。
     */
    REMOVE_BY_UNKNOWN("removeSingleByUnknown"),
    /**
     * 通过 HTTP/控制台导入配置文件。
     */
    IMPORT_BY_HTTP("importFileByHttp"),
    /**
     * 通过 HTTP 批量删除配置。
     */
    REMOVE_BATCH_HTTP("removeBatchByHttp");
    
    /** 切点对应的内部方法标识字符串。 */
    private final String value;
    
    ConfigChangePointCutTypes(String value) {
        this.value = value;
    }
    
    /**
     * 返回切点方法标识字符串。
     *
     * @return 切点 value
     */
    public String value() {
        return value;
    }
}
