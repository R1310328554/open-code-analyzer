/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.common.utils.StringUtils;

/**
 * Prompt data id utility methods.
 * <p>Prompt 在 Nacos Config 中的 dataId 构造与识别工具：descriptor、标签版本映射、latest 与版本化 prompt 等后缀规则。</p>
 *
 * @author nacos
 */
public final class PromptDataIdUtils {
    
    private PromptDataIdUtils() {
    }
    
    /** 构造 Prompt meta（descriptor）dataId，等价于 {@link #buildDescriptorDataId(String)}。 */
    public static String buildMetaDataId(String promptKey) {
        return buildDescriptorDataId(promptKey);
    }
    
    /** 构造 descriptor dataId：promptKey + {@link com.alibaba.nacos.ai.constant.Constants.Prompt#DESCRIPTOR_DATA_ID_SUFFIX}。 */
    public static String buildDescriptorDataId(String promptKey) {
        return promptKey + Constants.Prompt.DESCRIPTOR_DATA_ID_SUFFIX;
    }
    
    /** @deprecated 请使用 {@link #buildDescriptorDataId(String)}。 */
    @Deprecated
    public static String buildAdminInfoDataId(String promptKey) {
        return buildDescriptorDataId(promptKey);
    }
    
    /** 构造标签→版本映射 dataId。 */
    public static String buildLabelVersionMappingDataId(String promptKey) {
        return promptKey + Constants.Prompt.LABEL_VERSION_MAPPING_DATA_ID_SUFFIX;
    }
    
    /** 构造 latest prompt 内容 dataId（无版本号后缀）。 */
    public static String buildLatestDataId(String promptKey) {
        return promptKey + Constants.Prompt.PROMPT_DATA_ID_SUFFIX;
    }
    
    /** 构造指定版本的 prompt 内容 dataId：promptKey.version + 后缀。 */
    public static String buildVersionDataId(String promptKey, String version) {
        return promptKey + "." + version + Constants.Prompt.PROMPT_DATA_ID_SUFFIX;
    }
    
    /**
     * Check whether dataId is prompt meta dataId.
     * <p>判断 dataId 是否为 Prompt descriptor（meta）dataId。</p>
     *
     * @param dataId config dataId
     * @return true if meta dataId
     */
    public static boolean isMetaDataId(String dataId) {
        return isDescriptorDataId(dataId);
    }
    
    /** 判断 dataId 是否以 descriptor 后缀结尾。 */
    public static boolean isDescriptorDataId(String dataId) {
        return StringUtils.isNotBlank(dataId)
            && dataId.endsWith(Constants.Prompt.DESCRIPTOR_DATA_ID_SUFFIX);
    }
    
    /** @deprecated 请使用 {@link #isDescriptorDataId(String)}。 */
    @Deprecated
    public static boolean isAdminInfoDataId(String dataId) {
        return isDescriptorDataId(dataId);
    }
    
    /**
     * Check whether dataId is prompt label/version mapping dataId.
     * <p>判断 dataId 是否为标签与版本映射配置。</p>
     *
     * @param dataId config dataId
     * @return true if mapping dataId
     */
    public static boolean isLabelVersionMappingDataId(String dataId) {
        return StringUtils.isNotBlank(dataId)
            && dataId.endsWith(Constants.Prompt.LABEL_VERSION_MAPPING_DATA_ID_SUFFIX);
    }
    
    /**
     * Extract prompt key from prompt meta dataId.
     * <p>从 meta/descriptor dataId 提取 promptKey；非法时返回 null。</p>
     *
     * @param dataId config dataId
     * @return prompt key if valid, otherwise null
     */
    public static String extractPromptKeyFromMetaDataId(String dataId) {
        return extractPromptKeyFromDescriptorDataId(dataId);
    }
    
    /**
     * Extract prompt key from prompt descriptor dataId.
     * <p>从 descriptor dataId 去掉后缀得到 promptKey。</p>
     *
     * @param dataId config dataId
     * @return prompt key if valid, otherwise null
     */
    public static String extractPromptKeyFromDescriptorDataId(String dataId) {
        if (!isDescriptorDataId(dataId)) {
            return null;
        }
        return dataId.substring(0,
            dataId.length() - Constants.Prompt.DESCRIPTOR_DATA_ID_SUFFIX.length());
    }
    
    /** @deprecated 请使用 {@link #extractPromptKeyFromDescriptorDataId(String)}。 */
    @Deprecated
    public static String extractPromptKeyFromAdminInfoDataId(String dataId) {
        return extractPromptKeyFromDescriptorDataId(dataId);
    }
    
    /**
     * Extract prompt key from mapping dataId.
     * <p>从标签版本映射 dataId 提取 promptKey。</p>
     *
     * @param dataId config dataId
     * @return prompt key if valid, otherwise null
     */
    public static String extractPromptKeyFromLabelVersionMappingDataId(String dataId) {
        if (!isLabelVersionMappingDataId(dataId)) {
            return null;
        }
        return dataId.substring(0,
            dataId.length() - Constants.Prompt.LABEL_VERSION_MAPPING_DATA_ID_SUFFIX.length());
    }
}
