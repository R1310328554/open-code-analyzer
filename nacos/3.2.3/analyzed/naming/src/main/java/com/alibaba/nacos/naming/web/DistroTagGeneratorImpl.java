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

package com.alibaba.nacos.naming.web;

import com.alibaba.nacos.core.utils.ReuseHttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Distro 标签生成器 Spring 默认实现。
 *
 * <p>当前集群统一使用 {@link DistroIpPortTagGenerator}；预留按成员版本切换策略的扩展点。</p>
 *
 * @author xiweng.yy
 */
@Component
public class DistroTagGeneratorImpl implements DistroTagGenerator {
    
    private final DistroTagGenerator ipPortTag = new DistroIpPortTagGenerator();
    
    public DistroTagGeneratorImpl() {
    }
    
    @Override
    public String getResponsibleTag(ReuseHttpServletRequest request) {
        return getTagGenerator().getResponsibleTag(request);
    }
    
    /**
     * 按集群成员能力选择具体标签生成策略。
     *
     * <p>成员均为 2.x 及以上时使用 {@link DistroIpPortTagGenerator}。</p>
     *
     * @return actual tag generator
     */
    private DistroTagGenerator getTagGenerator() {
        return ipPortTag;
    }
}
