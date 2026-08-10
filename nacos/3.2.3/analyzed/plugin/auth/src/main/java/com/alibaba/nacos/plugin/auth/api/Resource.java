/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.api;

import com.alibaba.nacos.common.utils.StringUtils;

import java.io.Serializable;
import java.util.Properties;

/**
 * 授权体系中的资源描述，标识受权限保护的业务实体。
 *
 * <p>由命名空间、分组、名称、类型及扩展属性组成，服务端认证插件据此判断
 * 当前身份是否拥有访问权限。</p>
 *
 * @author nkorange
 * @author mai.jh
 * @since 1.2.0
 */
public class Resource implements Serializable {
    
    private static final long serialVersionUID = 925971662931204553L;
    
    /**
     * 空资源占位符，表示无需绑定具体资源的授权场景。
     */
    public static final Resource EMPTY_RESOURCE =
        new Resource(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            StringUtils.EMPTY, null);
    
    /**
     * 资源所属命名空间 ID。
     */
    private final String namespaceId;
    
    /**
     * 资源所属分组。
     */
    private final String group;
    
    /**
     * 资源名称（如服务名或 dataId）。
     */
    private final String name;
    
    /**
     * 资源类型，参见 {@link com.alibaba.nacos.plugin.auth.constant.SignType}。
     */
    private final String type;
    
    /**
     * 资源扩展属性，用于携带 AI 类型等附加信息。
     */
    private final Properties properties;
    
    public Resource(String namespaceId, String group, String name, String type,
        Properties properties) {
        this.namespaceId = namespaceId;
        this.group = group;
        this.name = name;
        this.type = type;
        this.properties = properties;
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public String getGroup() {
        return group;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public Properties getProperties() {
        return properties;
    }
    
    @Override
    public String toString() {
        return "Resource{" + "namespaceId='" + namespaceId + '\'' + ", group='" + group + '\''
            + ", name='" + name
            + '\'' + ", type='" + type + '\'' + ", properties=" + properties + '}';
    }
}
