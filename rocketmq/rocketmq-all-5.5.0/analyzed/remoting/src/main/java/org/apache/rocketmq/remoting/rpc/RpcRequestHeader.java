/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.remoting.rpc;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.apache.rocketmq.remoting.CommandCustomHeader;

/**
 * RPC 请求头基类：命名空间、目标 Broker 名与 oneway 标志。
 */
public abstract class RpcRequestHeader implements CommandCustomHeader {
    /** 命名空间名称。 */
    protected String ns;
    /** 数据是否已按命名空间隔离。 */
    protected Boolean nsd;
    /** 目标 Broker 抽象名（通常为物理 Broker 组名）。 */
    protected String bname;
    /** 是否为 oneway 请求（无需响应）。 */
    protected Boolean oway;

    /** 已废弃：请使用 {@link #getBrokerName()}。 */
    @Deprecated
    public String getBname() {
        return bname;
    }

    /** 已废弃：请使用 {@link #setBrokerName(String)}。 */
    @Deprecated
    public void setBname(String brokerName) {
        this.bname = brokerName;
    }

    /** 返回目标 Broker 名。 */
    public String getBrokerName() {
        return bname;
    }

    /** 设置目标 Broker 名。 */
    public void setBrokerName(String brokerName) {
        this.bname = brokerName;
    }

    /** 返回命名空间。 */
    public String getNamespace() {
        return ns;
    }

    /** 设置命名空间。 */
    public void setNamespace(String namespace) {
        this.ns = namespace;
    }

    /** 是否已命名空间化。 */
    public Boolean getNamespaced() {
        return nsd;
    }

    /** 设置命名空间化标志。 */
    public void setNamespaced(Boolean namespaced) {
        this.nsd = namespaced;
    }

    /** 是否 oneway 请求。 */
    public Boolean getOneway() {
        return oway;
    }

    /** 设置 oneway 标志。 */
    public void setOneway(Boolean oneway) {
        this.oway = oneway;
    }

    /** 比较请求头字段相等性。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RpcRequestHeader header = (RpcRequestHeader) o;
        return Objects.equals(ns, header.ns) && Objects.equals(nsd, header.nsd) && Objects.equals(bname, header.bname) && Objects.equals(oway, header.oway);
    }

    /** 计算请求头哈希。 */
    @Override
    public int hashCode() {
        return Objects.hash(ns, nsd, bname, oway);
    }

    /** 返回调试字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("namespace", ns)
            .add("namespaced", nsd)
            .add("brokerName", bname)
            .add("oneway", oway)
            .toString();
    }
}
