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

package com.alibaba.nacos.config.server.model;

/**
 * Beta 灰度配置包装类：在 {@link ConfigInfo4Beta} 基础上附加最后修改时间戳。
 * 用于服务端向客户端或 API 返回带版本信息的 Beta 配置快照。
 * ConfigInfoBetaWrapper.
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ConfigInfoBetaWrapper extends ConfigInfo4Beta {
    
    private static final long serialVersionUID = 4511997359365712505L;
    
    /** 配置内容最后修改时间（毫秒时间戳） */
    private long lastModified;
    
    /** 无参构造，供序列化与框架反射使用 */
    public ConfigInfoBetaWrapper() {
    }
    
    /** 获取最后修改时间戳 */
    public long getLastModified() {
        return lastModified;
    }
    
    /** 设置最后修改时间戳 */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
