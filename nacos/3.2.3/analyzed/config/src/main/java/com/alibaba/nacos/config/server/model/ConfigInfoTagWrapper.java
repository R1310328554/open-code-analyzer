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
 * Tag 标签配置包装类：在 {@link ConfigInfo4Tag} 上附加最后修改时间。
 * 用于按 tag 维度查询或推送配置时携带版本信息。
 * ConfigInfoTagWrapper.
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ConfigInfoTagWrapper extends ConfigInfo4Tag {
    
    private static final long serialVersionUID = 4511997359365712505L;
    
    /** 标签配置最后修改时间（毫秒） */
    private long lastModified;
    
    /** 无参构造 */
    public ConfigInfoTagWrapper() {
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
