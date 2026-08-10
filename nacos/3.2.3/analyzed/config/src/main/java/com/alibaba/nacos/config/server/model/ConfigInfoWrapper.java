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
 * 正式配置包装类：在 {@link ConfigInfo} 上附加 {@code lastModified} 时间戳。
 * 服务端查询、缓存同步及 Open API 返回常用此类型。
 * ConfigInfo Wrapper.
 *
 * @author Nacos
 */
public class ConfigInfoWrapper extends ConfigInfo {
    
    private static final long serialVersionUID = 4511997359365712505L;
    
    /** 配置最后修改时间（毫秒） */
    private long lastModified;
    
    /** 无参构造 */
    public ConfigInfoWrapper() {
    }
    
    /** 获取最后修改时间 */
    public long getLastModified() {
        return lastModified;
    }
    
    /** 设置最后修改时间 */
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
