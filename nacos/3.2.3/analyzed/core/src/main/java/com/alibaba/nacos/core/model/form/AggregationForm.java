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

package com.alibaba.nacos.core.model.form;

import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.NacosForm;

/**
 * HTTP 聚合查询 API 的表单参数，控制是否对下游结果做聚合。
 * <p>实现 {@link NacosForm}，默认 {@code aggregation=true}。</p>
 * Nacos HTTP Aggregatable API Form.
 *
 * @author xiweng.yy
 */
public class AggregationForm implements NacosForm {
    
    private static final long serialVersionUID = 3585575371677025046L;
    
    /** 是否启用结果聚合，默认 true。 */
    private boolean aggregation = Boolean.TRUE;
    
    /** 本表单无额外校验规则。 */
    @Override
    public void validate() throws NacosApiException {
    }
    
    /** 是否开启聚合。 */
    public boolean isAggregation() {
        return aggregation;
    }
    
    /** 设置是否开启聚合。 */
    public void setAggregation(boolean aggregation) {
        this.aggregation = aggregation;
    }
}
