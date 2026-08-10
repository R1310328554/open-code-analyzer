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

package com.alibaba.nacos.api.model;

import com.alibaba.nacos.api.exception.api.NacosApiException;

import java.io.Serializable;

/**
 * Nacos HTTP 表单 API 对象接口。
 *
 * <p>控制台与 Open API 的 Form 参数对象实现本接口，在提交前调用 {@link #validate()} 校验参数合法性。</p>
 *
 * @author xiweng.yy
 */
public interface NacosForm extends Serializable {
    
    /**
     * 校验表单参数合法性。
     *
     * @throws NacosApiException 参数不合法时抛出
     */
    void validate() throws NacosApiException;
}
