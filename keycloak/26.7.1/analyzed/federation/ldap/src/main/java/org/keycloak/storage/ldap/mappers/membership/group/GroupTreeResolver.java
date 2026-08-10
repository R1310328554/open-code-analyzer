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

package org.keycloak.storage.ldap.mappers.membership.group;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jboss.logging.Logger;

/**
 * LDAP 组树解析器：将 LDAP 组父子关系解析为 Keycloak 可用的单父组树，并校验循环与多父约束。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class GroupTreeResolver {

    private static final Logger logger = Logger.getLogger(GroupTreeResolver.class);


    /**
     * 将 LDAP 组信息（每组仅含名称与直接子组）完整解析为 Keycloak 可用的组树列表。
     * <p>
     * 同时执行校验：LDAP 允许递归与多父，Keycloak 不允许。
     *
     * @param groups 组列表
     * @param ignoreMissingGroups 是否忽略缺失的子组引用
     * @return 解析后的根组树条目列表
     * @throws GroupTreeResolveException 存在多父、循环或缺失组时抛出
     */
    public List<GroupTreeEntry> resolveGroupTree(List<Group> groups, boolean ignoreMissingGroups) throws GroupTreeResolveException {
        // 1 - 计算每个组的父组列表
        Map<String, List<String>> parentsTree = getParentsTree(groups, ignoreMissingGroups);

        // 2 - 找出根组（无父组），并检测多父组
        List<String> rootGroups = new LinkedList<>();
        for (Map.Entry<String, List<String>> group : parentsTree.entrySet()) {
            int parentCount = group.getValue().size();
            if (parentCount == 0) {
                rootGroups.add(group.getKey());
            } else if (parentCount > 1) {
                throw new GroupTreeResolveException("Group '" + group.getKey() + "' detected to have multiple parents. This is not allowed in Keycloak. Parents are: " + group.getValue());
            }
        }

        // 3 - 转为 Map 便于按名称查找
        Map<String, Group> asMap = new TreeMap<>();
        for (Group group : groups) {
            asMap.put(group.getGroupName(), group);
        }

        // 4 - 从各根组递归解析子树
        List<GroupTreeEntry> finalResult = new LinkedList<>();
        Set<String> visitedGroups = new TreeSet<>();
        for (String rootGroupName : rootGroups) {
            List<String> subtree = new LinkedList<>();
            subtree.add(rootGroupName);
            GroupTreeEntry groupTree = resolveGroupTree(rootGroupName, asMap, visitedGroups, subtree);
            finalResult.add(groupTree);
        }


        // 5 - 检测未访问到的组（说明存在循环）
        if (visitedGroups.size() != asMap.size()) {
            // 检测到循环，尝试定位循环路径
            for (Map.Entry<String, Group> entry : asMap.entrySet()) {
                String groupName = entry.getKey();
                if (!visitedGroups.contains(groupName)) {
                    List<String> subtree = new LinkedList<>();
                    subtree.add(groupName);

                    Set<String> newVisitedGroups = new TreeSet<>();
                    resolveGroupTree(groupName, asMap, newVisitedGroups, subtree);
                    visitedGroups.addAll(newVisitedGroups);
                }
            }

            // 不应到达此处
            throw new GroupTreeResolveException("Illegal state: Recursion detected, but wasn't able to find it");
        }

        return finalResult;
    }

    /** 根据子组引用反向构建父组映射。 */
    private Map<String, List<String>> getParentsTree(List<Group> groups, boolean ignoreMissingGroups) throws GroupTreeResolveException {
        Map<String, List<String>> result = new TreeMap<>();

        for (Group group : groups) {
            result.put(group.getGroupName(), new LinkedList<String>());
        }

        for (Group group : groups) {
            Iterator<String> iterator = group.getChildrenNames().iterator();
            while (iterator.hasNext()) {
                String child = iterator.next();
                List<String> list = result.get(child);
                if (list != null) {
                    list.add(group.getGroupName());
                } else if (ignoreMissingGroups) {
                    // 移除不存在的子组引用
                    iterator.remove();
                    logger.debug("Group '" + child + "' referenced as member of group '" + group.getGroupName() + "' doesn't exist. Ignoring.");
                } else {
                    throw new GroupTreeResolveException("Group '" + child + "' referenced as member of group '" + group.getGroupName() + "' doesn't exist");
                }
            }
        }
        return result;
    }

    /** 递归解析以 groupName 为根的子树。 */
    private GroupTreeEntry resolveGroupTree(String groupName, Map<String, Group> asMap, Set<String> visitedGroups, List<String> currentSubtree) throws GroupTreeResolveException {
        if (visitedGroups.contains(groupName)) {
            throw new GroupTreeResolveException("Recursion detected when trying to resolve group '" + groupName + "'. Whole recursion path: " + currentSubtree);
        }

        visitedGroups.add(groupName);

        Group group = asMap.get(groupName);

        List<GroupTreeEntry> children = new LinkedList<>();
        GroupTreeEntry result =  new GroupTreeEntry(group.getGroupName(), children);

        for (String childrenName : group.getChildrenNames()) {
            List<String> subtreeCopy = new LinkedList<>(currentSubtree);
            subtreeCopy.add(childrenName);
            GroupTreeEntry childEntry = resolveGroupTree(childrenName, asMap, visitedGroups, subtreeCopy);
            children.add(childEntry);
        }

        return result;
    }



    // 静态内部类

    /** 组树解析失败时抛出的异常。 */
    public static class GroupTreeResolveException extends Exception {

        public GroupTreeResolveException(String message) {
            super(message);
        }
    }

    /** 输入组节点：组名及其直接子组名列表。 */
    public static class Group {

        private final String groupName;
        private final List<String> childrenNames;

        public Group(String groupName, String... childrenNames) {
            this(groupName, Arrays.asList(childrenNames));
        }

        public Group(String groupName, Collection<String> childrenNames) {
            this.groupName = groupName;
            this.childrenNames = new LinkedList<>(childrenNames);
        }

        public String getGroupName() {
            return groupName;
        }

        public List<String> getChildrenNames() {
            return childrenNames;
        }
    }

    /** 解析后的组树节点，含组名与子节点列表。 */
    public static class GroupTreeEntry {

        private final String groupName;
        private final List<GroupTreeEntry> children;

        public GroupTreeEntry(String groupName, List<GroupTreeEntry> children) {
            this.groupName = groupName;
            this.children = children;
        }

        public String getGroupName() {
            return groupName;
        }

        public List<GroupTreeEntry> getChildren() {
            return children;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("{ " + groupName + " -> [ ");
            for (GroupTreeEntry child : children) {
                builder.append(child.toString());
            }
            builder.append(" ]}");

            return builder.toString();
        }
    }
}
