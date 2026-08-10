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

import com.alibaba.nacos.common.utils.StringUtils;

/**
 * Aliyun RAM context.
 * <p>RAM 鉴权运行时上下文：持有 AccessKey、SecretKey、RAM 角色名与签名 region，供各 ResourceInjector 生成请求签名。</p>
 *
 * @author xiweng.yy
 */
public class RamContext {
    
    /** 阿里云 AccessKey ID */
    private String accessKey;
    
    /** 阿里云 AccessKey Secret */
    private String secretKey;
    
    /** 可选 RAM 角色名，走 STS 临时凭证时使用 */
    private String ramRoleName;
    
    /** V4 签名 region 标识 */
    private String regionId;
    
    /** @return AccessKey ID */
    public String getAccessKey() {
        return accessKey;
    }
    
    /** @param accessKey AccessKey ID */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
    
    /** @return AccessKey Secret */
    public String getSecretKey() {
        return secretKey;
    }
    
    /** @param secretKey AccessKey Secret */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
    
    /** @return RAM 角色名，可为 null */
    public String getRamRoleName() {
        return ramRoleName;
    }
    
    /** @param ramRoleName RAM 角色名 */
    public void setRamRoleName(String ramRoleName) {
        this.ramRoleName = ramRoleName;
    }
    
    /** @return 签名 regionId */
    public String getRegionId() {
        return regionId;
    }
    
    /** @param regionId 签名 regionId */
    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }
    
    /**
     * Validate the RAM context.
     *
     * @return true if the context is valid
     *         配置了 RAM 角色名，或 AK/SK 均非空时为 true
     */
    public boolean validate() {
        return StringUtils.isNotBlank(ramRoleName)
            || StringUtils.isNotBlank(accessKey) && StringUtils
                .isNotBlank(secretKey);
    }
}
