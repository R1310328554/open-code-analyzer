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
package org.keycloak.dom.saml.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SAML Advice 类型，可携带断言引用、嵌套断言或其他扩展元素。
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class CommonAdviceType implements Serializable {

    protected List<Object> advices = new ArrayList<>();

    /**
     * 添加一条 Advice 元素。
     *
     * @param obj Advice 对象
     */
    public void addAdvice(Object obj) {
        advices.add(obj);
    }

    /**
     * 移除一条 Advice 元素。
     *
     * @param advice 待移除对象
     *
     * @return 是否成功移除
     */
    public boolean remove(Object advice) {
        return this.advices.remove(advice);
    }

    /**
     * 获取 Advice 列表（只读）。
     *
     * @return 只读 {@link List}
     */
    public List<Object> getAdvices() {
        return Collections.unmodifiableList(advices);
    }
}
