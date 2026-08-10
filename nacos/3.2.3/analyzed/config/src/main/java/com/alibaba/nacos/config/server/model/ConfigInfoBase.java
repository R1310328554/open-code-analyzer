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

import com.alibaba.nacos.common.utils.MD5Utils;
import com.alibaba.nacos.config.server.constant.Constants;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Objects;

/**
 * 配置基础实体：包含 id、dataId、group、content、md5 与加密密钥，
 * 字段集合固定以兼容旧版 Open API，不可随意增删字段。
 * ConfigInfoBase.
 * And can't add field, to compatible with old interface(If adding a field, then it will occur compatibility problems).
 *
 * @author Nacos
 */
public class ConfigInfoBase implements Serializable, Comparable<ConfigInfoBase> {
    
    static final long serialVersionUID = 265316491795790798L;
    
    @JsonSerialize(using = ToStringSerializer.class)
    /** 数据库主键 ID */
    private long id;
    
    /** 配置 dataId，业务唯一标识之一 */
    private String dataId;
    
    /** 配置 group，默认 {@link com.alibaba.nacos.config.server.constant.Constants#DEFAULT_GROUP} */
    private String group;
    
    /** 配置内容正文 */
    private String content;
    
    /** 配置内容 MD5 摘要，用于客户端变更检测 */
    private String md5;
    
    /** 加密配置的数据密钥标识 */
    private String encryptedDataKey;
    
    public ConfigInfoBase() {
        
    }
    
    /** 构造基础配置并自动按 {@link Constants#PERSIST_ENCODE} 计算 MD5 */
    public ConfigInfoBase(String dataId, String group, String content) {
        this.dataId = dataId;
        this.group = group;
        this.content = content;
        if (this.content != null) {
            this.md5 = MD5Utils.md5Hex(this.content, Constants.PERSIST_ENCODE);
        }
    }
    
    public long getId() {
        return id;
    }
    
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
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getMd5() {
        return md5;
    }
    
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    
    /** 将配置内容写入 {@link PrintWriter}，供导出或流式响应 */
    public void dump(PrintWriter writer) {
        writer.write(this.content);
    }
    
    public String getEncryptedDataKey() {
        return encryptedDataKey;
    }
    
    public void setEncryptedDataKey(String encryptedDataKey) {
        this.encryptedDataKey = encryptedDataKey;
    }
    
    /** 按 dataId、group、content 字典序比较，用于排序与去重 */
    @Override
    public int compareTo(ConfigInfoBase o) {
        if (o == null) {
            return 1;
        }
        if (this.dataId == null) {
            if (o.getDataId() == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (o.getDataId() == null) {
                return 1;
            } else {
                int cmpDataId = this.dataId.compareTo(o.getDataId());
                if (cmpDataId != 0) {
                    return cmpDataId;
                }
            }
        }
        
        if (this.group == null) {
            if (o.getGroup() == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (o.getGroup() == null) {
                return 1;
            } else {
                int cmpGroup = this.group.compareTo(o.getGroup());
                if (cmpGroup != 0) {
                    return cmpGroup;
                }
            }
        }
        
        if (this.content == null) {
            if (o.getContent() == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (o.getContent() == null) {
                return 1;
            } else {
                int cmpContent = this.content.compareTo(o.getContent());
                if (cmpContent != 0) {
                    return cmpContent;
                }
            }
        }
        return 0;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ConfigInfoBase other = (ConfigInfoBase) obj;
        if (content == null) {
            if (other.content != null) {
                return false;
            }
        } else if (!content.equals(other.content)) {
            return false;
        }
        if (dataId == null) {
            if (other.dataId != null) {
                return false;
            }
        } else if (!dataId.equals(other.dataId)) {
            return false;
        }
        if (group == null) {
            if (other.group != null) {
                return false;
            }
        } else if (!group.equals(other.group)) {
            return false;
        }
        if (md5 == null) {
            if (other.md5 != null) {
                return false;
            }
        } else if (!md5.equals(other.md5)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(dataId, group, content, md5);
    }
    
    @Override
    public String toString() {
        return "ConfigInfoBase{" + "id=" + id + ", dataId='" + dataId + '\'' + ", group='" + group
            + '\''
            + ", content='" + content + '\'' + ", md5='" + md5 + '\'' + '}';
    }
}
