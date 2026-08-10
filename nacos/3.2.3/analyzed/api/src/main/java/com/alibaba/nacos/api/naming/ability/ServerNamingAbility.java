/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.naming.ability;

import java.io.Serializable;
import java.util.Objects;

/**
 * Nacos 命名服务端能力描述。
 *
 * <p>描述集群是否启用 JRaft 持久化等特性，供客户端或运维工具探测。</p>
 *
 * @author liuzunfei
 * @version $Id: ServerNamingAbility.java, v 0.1 2021年01月24日 00:09 AM liuzunfei Exp $
 */
public class ServerNamingAbility implements Serializable {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = 8308895444341445512L;
    
    /** 服务端是否使用 SOFA-JRaft 持久化服务与元数据。 */
    private boolean supportJraft;
    
    /** 是否支持 JRaft 持久化。 */
    public boolean isSupportJraft() {
        return supportJraft;
    }
    
    /** 设置 JRaft 支持标志。 */
    public void setSupportJraft(boolean supportJraft) {
        this.supportJraft = supportJraft;
    }
    
    /** 按 {@link #supportJraft} 比较能力是否相同。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerNamingAbility)) {
            return false;
        }
        ServerNamingAbility that = (ServerNamingAbility) o;
        return supportJraft == that.supportJraft;
    }
    
    /** 基于 JRaft 支持标志的哈希码。 */
    @Override
    public int hashCode() {
        return Objects.hash(supportJraft);
    }
}
