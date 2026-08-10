/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.datasource.impl.oracle;

import com.alibaba.nacos.plugin.datasource.impl.enums.oracle.TrustedOracleFunctionEnum;
import com.alibaba.nacos.plugin.datasource.mapper.AbstractMapper;

/**
 * Oracle 数据源 Mapper 抽象基类。
 *
 * <p>继承 {@link com.alibaba.nacos.plugin.datasource.mapper.AbstractMapper}， 通过 {@link TrustedOracleFunctionEnum} 解析 Oracle 可信 SQL 函数。</p>
 *
 * @author liam.fu
 **/
public abstract class AbstractMapperByOracle extends AbstractMapper {
    
    /** 从 Oracle 可信函数白名单解析 SQL 函数片段。 */
    @Override
    public String getFunction(String functionName) {
        return TrustedOracleFunctionEnum.getFunctionByName(functionName);
    }
}
