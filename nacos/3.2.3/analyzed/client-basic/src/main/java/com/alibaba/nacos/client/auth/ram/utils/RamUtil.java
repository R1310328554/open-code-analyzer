/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.auth.ram.utils;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.SystemPropertyKeyConst;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.Properties;

/**
 * Util to get ram info, such as AK, SK and RAM role.
 * <p>RAM 凭证读取工具：优先从 {@link java.util.Properties} 取 AK/SK；开启 RAM 信息解析且属性为空时回退 {@link SpasAdapter} 本地凭证。</p>
 *
 * @author xiweng.yy
 */
public class RamUtil {
    
    /** 从属性或 SPAS 解析 AccessKey */
    public static String getAccessKey(Properties properties) {
        boolean isUseRamInfoParsing = Boolean.parseBoolean(properties
            .getProperty(PropertyKeyConst.IS_USE_RAM_INFO_PARSING,
                System.getProperty(SystemPropertyKeyConst.IS_USE_RAM_INFO_PARSING,
                    Constants.DEFAULT_USE_RAM_INFO_PARSING)));
        
        String result = properties.getProperty(PropertyKeyConst.ACCESS_KEY);
        if (isUseRamInfoParsing && StringUtils.isBlank(result)) {
            result = SpasAdapter.getAk();
        }
        return result;
    }
    
    /** 从属性或 SPAS 解析 SecretKey */
    public static String getSecretKey(Properties properties) {
        boolean isUseRamInfoParsing = Boolean.parseBoolean(properties
            .getProperty(PropertyKeyConst.IS_USE_RAM_INFO_PARSING,
                System.getProperty(SystemPropertyKeyConst.IS_USE_RAM_INFO_PARSING,
                    Constants.DEFAULT_USE_RAM_INFO_PARSING)));
        
        String result = properties.getProperty(PropertyKeyConst.SECRET_KEY);
        if (isUseRamInfoParsing && StringUtils.isBlank(result)) {
            result = SpasAdapter.getSk();
        }
        return result;
    }
}
