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
package com.alibaba.csp.sentinel.slotchain;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * 资源名称与类型的包装器，作为 Slot 链处理的基本输入。
 *
 * @author qinan.qn
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public abstract class ResourceWrapper {

    protected final String name;

    protected final EntryType entryType;
    protected final int resourceType;

    public ResourceWrapper(String name, EntryType entryType, int resourceType) {
        AssertUtil.notEmpty(name, "resource name cannot be empty");
        AssertUtil.notNull(entryType, "entryType cannot be null");
        this.name = name;
        this.entryType = entryType;
        this.resourceType = resourceType;
    }

    /**
     * 获取资源名称。
     *
     * @return 资源名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取本包装器的 {@link EntryType}。
     *
     * @return 入口类型
     */
    public EntryType getEntryType() {
        return entryType;
    }

    /**
     * 获取资源分类。
     *
     * @return 资源分类标识
     * @since 1.7.0
     */
    public int getResourceType() {
        return resourceType;
    }

    /**
     * 获取用于展示的友好资源名称。
     *
     * @return 展示用资源名称
     */
    public abstract String getShowName();

    /**
     * 仅依据 {@link #getName()} 计算哈希值。
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * 仅依据 {@link #getName()} 判断相等性。
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ResourceWrapper) {
            ResourceWrapper rw = (ResourceWrapper)obj;
            return rw.getName().equals(getName());
        }
        return false;
    }
}
