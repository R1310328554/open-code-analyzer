/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.ai.utils;

import com.alibaba.nacos.common.utils.StringUtils;

import java.util.regex.Pattern;

/**
 * Utility class for prompt version validation and comparison.
 * <p>Prompt 语义化版本校验与比较工具，格式 major.minor.patch（如 1.0.0）。</p>
 *
 * <p>Version format: major.minor.patch (e.g., "1.0.0", "2.1.3")</p>
 *
 * @author nacos
 */
public class PromptVersionUtils {
    
    /** 版本正则：a.b.c，a/b/c 均为非负整数。 */
    /** Version pattern: a.b.c where a, b, c are non-negative integers. */
    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");
    
    /** 语义化版本段数（major.minor.patch）。 */
    /** Number of parts in semantic version (major.minor.patch). */
    private static final int VERSION_PARTS_COUNT = 3;
    
    /** JSON 文件后缀，用于 legacy dataId 规则。 */
    /** JSON file extension suffix. */
    private static final String JSON_SUFFIX = ".json";
    
    private PromptVersionUtils() {
    }
    
    /**
     * Validate version format.
     * <p>校验版本字符串是否符合 a.b.c 格式。</p>
     *
     * @param version version string
     * @return true if version is valid (a.b.c format)
     */
    public static boolean isValidVersion(String version) {
        if (StringUtils.isBlank(version)) {
            return false;
        }
        return VERSION_PATTERN.matcher(version).matches();
    }
    
    /**
     * Compare two versions.
     * <p>逐段比较 major/minor/patch，返回差值符号。</p>
     *
     * @param version1 first version
     * @param version2 second version
     * @return positive if version1 > version2, negative if version1 < version2, 0 if equal
     * @throws IllegalArgumentException if version format is invalid
     */
    public static int compareVersion(String version1, String version2) {
        if (!isValidVersion(version1)) {
            throw new IllegalArgumentException("Invalid version format: " + version1);
        }
        if (!isValidVersion(version2)) {
            throw new IllegalArgumentException("Invalid version format: " + version2);
        }
        
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");
        
        for (int i = 0; i < VERSION_PARTS_COUNT; i++) {
            int num1 = Integer.parseInt(parts1[i]);
            int num2 = Integer.parseInt(parts2[i]);
            if (num1 != num2) {
                return num1 - num2;
            }
        }
        return 0;
    }
    
    /**
     * Check if newVersion is greater than currentVersion.
     * <p>发布前校验：新版本须严格大于当前版本；首次发布或当前版本非法时放行。</p>
     *
     * @param newVersion     new version to publish
     * @param currentVersion current version (can be null or empty for first publish)
     * @return true if newVersion > currentVersion, or currentVersion is null/empty
     */
    public static boolean isVersionGreater(String newVersion, String currentVersion) {
        if (!isValidVersion(newVersion)) {
            return false;
        }
        if (StringUtils.isBlank(currentVersion)) {
            // 首次发布：任意合法版本均可
            return true;
        }
        if (!isValidVersion(currentVersion)) {
            // 当前版本非法：允许覆盖
            return true;
        }
        return compareVersion(newVersion, currentVersion) > 0;
    }
    
    /**
     * Build dataId from promptKey.
     * <p>legacy 规则：promptKey + .json。</p>
     *
     * @param promptKey prompt key
     * @return dataId (promptKey.json)
     */
    public static String buildDataId(String promptKey) {
        return promptKey + JSON_SUFFIX;
    }
    
    /**
     * Extract promptKey from dataId.
     * <p>从 dataId 去掉 .json 后缀得到 promptKey。</p>
     *
     * @param dataId dataId
     * @return promptKey
     */
    public static String extractPromptKey(String dataId) {
        if (StringUtils.isBlank(dataId)) {
            return null;
        }
        if (dataId.endsWith(JSON_SUFFIX)) {
            return dataId.substring(0, dataId.length() - JSON_SUFFIX.length());
        }
        return dataId;
    }
}
