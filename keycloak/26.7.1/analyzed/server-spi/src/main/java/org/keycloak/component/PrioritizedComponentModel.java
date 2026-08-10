/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.component;

import java.util.Comparator;

/**
 * 带优先级的 {@link ComponentModel}：通过配置项 {@link #PRIORITY} 排序组件实例。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PrioritizedComponentModel extends ComponentModel {
    /** 优先级配置键。 */
    public static final String PRIORITY = "priority";
    /** 按优先级升序比较 {@link ComponentModel} 的比较器。 */
    public static Comparator<ComponentModel> comparator = new Comparator<ComponentModel>() {
        @Override
        public int compare(ComponentModel o1, ComponentModel o2) {
            return parsePriority(o1) - parsePriority(o2);
        }
    };

    /** 从已有 {@link ComponentModel} 复制构造。 */
    public PrioritizedComponentModel(ComponentModel copy) {
        super(copy);
    }

    public PrioritizedComponentModel() {
    }

    /** 从组件配置解析优先级，缺省为 0。 */
    public static int parsePriority(ComponentModel component) {
        String priority = component.getConfig().getFirst(PRIORITY);
        if (priority == null) return 0;
        return Integer.parseInt(priority);

    }

    /** @return 当前组件的优先级 */
    public int getPriority() {
        return parsePriority(this);

    }

    /** 设置当前组件的优先级。 */
    public void setPriority(int priority) {
        getConfig().putSingle("priority", Integer.toString(priority));
    }
}
