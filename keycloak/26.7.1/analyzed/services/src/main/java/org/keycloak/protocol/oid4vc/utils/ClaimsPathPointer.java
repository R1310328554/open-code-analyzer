/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oid4vc.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.keycloak.protocol.oid4vc.model.ClaimsDescription;
import org.keycloak.utils.StringUtil;

import org.jboss.logging.Logger;

/**
 * claims path pointer 工具类。
 * <p>claims path pointer 指向可验证凭证 JSON 中的一条或多条声明，用于授权详情过滤与校验。</p>
 *
 * @author <a href="mailto:Forkim.Akwichek@adorsys.com">Forkim Akwichek</a>
 */
public class ClaimsPathPointer {

    private static final Logger logger = Logger.getLogger(ClaimsPathPointer.class);

    /**
     * 校验 claims path pointer 格式是否合法。
     * <p>路径分量仅允许非空字符串、非负整数或 null（数组全选）。</p>
     *
     * @param path claims path pointer
     * @return 合法返回 true
     */
    public static boolean isValidPath(List<Object> path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        for (Object component : path) {
            if (component == null) {
                // null 表示选择当前数组的全部元素
                continue;
            }

            if (component instanceof String) {
                // 字符串表示对象键，且不可为空白
                if (StringUtil.isBlank((String) component)) {
                    return false;
                }
                continue;
            }

            if (component instanceof Integer) {
                Integer index = (Integer) component;
                if (index < 0) {
                    // 不允许负整数索引
                    return false;
                }
                // 非负整数表示数组下标
                continue;
            }

            // 其他类型均非法
            return false;
        }

        return true;
    }

