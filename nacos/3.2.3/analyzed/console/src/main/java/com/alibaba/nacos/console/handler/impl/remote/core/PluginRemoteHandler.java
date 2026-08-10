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

package com.alibaba.nacos.console.handler.impl.remote.core;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.plugin.ConfigItemDefinition;
import com.alibaba.nacos.api.plugin.ConfigItemType;
import com.alibaba.nacos.console.handler.core.PluginHandler;
import com.alibaba.nacos.console.handler.impl.remote.EnabledRemoteHandler;
import com.alibaba.nacos.console.handler.impl.remote.NacosMaintainerClientHolder;
import com.alibaba.nacos.core.plugin.model.vo.PluginDetailVO;
import com.alibaba.nacos.core.plugin.model.vo.PluginInfoVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 插件管理远程 Handler：通过 {@link NacosMaintainerClientHolder} 调用远端 Naming Maintainer API 查询插件列表与详情，并将 Map 响应转换为 VO。
 * Remote implementation of PluginHandler that handles plugin-related operations via HTTP.
 *
 * @author WangzJi
 */
@Service
@EnabledRemoteHandler
public class PluginRemoteHandler implements PluginHandler {
    
    /** JSON 字段名：插件 ID */
    private static final String FIELD_PLUGIN_ID = "pluginId";
    
    /** JSON 字段名：插件类型 */
    private static final String FIELD_PLUGIN_TYPE = "pluginType";
    
    /** JSON 字段名：插件名称 */
    private static final String FIELD_PLUGIN_NAME = "pluginName";
    
    /** JSON 字段名：是否启用 */
    private static final String FIELD_ENABLED = "enabled";
    
    /** JSON 字段名：是否关键插件 */
    private static final String FIELD_CRITICAL = "critical";
    
    /** JSON 字段名：是否可配置 */
    private static final String FIELD_CONFIGURABLE = "configurable";
    
    /** JSON 字段名：是否互斥型插件 */
    private static final String FIELD_EXCLUSIVE = "exclusive";
    
    /** JSON 字段名：集群总节点数 */
    private static final String FIELD_TOTAL_NODE_COUNT = "totalNodeCount";
    
    /** JSON 字段名：可用节点数 */
    private static final String FIELD_AVAILABLE_NODE_COUNT = "availableNodeCount";
    
    /** JSON 字段名：插件运行配置 */
    private static final String FIELD_CONFIG = "config";
    
    /** JSON 字段名：配置项定义列表 */
    private static final String FIELD_CONFIG_DEFINITIONS = "configDefinitions";
    
    /** JSON 字段名：配置项键 */
    private static final String FIELD_KEY = "key";
    
    /** JSON 字段名：配置项显示名 */
    private static final String FIELD_NAME = "name";
    
    /** JSON 字段名：配置项描述 */
    private static final String FIELD_DESCRIPTION = "description";
    
    /** JSON 字段名：配置项默认值 */
    private static final String FIELD_DEFAULT_VALUE = "defaultValue";
    
    /** JSON 字段名：配置项是否必填 */
    private static final String FIELD_REQUIRED = "required";
    
    /** JSON 字段名：配置项类型 */
    private static final String FIELD_TYPE = "type";
    
    /** JSON 字段名：枚举可选值列表 */
    private static final String FIELD_ENUM_VALUES = "enumValues";
    
    /** 运维客户端持有者，提供 Naming Maintainer 远程访问能力 */
    private final NacosMaintainerClientHolder clientHolder;
    
    /** 注入运维客户端持有者 */
    public PluginRemoteHandler(NacosMaintainerClientHolder clientHolder) {
        this.clientHolder = clientHolder;
    }
    
