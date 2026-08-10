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

import java.io.Serializable;
import java.util.Objects;

/**
 * 配置状态快照包装：聚合主键、三元组、MD5、修改时间及可选灰度名。
 * 用于长轮询 listen 接口返回客户端比对用的轻量状态体。
 * ConfigInfoStateWrapper. include id，dataId，group，tenant，lastModified.
 *
 * @author zunfei.lzf
 */
public class ConfigInfoStateWrapper implements Serializable {
    
    /** 配置持久化主键 ID */
    private long id;
    
    /** 配置 dataId */
    private String dataId;
    
    /** 配置 group */
    private String group;
    
    /** 命名空间/租户 */
    private String tenant;
    
    /** 最后修改时间戳 */
    private long lastModified;
    
    /** 配置内容 MD5 摘要，供客户端比对 */
    private String md5;
    
    /** 关联灰度版本名（非灰度配置可为空） */
    private String grayName;
    
    /** 获取配置 ID */
    public long getId() {
        return id;
    }
    
    /** 设置配置 ID */
    public void setId(long id) {
        this.id = id;
    }
    
    public String getDataId() {
        return dataId;
    }
    
    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
    
    public long getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
    
    public String getTenant() {
        return tenant;
    }
    
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
    
    public String getGrayName() {
        return grayName;
    }
    
    public void setGrayName(String grayName) {
        this.grayName = grayName;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConfigInfoStateWrapper that = (ConfigInfoStateWrapper) o;
        return id == that.id && lastModified == that.lastModified
            && Objects.equals(dataId, that.dataId)
            && Objects.equals(group, that.group) && Objects.equals(tenant, that.tenant)
            && Objects.equals(md5,
                that.md5);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(dataId, group, tenant);
    }
    
    /** 获取内容 MD5 */
    public String getMd5() {
        return md5;
    }
    
    /** 设置内容 MD5 */
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    
}
