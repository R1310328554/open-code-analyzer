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

package com.alibaba.nacos.naming.consistency.ephemeral.distro.v2;

import java.io.Serializable;

/**
 * Distro 客户端校验信息。
 *
 * <p>携带 clientId 与 revision，供节点间比对本地客户端是否一致。</p>
 *
 * @author xiweng.yy
 */
public class DistroClientVerifyInfo implements Serializable {
    
    private static final long serialVersionUID = 2223964944788737629L;
    
    /** 客户端唯一标识。 */
    private String clientId;
    
    /** 客户端数据修订号。 */
    private long revision;
    
    /** 无参构造，供反序列化使用。 */
    public DistroClientVerifyInfo() {
    }
    
    /**
     * 构造校验信息。
     *
     * @param clientId 客户端 ID
     * @param revision 修订号
     */
        this.clientId = clientId;
        this.revision = revision;
    }
    
    /** 获取客户端 ID。 */
    public String getClientId() {
        return clientId;
    }
    
    /** 设置客户端 ID。 */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    /** 获取修订号。 */
    public long getRevision() {
        return revision;
    }
    
    /** 设置修订号。 */
    public void setRevision(long revision) {
        this.revision = revision;
    }
}
