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
package org.keycloak.client.admin.cli.operations;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 本地 JSON 对象列表的精确匹配搜索工具。
 * <p>
 * 在已获取的角色/客户端等列表中按属性值查找唯一匹配项，供 add/remove-roles 等命令使用。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class LocalSearch {

    /** 待搜索的 JSON 对象列表。 */
    private List<ObjectNode> items;

    /** 使用给定对象列表构造搜索器。 */
    public LocalSearch(List<ObjectNode> items) {
        this.items = items;
    }

    /**
     * 在指定属性上精确匹配唯一对象。
     *
     * @param value 待匹配的属性值
     * @param attrs 依次尝试的属性名列表
     * @return 唯一匹配项；无匹配返回 null；多条匹配抛出异常
     */
    public ObjectNode exactMatchOne(String value, String ... attrs) {

        List<ObjectNode> matched = new LinkedList<>();

        for (ObjectNode item: items) {
            for (String attr: attrs) {
                JsonNode node = item.get(attr);
                if (node != null && node.asText().equals(value)) {
                    matched.add(item);
                    break;
                }
            }
        }

        if (matched.size() == 0) {
            return null;
        }

        if (matched.size() > 1) {
            throw new RuntimeException("More than one match");
        }

        return matched.get(0);
    }
}
