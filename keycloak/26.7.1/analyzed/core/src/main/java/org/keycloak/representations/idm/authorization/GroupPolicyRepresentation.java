/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.representations.idm.authorization;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 组（group）类型授权策略的 REST 表示，按用户所属组或令牌中的组声明匹配请求。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class GroupPolicyRepresentation extends AbstractPolicyRepresentation {

    /** 令牌中读取组列表的声明名称（claim）。 */
    private String groupsClaim;
    /** 匹配的组定义集合。 */
    private Set<GroupDefinition> groups;

    /** @return 固定策略类型 {@code group} */
    @Override
    public String getType() {
        return "group";
    }

    /** @return 组声明名称 */
    public String getGroupsClaim() {
        return groupsClaim;
    }

    /** @param groupsClaim 组声明名称 */
    public void setGroupsClaim(String groupsClaim) {
        this.groupsClaim = groupsClaim;
    }

    /** @return 组定义集合 */
    public Set<GroupDefinition> getGroups() {
        return groups;
    }

    /** @param groups 组定义集合 */
    public void setGroups(Set<GroupDefinition> groups) {
        this.groups = groups;
    }

    /** 按组 ID 添加组（默认不扩展子组）。 */
    public void addGroup(String... ids) {
        for (String id : ids) {
            addGroup(id, false);
        }
    }

    /** 按组 ID 添加组，可指定是否扩展子组。 */
    public void addGroup(String id, boolean extendChildren) {
        if (groups == null) {
            groups = new HashSet<>();
        }
        groups.add(new GroupDefinition(id, extendChildren));
    }

    /** 按组路径添加组（默认不扩展子组）。 */
    public void addGroupPath(String... paths) {
        for (String path : paths) {
            addGroupPath(path, false);
        }
    }

    /** 按组路径添加组，可指定是否扩展子组。 */
    public void addGroupPath(String path, boolean extendChildren) {
        if (groups == null) {
            groups = new HashSet<>();
        }
        groups.add(new GroupDefinition(null, path, extendChildren));
    }

    /** 按 ID 或路径移除组。 */
    public void removeGroup(String... ids) {
        if (groups != null) {
            for (String id : ids) {
                Iterator<GroupDefinition> iterator = groups.iterator();
                while (iterator.hasNext()) {
                    GroupDefinition group = iterator.next();
                    if (id.equals(group.getId()) || (group.getPath() != null && group.getPath().equals(id))) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    /** 组匹配定义，支持按 ID 或路径标识，并可扩展子组。 */
    public static class GroupDefinition implements Comparable<GroupDefinition> {

        /** 组 ID。 */
        private String id;
        /** 组路径。 */
        private String path;
        /** 是否扩展匹配子组。 */
        private boolean extendChildren;

        public GroupDefinition() {
            this(null);
        }

        public GroupDefinition(String id) {
            this(id, false);
        }

        public GroupDefinition(String id, boolean extendChildren) {
            this(id, null, extendChildren);
        }

        public GroupDefinition(String id, String path, boolean extendChildren) {
            this.id = id;
            this.path = path;
            this.extendChildren = extendChildren;
        }

        /** @return 组 ID */
        public String getId() {
            return id;
        }

        /** @param id 组 ID */
        public void setId(String id) {
            this.id = id;
        }

        /** @return 组路径 */
        public String getPath() {
            return path;
        }

        /** @param path 组路径 */
        public void setPath(String path) {
            this.path = path;
        }

        /** @return 是否扩展子组 */
        public boolean isExtendChildren() {
            return extendChildren;
        }

        /** @param extendChildren 是否扩展子组 */
        public void setExtendChildren(boolean extendChildren) {
            this.extendChildren = extendChildren;
        }

        @Override
        public int compareTo(GroupDefinition o) {
            if (o.id == null || id == null) {
                return 1;
            }
            return id.compareTo(o.id);
        }
    }
}
