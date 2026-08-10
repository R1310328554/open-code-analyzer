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
package org.keycloak.dom.saml.v2.ac.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A type that contains a list of objects
 * 对象列表容器：维护通用 Object 的可变列表并提供增删与只读访问。
 *
 * @author Anil.Saldhana@redhat.com
 * @since Feb 3, 2011
 */
public class ObjectListType {

    protected List<Object> content = new ArrayList<>();

    /** 添加对象元素。 */
    public void add(Object obj) {
        this.content.add(obj);
    }

    /** 移除对象元素。 */
    public void remove(Object obj) {
        this.content.remove(obj);
    }

    /** 获取不可修改的对象列表。 */
    public List<Object> getContent() {

        return Collections.unmodifiableList(this.content);
    }
}
