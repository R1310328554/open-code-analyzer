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

package com.alibaba.nacos.api.naming.pojo;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.NacosForm;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.selector.NoneSelector;
import com.alibaba.nacos.api.selector.Selector;
import com.alibaba.nacos.api.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Nacos 命名服务中的服务定义模型。
 *
 * <p>采用「服务 → 集群 → 实例」三级结构：服务下挂多个集群，集群内包含多个实例。
 * 通常将实例间的共性属性（如保护阈值、选择器）提升到服务级别统一管理。</p>
 *
 * @author nkorange
 */
public class Service implements NacosForm {
    
    private static final long serialVersionUID = -3470985546826874460L;
    
    /** 服务所在命名空间 ID。 */
    private String namespaceId;
    
    /** 服务所属分组名。 */
    private String groupName;
    
    /** 服务名称。 */
    private String name;
    
    /** 是否为临时服务。 */
    private boolean ephemeral;
    
    /** 健康实例比例保护阈值，低于该比例时可能触发保护策略。 */
    private float protectThreshold = 0.0F;
    
    /** 服务扩展元数据。 */
    private Map<String, String> metadata = new HashMap<>();
    
    /** 实例路由选择器，默认不做过滤。 */
    private Selector selector = new NoneSelector();
    
    /** 获取命名空间 ID。 */
    public String getNamespaceId() {
        return namespaceId;
    }
    
    /** 设置命名空间 ID。 */
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    /** 获取分组名。 */
    public String getGroupName() {
        return groupName;
    }
    
    /** 设置分组名。 */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    /** 获取服务名。 */
    public String getName() {
        return name;
    }
    
    /** 设置服务名。 */
    public void setName(String name) {
        this.name = name;
    }
    
    /** 是否为临时服务。 */
    public boolean isEphemeral() {
        return ephemeral;
    }
    
    /** 设置是否为临时服务。 */
    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }
    
    /** 获取健康实例保护阈值。 */
    public float getProtectThreshold() {
        return protectThreshold;
    }
    
    /** 设置健康实例保护阈值。 */
    public void setProtectThreshold(float protectThreshold) {
        this.protectThreshold = protectThreshold;
    }
    
    /** 获取服务元数据。 */
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    /** 设置服务元数据。 */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    /** 向元数据中添加键值对。 */
    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }
    
    /** 获取实例路由选择器。 */
    public Selector getSelector() {
        return selector;
    }
    
    /** 设置实例路由选择器。 */
    public void setSelector(Selector selector) {
        this.selector = selector;
    }
    
    @Override
    public void validate() throws NacosApiException {
        fillDefaultValue();
        if (StringUtils.isBlank(name)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
                "Required parameter 'name' type String is not present");
        }
    }
    
    /**
     * 填充命名空间 ID、分组名等默认值。
     */
    public void fillDefaultValue() {
        if (StringUtils.isBlank(namespaceId)) {
            namespaceId = Constants.DEFAULT_NAMESPACE_ID;
        }
        if (StringUtils.isBlank(groupName)) {
            groupName = Constants.DEFAULT_GROUP;
        }
    }
}
