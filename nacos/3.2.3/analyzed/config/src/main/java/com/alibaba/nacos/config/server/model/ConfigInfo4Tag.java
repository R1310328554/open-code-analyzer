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
 * 标签维度配置实体：继承 {@link ConfigInfo} 并附加 {@code tag} 字段，
 * 支持同一 dataId/group 下按标签隔离不同配置版本。
 * ConfigInfo4Tag.
 *
 * @author Nacos
 */
public class ConfigInfo4Tag extends ConfigInfo {
    
    private static final long serialVersionUID = 296578467953931353L;
    
    /** 配置标签名，与 dataId/group 共同构成唯一键 */
    private String tag;
    
    public ConfigInfo4Tag() {
    }
    
    /** 以 dataId、group、tag、appName 与 content 构造标签配置实体 */
    public ConfigInfo4Tag(String dataId, String group, String tag, String appName, String content) {
        super(dataId, group, appName, content);
        this.tag = tag;
    }
    
    public String getTag() {
        return tag;
    }
    
    public void setTag(String tag) {
        this.tag = tag;
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
