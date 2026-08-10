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

package com.alibaba.nacos.core.code;

import com.alibaba.nacos.core.code.condition.ParamRequestCondition;
import com.alibaba.nacos.core.code.condition.PathRequestCondition;

import java.util.Comparator;

/**
 * 请求映射信息：聚合路径条件与参数条件，供 {@link ControllerMethodsCache} 匹配 HTTP 请求。
 * Request mapping information. to find the matched method by request
 *
 * @author horizonzy
 * @since 1.3.2
 */
public class RequestMappingInfo {
    
    /** HTTP 方法与路径表达式条件。 */
    private PathRequestCondition pathRequestCondition;
    
    /** 请求参数匹配条件。 */
    private ParamRequestCondition paramRequestCondition;
    
    /** 返回参数匹配条件。 */
    public ParamRequestCondition getParamRequestCondition() {
        return paramRequestCondition;
    }
    
    /** 设置参数匹配条件。 */
    public void setParamRequestCondition(ParamRequestCondition paramRequestCondition) {
        this.paramRequestCondition = paramRequestCondition;
    }
    
    /** 设置路径匹配条件。 */
    public void setPathRequestCondition(PathRequestCondition pathRequestCondition) {
        this.pathRequestCondition = pathRequestCondition;
    }
    
    @Override
    public String toString() {
        return "RequestMappingInfo{" + "pathRequestCondition=" + pathRequestCondition
            + ", paramRequestCondition="
            + paramRequestCondition + '}';
    }
    
    /** 按参数表达式数量降序排序，优先匹配约束更多的映射。 */
    public static class RequestMappingInfoComparator implements Comparator<RequestMappingInfo> {
        
        /** 参数条件越多优先级越高。 */
        @Override
        public int compare(RequestMappingInfo o1, RequestMappingInfo o2) {
            return Integer.compare(o2.getParamRequestCondition().getExpressions().size(),
                o1.getParamRequestCondition().getExpressions().size());
        }
    }
}
