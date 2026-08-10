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

package com.alibaba.nacos.client.utils;

import com.alibaba.nacos.api.SystemPropertyKeyConst;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.common.utils.StringUtils;

/**
 * Tenant Util.
 * <p>租户（命名空间）解析工具：从客户端属性 {@code tenant.id} 读取用户租户，并按 ACM 与 ANS 两种云上接入方式分别回退到 {@code acm.namespace} 或 ANS 命名空间属性。</p>
 *
 * @author Nacos
 */
public class TenantUtil {
    
    /** 启动时从 {@link NacosClientProperties#PROTOTYPE} 读取的 tenant.id */
    private static final String USER_TENANT;
    
    /** ACM 命名空间缺省值（空串） */
    private static final String DEFAULT_ACM_NAMESPACE = "";
    
    /** 系统属性/客户端属性键：租户 ID */
    private static final String TENANT_ID = "tenant.id";
    
    /** ACM 命名空间属性键 */
    private static final String ACM_NAMESPACE_PROPERTY = "acm.namespace";
    
    // 类加载时从客户端属性原型读取 tenant.id
    static {
        USER_TENANT = NacosClientProperties.PROTOTYPE.getProperty(TENANT_ID, "");
    }
    
    /**
     * Adapt the way ACM gets tenant on the cloud.
     * <p>
     * Note the difference between getting and getting ANS. Since the processing logic on the server side is different,
     * the default value returns differently.
     * </p>
     * <p>云上 ACM 场景获取租户：{@code tenant.id} 为空时回退 {@code acm.namespace}，缺省为空串（与服务端 ACM 逻辑一致）。</p>
     *
     * @return user tenant for acm
     */
    public static String getUserTenantForAcm() {
        String tmp = USER_TENANT;
        
        if (StringUtils.isBlank(USER_TENANT)) {
            tmp = NacosClientProperties.PROTOTYPE.getProperty(ACM_NAMESPACE_PROPERTY,
                DEFAULT_ACM_NAMESPACE);
        }
        
        return tmp;
    }
    
    /**
     * Adapt the way ANS gets tenant on the cloud.
     * <p>云上 ANS 场景获取租户：{@code tenant.id} 为空时回退 {@link SystemPropertyKeyConst#ANS_NAMESPACE} 对应属性。</p>
     *
     * @return user tenant for ans
     */
    public static String getUserTenantForAns() {
        String tmp = USER_TENANT;
        
        if (StringUtils.isBlank(USER_TENANT)) {
            tmp = NacosClientProperties.PROTOTYPE.getProperty(SystemPropertyKeyConst.ANS_NAMESPACE);
        }
        return tmp;
    }
}