    /** 列出远端插件并转换为 {@link PluginInfoVO} 列表。 */
    @Override
    public List<PluginInfoVO> listPlugins(String pluginType) throws NacosException {
        List<Map<String, Object>> rawList =
            clientHolder.getNamingMaintainerService().listPlugins(pluginType);
        List<PluginInfoVO> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            result.add(convertToPluginInfoVO(raw));
        }
        return result;
    }
    
    /** 获取远端指定插件的详细配置与定义。 */
    @Override
    public PluginDetailVO getPluginDetail(String pluginType, String pluginName)
        throws NacosException {
        Map<String, Object> raw =
            clientHolder.getNamingMaintainerService().getPluginDetail(pluginType, pluginName);
        return convertToPluginDetailVO(raw);
    }
    
    /** 启用或禁用远端插件，支持仅本地节点生效。 */
    @Override
    public void updatePluginStatus(String pluginType, String pluginName, boolean enabled,
        boolean localOnly)
        throws NacosException {
        clientHolder.getNamingMaintainerService().updatePluginStatus(pluginType, pluginName,
            enabled, localOnly);
    }
    
    /** 更新远端插件运行配置，支持仅本地节点生效。 */
    @Override
    public void updatePluginConfig(String pluginType, String pluginName, Map<String, String> config,
        boolean localOnly) throws NacosException {
        clientHolder.getNamingMaintainerService().updatePluginConfig(pluginType, pluginName, config,
            localOnly);
    }
    
    /** 查询远端指定插件在各集群节点上的可用性映射。 */
    @Override
    public Map<String, Boolean> getPluginAvailability(String pluginType, String pluginName)
        throws NacosException {
        return clientHolder.getNamingMaintainerService().getPluginAvailability(pluginType,
            pluginName);
    }
    
    /** 将远端返回的 Map 转换为 {@link PluginInfoVO}。 */
    private PluginInfoVO convertToPluginInfoVO(Map<String, Object> raw) {
        PluginInfoVO vo = new PluginInfoVO();
        vo.setPluginId((String) raw.get(FIELD_PLUGIN_ID));
        vo.setPluginType((String) raw.get(FIELD_PLUGIN_TYPE));
        vo.setPluginName((String) raw.get(FIELD_PLUGIN_NAME));
        vo.setEnabled(Boolean.TRUE.equals(raw.get(FIELD_ENABLED)));
        vo.setCritical(Boolean.TRUE.equals(raw.get(FIELD_CRITICAL)));
        vo.setConfigurable(Boolean.TRUE.equals(raw.get(FIELD_CONFIGURABLE)));
        vo.setExclusive(Boolean.TRUE.equals(raw.get(FIELD_EXCLUSIVE)));
        if (raw.get(FIELD_TOTAL_NODE_COUNT) != null) {
            vo.setTotalNodeCount(((Number) raw.get(FIELD_TOTAL_NODE_COUNT)).intValue());
        }
        if (raw.get(FIELD_AVAILABLE_NODE_COUNT) != null) {
            vo.setAvailableNodeCount(((Number) raw.get(FIELD_AVAILABLE_NODE_COUNT)).intValue());
        }
        return vo;
    }
    
    @SuppressWarnings("unchecked")
    /** 将远端返回的 Map 转换为 {@link PluginDetailVO}。 */
    private PluginDetailVO convertToPluginDetailVO(Map<String, Object> raw) {
        PluginDetailVO vo = new PluginDetailVO();
        vo.setPluginId((String) raw.get(FIELD_PLUGIN_ID));
        vo.setPluginType((String) raw.get(FIELD_PLUGIN_TYPE));
        vo.setPluginName((String) raw.get(FIELD_PLUGIN_NAME));
        vo.setEnabled(Boolean.TRUE.equals(raw.get(FIELD_ENABLED)));
        vo.setCritical(Boolean.TRUE.equals(raw.get(FIELD_CRITICAL)));
        vo.setConfigurable(Boolean.TRUE.equals(raw.get(FIELD_CONFIGURABLE)));
        if (raw.get(FIELD_CONFIG) != null) {
            vo.setConfig((Map<String, String>) raw.get(FIELD_CONFIG));
        }
        if (raw.get(FIELD_CONFIG_DEFINITIONS) != null) {
            List<Map<String, Object>> rawDefinitions =
                (List<Map<String, Object>>) raw.get(FIELD_CONFIG_DEFINITIONS);
            vo.setConfigDefinitions(convertToConfigItemDefinitions(rawDefinitions));
        }
        return vo;
    }
    
    @SuppressWarnings("unchecked")
    /** 将远端配置项定义 Map 列表转换为 {@link ConfigItemDefinition} 列表。 */
    private List<ConfigItemDefinition> convertToConfigItemDefinitions(
        List<Map<String, Object>> rawList) {
        List<ConfigItemDefinition> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            ConfigItemDefinition definition = new ConfigItemDefinition();
            definition.setKey((String) raw.get(FIELD_KEY));
            definition.setName((String) raw.get(FIELD_NAME));
            definition.setDescription((String) raw.get(FIELD_DESCRIPTION));
            definition.setDefaultValue((String) raw.get(FIELD_DEFAULT_VALUE));
            definition.setRequired(Boolean.TRUE.equals(raw.get(FIELD_REQUIRED)));
            if (raw.get(FIELD_TYPE) != null) {
                String typeStr = raw.get(FIELD_TYPE).toString();
                try {
                    definition.setType(ConfigItemType.valueOf(typeStr));
                } catch (IllegalArgumentException e) {
                    definition.setType(ConfigItemType.STRING);
                }
            }
            if (raw.get(FIELD_ENUM_VALUES) != null) {
                definition.setEnumValues((List<String>) raw.get(FIELD_ENUM_VALUES));
            }
            result.add(definition);
        }
        return result;
    }
}
