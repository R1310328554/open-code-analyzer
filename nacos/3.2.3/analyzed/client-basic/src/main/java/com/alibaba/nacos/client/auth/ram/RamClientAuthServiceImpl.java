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

package com.alibaba.nacos.client.auth.ram;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.auth.ram.identify.StsConfig;
import com.alibaba.nacos.client.auth.ram.injector.AbstractResourceInjector;
import com.alibaba.nacos.client.auth.ram.injector.AiResourceInjector;
import com.alibaba.nacos.client.auth.ram.injector.ConfigResourceInjector;
import com.alibaba.nacos.client.auth.ram.injector.LockResourceInjector;
import com.alibaba.nacos.client.auth.ram.injector.NamingResourceInjector;
import com.alibaba.nacos.client.auth.ram.utils.RamUtil;
import com.alibaba.nacos.client.auth.ram.utils.SpasAdapter;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.api.LoginIdentityContext;
import com.alibaba.nacos.plugin.auth.api.RequestResource;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import com.alibaba.nacos.plugin.auth.spi.client.AbstractClientAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Client Auth service implementation for aliyun RAM.
 * <p>阿里云 RAM/STS 客户端鉴权：从 Properties 或 Credential 文件加载 AK/SK、RAM 角色与 region，再按 {@link RequestResource} 类型委托对应 {@link com.alibaba.nacos.client.auth.ram.injector.AbstractResourceInjector} 注入签名头。</p>
 *
 * @author xiweng.yy
 */
public class RamClientAuthServiceImpl extends AbstractClientAuthService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RamClientAuthServiceImpl.class);
    
    /** RAM 凭证与角色上下文 */
    private final RamContext ramContext;
    
    /** 资源类型（naming/config/lock/ai）到签名注入器的映射 */
    private final Map<String, AbstractResourceInjector> resourceInjectors;
    
    /** 注册 naming、config、lock、ai 四类资源的 RAM 签名注入器。 */
    public RamClientAuthServiceImpl() {
        ramContext = new RamContext();
        resourceInjectors = new HashMap<>();
        resourceInjectors.put(SignType.NAMING, new NamingResourceInjector());
        resourceInjectors.put(SignType.CONFIG, new ConfigResourceInjector());
        resourceInjectors.put(SignType.LOCK, new LockResourceInjector());
        resourceInjectors.put(SignType.AI, new AiResourceInjector());
    }
    
    /** {@inheritDoc} 加载 RAM 角色名、AK/SK 与签名 region，已有效则短路返回。 */
    @Override
    public Boolean login(Properties properties) {
        if (ramContext.validate()) {
            return true;
        }
        loadRoleName(properties);
        loadAccessKey(properties);
        loadSecretKey(properties);
        loadRegionId(properties);
        return true;
    }
    
    /** 从 Properties 读取 RAM 角色名并同步到 {@link com.alibaba.nacos.client.auth.ram.identify.StsConfig}。 */
    private void loadRoleName(Properties properties) {
        String ramRoleName = properties.getProperty(PropertyKeyConst.RAM_ROLE_NAME);
        if (!StringUtils.isBlank(ramRoleName)) {
            StsConfig.getInstance().setRamRoleName(ramRoleName);
            ramContext.setRamRoleName(ramRoleName);
        }
    }
    
    /** 通过 {@link com.alibaba.nacos.client.auth.ram.utils.RamUtil} 解析 AccessKey。 */
    private void loadAccessKey(Properties properties) {
        ramContext.setAccessKey(RamUtil.getAccessKey(properties));
    }
    
    /** 通过 RamUtil 解析 SecretKey。 */
    private void loadSecretKey(Properties properties) {
        ramContext.setSecretKey(RamUtil.getSecretKey(properties));
    }
    
    /** 读取 V4 签名使用的 regionId（{@link com.alibaba.nacos.api.PropertyKeyConst#SIGNATURE_REGION_ID}）。 */
    private void loadRegionId(Properties properties) {
        String regionId = properties.getProperty(PropertyKeyConst.SIGNATURE_REGION_ID);
        ramContext.setRegionId(regionId);
    }
    
    /** {@inheritDoc} 按资源类型注入 RAM 签名相关参数到 {@link LoginIdentityContext}。 */
    @Override
    public LoginIdentityContext getLoginIdentityContext(RequestResource resource) {
        LoginIdentityContext result = new LoginIdentityContext();
        if (!ramContext.validate() || notFountInjector(resource.getType())) {
            return result;
        }
        resourceInjectors.get(resource.getType()).doInject(resource, ramContext, result);
        return result;
    }
    
    /** 判断是否存在对应类型的注入器；缺失时打 warn 并返回 true。 */
    private boolean notFountInjector(String type) {
        if (!resourceInjectors.containsKey(type)) {
            LOGGER.warn("Injector for type {} not found, will use default ram identity context.",
                type);
            return true;
        }
        return false;
    }
    
    /** {@inheritDoc} 释放 SPAS Credential 单例等资源。 */
    @Override
    public void shutdown() throws NacosException {
        SpasAdapter.freeCredentialInstance();
    }
}
