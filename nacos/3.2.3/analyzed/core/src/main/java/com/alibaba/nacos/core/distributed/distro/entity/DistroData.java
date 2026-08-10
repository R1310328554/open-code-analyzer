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

import com.alibaba.nacos.consistency.DataOperation;

/**
 * Distro 数据载体：包含资源键、操作类型与序列化后的内容字节。
 * Distro data.
 *
 * @author xiweng.yy
 */
public class DistroData {
    
    /** 数据对应的 Distro 键（资源 key、类型、目标节点等）。 */
    private DistroKey distroKey;
    
    /** 数据操作类型（增删改等）。 */
    private DataOperation type;
    
    /** 序列化后的业务载荷。 */
    private byte[] content;
    
    /** 无参构造，供反序列化使用。 */
    public DistroData() {
    }
    
    /** 构造指定 key 与内容的 Distro 数据。 */
    public DistroData(DistroKey distroKey, byte[] content) {
        this.distroKey = distroKey;
        this.content = content;
    }
    
    /** 返回 Distro 键。 */
    public DistroKey getDistroKey() {
        return distroKey;
    }
    
    /** 设置 Distro 键。 */
    public void setDistroKey(DistroKey distroKey) {
        this.distroKey = distroKey;
    }
    
    /** 返回操作类型。 */
    public DataOperation getType() {
        return type;
    }
    
    /** 设置操作类型。 */
    public void setType(DataOperation type) {
        this.type = type;
    }
    
    /** 返回内容字节数组。 */
    public byte[] getContent() {
        return content;
    }
    
    /** 设置内容字节数组。 */
    public void setContent(byte[] content) {
        this.content = content;
    }
}
