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

package com.alibaba.nacos.naming.core.v2.pojo;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.utils.NamingUtils;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Nacos V2 服务标识 POJO，由 namespace、group、name 与 ephemeral 唯一确定。
 *
 * <p>维护 revision 与最后更新时间，用于 Distro 同步与变更推送。</p>
 *
 * @author xiweng.yy
 */
public class Service implements Serializable {
    
    private static final long serialVersionUID = -990509089519499344L;
    
    /** 命名空间 ID。 */
    private final String namespace;
    
    /** 服务分组名。 */
    private final String group;
    
    /** 服务名。 */
    private final String name;
    
    /** 是否为临时服务。 */
    private final boolean ephemeral;
    
    /** 服务版本号，实例变更时递增。 */
    private final AtomicLong revision;
    
    /** 最近一次更新时间戳。 */
    private long lastUpdatedTime;
    
    private Service(String namespace, String group, String name, boolean ephemeral) {
        this.namespace = namespace;
        this.group = group;
        this.name = name;
        this.ephemeral = ephemeral;
        revision = new AtomicLong();
        lastUpdatedTime = System.currentTimeMillis();
    }
    
    /** 创建默认临时服务（ephemeral=true）。 */
    public static Service newService(String namespace, String group, String name) {
        return newService(namespace, group, name, true);
    }
    
    public static Service newService(String namespace, String group, String name,
        boolean ephemeral) {
        return new Service(namespace, group, name, ephemeral);
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public String getGroup() {
        return group;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isEphemeral() {
        return ephemeral;
    }
    
    public long getRevision() {
        return revision.get();
    }
    
    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }
    
    /** 刷新最后更新时间为当前时刻。 */
    public void renewUpdateTime() {
        lastUpdatedTime = System.currentTimeMillis();
    }
    
    /** 递增服务 revision。 */
    public void incrementRevision() {
        revision.incrementAndGet();
    }
    
    /** 返回 group@@serviceName 格式名称。 */
    public String getGroupedServiceName() {
        return NamingUtils.getGroupedName(name, group);
    }
    
    public String getNameSpaceGroupedServiceName() {
        // 不使用 String.intern，避免永久代/元空间泄漏
        //do not String.intern
        return namespace + Constants.SERVICE_INFO_SPLITER + NamingUtils.getGroupedName(name, group);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Service)) {
            return false;
        }
        Service service = (Service) o;
        return namespace.equals(service.namespace) && group.equals(service.group)
            && name.equals(service.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(namespace, group, name);
    }
    
    @Override
    public String toString() {
        return "Service{" + "namespace='" + namespace + '\'' + ", group='" + group + '\''
            + ", name='" + name + '\''
            + ", ephemeral=" + ephemeral + ", revision=" + revision + '}';
    }
}
