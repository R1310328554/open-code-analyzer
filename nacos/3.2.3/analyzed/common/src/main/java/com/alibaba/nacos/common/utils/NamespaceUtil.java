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

package com.alibaba.nacos.common.utils;

import com.alibaba.nacos.api.common.Constants;

/**
 * 命名空间（租户）工具：Config 模块将空字符串租户归一化为默认 public namespace；
 * Naming 与 Config 对 tenant 语义不同，本类仅限 Config 侧使用。
 * namespace(tenant) util. Because config and naming treat namespace(tenant) differently, this tool class can only be
 * used by the config module.
 *
 * @author klw(213539 @ qq.com)
 * @date 2020/10/12 17:56
 */
public class NamespaceUtil {
    
    private NamespaceUtil() {
    }
    
    /**
     * public id，默认值为 "public".
     */
    public static String namespaceDefaultId = Constants.DEFAULT_NAMESPACE_ID;
    
    /**
     * Treat the namespace(tenant) parameters with values of empty string as "public".
     *
     * @param tenant namespace(tenant) id
     * @return java.lang.String A namespace(tenant) string processed
      * <p>Config 命名空间归一化；详见类级说明。</p>
     */
    public static String processNamespaceParameter(String tenant) {
        if (isNeedTransferNamespace(tenant)) {
            return getNamespaceDefaultId();
        }
        return tenant.trim();
    }
    
    /** 判断租户 ID 是否应转换为默认 public namespace（空或空白时为 true） */
    public static boolean isNeedTransferNamespace(String tenant) {
        return StringUtils.isBlank(tenant);
    }
    
    /**
     * Set default namespace id. Invoke settings at system startup.
     *
     * @param namespaceDefaultId 参数不能为 null
     */
    public static void setNamespaceDefaultId(String namespaceDefaultId) {
        NamespaceUtil.namespaceDefaultId = namespaceDefaultId;
    }
    
    /**
     * Get default namespace id.
     *
     * @return namespace id
      * <p>Config 命名空间归一化；详见类级说明。</p>
     */
    public static String getNamespaceDefaultId() {
        return NamespaceUtil.namespaceDefaultId;
    }
    
    /**
     * Judge whether is default namespaceId.
     *
     * @param namespaceId to check namespaceId
     * @return {@code true} if equals default namespaceId, otherwise {@code false}.
      * <p>Config 命名空间归一化；详见类级说明。</p>
     */
    public static boolean isDefaultNamespaceId(String namespaceId) {
        return StringUtils.equals(namespaceId, getNamespaceDefaultId());
    }
}