    /**
     * 校验 claims 描述列表是否存在冲突或矛盾。
     *
     * @param claims claims 描述列表
     * @return 无冲突返回 true
     */
    public static boolean validateClaimsDescriptions(List<ClaimsDescription> claims) {
        if (claims == null || claims.isEmpty()) {
            return true;
        }

        // 两两比较是否存在重复或矛盾描述
        for (int i = 0; i < claims.size(); i++) {
            for (int j = i + 1; j < claims.size(); j++) {
                ClaimsDescription claim1 = claims.get(i);
                ClaimsDescription claim2 = claims.get(j);

                if (isConflicting(claim1, claim2)) {
                    logger.warnf("Conflicting claims descriptions found: %s and %s", claim1.getPath(), claim2.getPath());
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 判断两条 claims 描述是否冲突。
     *
     * @param claim1 第一条描述
     * @param claim2 第二条描述
     * @return 冲突返回 true
     */
    private static boolean isConflicting(ClaimsDescription claim1, ClaimsDescription claim2) {
        List<Object> path1 = claim1.getPath();
        List<Object> path2 = claim2.getPath();

        if (path1 == null || path2 == null) {
            return false;
        }

        // 路径完全相同视为冲突
        if (path1.equals(path2)) {
            return true;
        }

        // 检查数组与对象寻址方式的冲突
        return hasArrayObjectConflict(path1, path2);
    }

    /**
     * 检测两条路径在数组/对象寻址上是否冲突。
     *
     * @param path1 第一条路径
     * @param path2 第二条路径
     * @return 存在冲突返回 true
     */
    private static boolean hasArrayObjectConflict(List<Object> path1, List<Object> path2) {
        int minLength = Math.min(path1.size(), path2.size());

        for (int i = 0; i < minLength; i++) {
            Object comp1 = path1.get(i);
            Object comp2 = path2.get(i);

            // null（全选数组）与 string（对象键）混用视为冲突
            if (comp1 == null && comp2 instanceof String) {
                return true;
            }
            if (comp2 == null && comp1 instanceof String) {
                return true;
            }

            // 具体下标与 null（全选）混用视为冲突
            if (comp1 == null && comp2 instanceof Integer) {
                return true;
            }
            if (comp2 == null && comp1 instanceof Integer) {
                return true;
            }

            // 相同分量也视为冲突（审阅意见）
            if (Objects.equals(comp1, comp2)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 按授权详情中的 claims 描述过滤完整 claims 映射。
     * <p>仅保留路径匹配的声明；必填项缺失时抛出异常。</p>
     *
     * @param allClaims       完整 claims
     * @param requestedClaims 授权详情请求的 claims 描述
     * @return 过滤后的 claims
     * @throws IllegalArgumentException 必填 claim 缺失
     */
    public static Map<String, Object> filterClaimsByAuthorizationDetails(
            Map<String, Object> allClaims,
            List<ClaimsDescription> requestedClaims) {

        if (requestedClaims == null || requestedClaims.isEmpty()) {
            return allClaims; // 未请求过滤则返回全部
        }

        Map<String, Object> filteredClaims = new HashMap<>();

        for (ClaimsDescription claim : requestedClaims) {
            List<Object> path = claim.getPath();
            if (path == null || path.isEmpty()) {
                continue; // 跳过无效路径
            }

            // 按 OID4VCI 规范校验路径格式
            if (!isValidPath(path)) {
                logger.warnf("Invalid claims path pointer: %s. Path must contain only strings, non-negative integers, and null values.", path);
                continue; // Skip invalid paths
            }

            try {
                // 解析路径得到 claim 值
                List<Object> claimValues = processClaimsPathPointer(allClaims, path);

                if (!claimValues.isEmpty()) {
                    // 将选中值写入结果
                    if (claimValues.size() == 1) {
                        // 单值直接写入
                        addClaimByPath(filteredClaims, path, claimValues.get(0));
                    } else {
                        // 多值（数组选择）使用辅助方法
                        addMultipleClaimsByPath(filteredClaims, path, claimValues);
                    }
                } else if (Boolean.TRUE.equals(claim.getMandatory())) {
                    // 必填 claim 缺失则失败
                    throw new IllegalArgumentException("Mandatory claim not found: " + path);
                }
                // 可选 claim 不存在则忽略
            } catch (IllegalArgumentException e) {
                if (Boolean.TRUE.equals(claim.getMandatory())) {
                    // 必填项处理失败前记录警告
                    logger.warnf("Failed to process mandatory claim path %s: %s", path, e.getMessage());
                    // 必填项重新抛出
                    throw e;
                }
                // 可选项记录 debug 并继续
                logger.debugf("Failed to process optional claim path %s: %s", path, e.getMessage());
            }
        }

        return filteredClaims;
    }


    /**
     * 按 OID4VCI 规范从左到右处理 claims path pointer。
     *
     * @param claims 根 claims 映射
     * @param path   claims path pointer
     * @return 选中的 JSON 元素列表
     * @throws IllegalArgumentException 处理违反规范时
     */
    public static List<Object> processClaimsPathPointer(Map<String, Object> claims, List<Object> path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Claims path pointer must be a non-empty array");
        }
        if (claims == null) {
            throw new IllegalArgumentException("Claims map cannot be null");
        }

        // 从根元素开始
        List<Object> currentSelection = new ArrayList<>();
        currentSelection.add(claims);

        // 逐分量处理路径
        for (Object component : path) {
            if (currentSelection.isEmpty()) {
                throw new IllegalArgumentException("No elements currently selected, cannot process further");
            }

            List<Object> nextSelection = new ArrayList<>();

            for (Object current : currentSelection) {
                if (component instanceof String) {
                    // 字符串：按键选取对象元素
                    if (current instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) current;
                        Object value = map.get(component);
                        if (value != null) {
                            nextSelection.add(value);
                        }
                    }
                } else if (component instanceof Integer) {
                    // 整数：按下标选取数组元素
                    int index = (Integer) component;
                    if (index < 0) {
                        throw new IllegalArgumentException("Negative integer values are not allowed in claims path pointer");
                    }
                    if (current instanceof List) {
                        List<?> list = (List<?>) current;
                        if (index < list.size()) {
                            nextSelection.add(list.get(index));
                        }
                    }
                } else if (component == null) {
                    // null：选取当前数组的全部元素
                    if (current instanceof List) {
                        List<?> list = (List<?>) current;
                        nextSelection.addAll(list);
                    }
                } else {
                    throw new IllegalArgumentException("Invalid path component type: " + component.getClass().getSimpleName() +
                            ". Only String, Integer, and null are allowed.");
                }
            }

            currentSelection = nextSelection;
        }

        if (currentSelection.isEmpty()) {
            throw new IllegalArgumentException("No elements selected after processing claims path pointer");
        }

        return currentSelection;
    }

    /**
     * 数组选择场景下将多个 claim 值写入结果映射。
     *
     * @param claims 目标 claims
     * @param path   claims path pointer
     * @param values 待写入的值列表
     */
    private static void addMultipleClaimsByPath(Map<String, Object> claims, List<Object> path, List<Object> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        // 单段路径直接写入首值
        if (path.size() == 1 && path.get(0) instanceof String) {
            claims.put((String) path.get(0), values.get(0));
            return;
        }

        // 复杂路径需构建嵌套结构
        // This creates the appropriate nested structure to hold the selected values
        if (values.size() == 1) {
            // 单值复用 addClaimByPath
            addClaimByPath(claims, path, values.get(0));
        } else {
            // 多值表示数组全选
            // 创建数组结构容纳全部值
            createArrayStructureForMultipleValues(claims, path, values);
        }
    }

    /**
     * 为数组选择结果创建数组嵌套结构。
     *
     * @param claims 目标 claims
     * @param path   claims path pointer
     * @param values 值列表
     */
    private static void createArrayStructureForMultipleValues(Map<String, Object> claims, List<Object> path, List<Object> values) {
        buildNestedStructure(claims, path, new ArrayList<Object>(values), true);
    }

    /**
     * 按路径将单个 claim 值写入映射。
     *
     * @param claims 目标 claims
     * @param path   claims path pointer
     * @param value  要写入的值
     */
    private static void addClaimByPath(Map<String, Object> claims, List<Object> path, Object value) {
        if (path == null || path.isEmpty() || claims == null) {
            return;
        }

        if (path.size() == 1 && path.get(0) instanceof String) {
            // 单键直接赋值
            claims.put((String) path.get(0), value);
            return;
        }

        // 嵌套路径构建中间结构
        buildNestedClaimStructure(claims, path, value);
    }

    /**
     * 为复杂路径构建嵌套 claim 结构。
     *
     * @param claims 目标 claims
     * @param path   claims path pointer
     * @param value  叶子值
     */
    private static void buildNestedClaimStructure(Map<String, Object> claims, List<Object> path, Object value) {
        buildNestedStructure(claims, path, value, false);
    }

    /**
     * 通用嵌套结构构建（支持单值与多值数组选择）。
     *
     * @param claims           目标 claims
     * @param path             claims path pointer
     * @param value            单值或值列表
     * @param isArraySelection 是否为数组全选场景
     */
    private static void buildNestedStructure(Map<String, Object> claims, List<Object> path, Object value, boolean isArraySelection) {
        if (path.size() < 2) {
            return;
        }

        Object current = claims;
        String rootKey = (String) path.get(0);

        // 确保根键存在
        if (!(current instanceof Map)) {
            return;
        }

        Map<String, Object> rootMap = (Map<String, Object>) current;
        if (!rootMap.containsKey(rootKey)) {
            // 数组选择用 ArrayList，单值用 HashMap
            rootMap.put(rootKey, isArraySelection ? new ArrayList<Object>() : new HashMap<String, Object>());
        }

        current = rootMap.get(rootKey);

        // 沿路径导航并按需创建中间节点
        for (int i = 1; i < path.size() - 1; i++) {
            Object component = path.get(i);

            if (component instanceof String) {
                if (!(current instanceof Map)) {
                    return; // 无法继续导航
                }
                Map<String, Object> map = (Map<String, Object>) current;
                if (!map.containsKey(component)) {
                    map.put((String) component, new HashMap<String, Object>());
                }
                current = map.get(component);
            } else if (component instanceof Integer) {
                if (!(current instanceof List)) {
                    return; // Can't navigate further
                }
                List<Object> list = (List<Object>) current;
                int index = (Integer) component;
                while (list.size() <= index) {
                    // 单值占位 HashMap，数组选择占位 null
                    list.add(isArraySelection ? null : new HashMap<String, Object>());
                }
                current = list.get(index);
            }
        }

        // 写入最终叶子值
        Object finalComponent = path.get(path.size() - 1);
        if (finalComponent instanceof String) {
            if (current instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) current;
                map.put((String) finalComponent, value);
            }
        } else if (finalComponent instanceof Integer) {
            if (current instanceof List) {
                List<Object> list = (List<Object>) current;
                int index = (Integer) finalComponent;
                while (list.size() <= index) {
                    list.add(null);
                }
                list.set(index, value);
            }
        }
    }
}
