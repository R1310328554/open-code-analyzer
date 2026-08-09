/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.liveobject.misc;

import jodd.bean.BeanUtil;
import jodd.bean.BeanUtilBean;
import jodd.bean.BeanVisitor;

import java.util.List;

/**
 * 基于 Jodd BeanUtil 的属性拷贝工具。
 * <p>
 * 遍历 source 的非 null 属性写入 destination，可排除指定字段名。
 *
 * @author Nikita Koksharov
 *
 */
public final class AdvBeanCopy {

    /** 属性来源对象。 */
    private final Object source;
    /** 属性目标对象。 */
    private final Object destination;

    /** @param source 源 Bean @param destination 目标 Bean */
    public AdvBeanCopy(Object source, Object destination) {
        this.source = source;
        this.destination = destination;
    }
    
    /**
     * 将 source 的可读属性复制到 destination。
     * @param excludedFields 跳过不拷贝的属性名列表
     */
    public void copy(List<String> excludedFields) {
        BeanUtil beanUtil = new BeanUtilBean();

        new BeanVisitor(source)
                .ignoreNulls(true)
                .visit((name, value) -> {
                    if (excludedFields.contains(name)) {
                        return;
                    }

                    beanUtil.setProperty(destination, name, value);
                });
    }

}
