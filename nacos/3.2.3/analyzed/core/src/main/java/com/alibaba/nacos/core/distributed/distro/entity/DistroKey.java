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

package com.alibaba.nacos.core.distributed.distro.entity;

import java.util.Objects;

/**
 * Distro 数据键：由资源 key、资源类型与可选目标服务器地址组成，用于同步路由与哈希分区。
 * Distro key.
 *
 * @author xiweng.yy
 */
public class DistroKey {
    
    /** 业务资源唯一标识。 */
    private String resourceKey;
    
    /** 资源类型（对应注册的 Distro 组件类型）。 */
    private String resourceType;
    
    /** 同步/拉取目标节点地址（可为空表示本地查询）。 */
    private String targetServer;
    
    /** 无参构造，供反序列化使用。 */
    public DistroKey() {
    }
    
    /** 构造不含目标节点的本地 Distro 键。 */
    public DistroKey(String resourceKey, String resourceType) {
        this.resourceKey = resourceKey;
        this.resourceType = resourceType;
    }
    
    /** 构造含目标节点的 Distro 键（用于跨节点同步）。 */
    public DistroKey(String resourceKey, String resourceType, String targetServer) {
        this.resourceKey = resourceKey;
        this.resourceType = resourceType;
        this.targetServer = targetServer;
    }
    
    /** 返回资源 key。 */
    public String getResourceKey() {
        return resourceKey;
    }
    
    /** 设置资源 key。 */
    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
    
    /** 返回资源类型。 */
    public String getResourceType() {
        return resourceType;
    }
    
    /** 设置资源类型。 */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    /** 返回目标服务器地址。 */
    public String getTargetServer() {
        return targetServer;
    }
    
    /** 设置目标服务器地址。 */
    public void setTargetServer(String targetServer) {
        this.targetServer = targetServer;
    }
    
    /** 基于 resourceKey、resourceType 与 targetServer 判等。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DistroKey distroKey = (DistroKey) o;
        return Objects.equals(resourceKey, distroKey.resourceKey) && Objects
            .equals(resourceType, distroKey.resourceType)
            && Objects.equals(targetServer, distroKey.targetServer);
    }
    
    /** 与 {@link #equals(Object)} 一致的哈希。 */
    @Override
    public int hashCode() {
        return Objects.hash(resourceKey, resourceType, targetServer);
    }
    
    /** 返回便于日志排查的字符串表示。 */
    @Override
    public String toString() {
        return "DistroKey{" + "resourceKey='" + resourceKey + '\'' + ", resourceType='"
            + resourceType + '\''
            + ", targetServer='" + targetServer + '\'' + '}';
    }
}
