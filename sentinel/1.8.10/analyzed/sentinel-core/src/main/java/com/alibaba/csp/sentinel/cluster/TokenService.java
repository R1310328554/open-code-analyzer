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
package com.alibaba.csp.sentinel.cluster;

import java.util.Collection;

/**
 * 流控服务接口。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public interface TokenService {

    /**
     * 向远程令牌服务端请求令牌。
     *
     * @param ruleId 唯一规则 ID
     * @param acquireCount 要获取的令牌数量
     * @param prioritized 请求是否优先
     * @return 令牌请求结果
     */
    TokenResult requestToken(Long ruleId, int acquireCount, boolean prioritized);

    /**
     * 向远程令牌服务端为特定参数请求令牌。
     *
     * @param ruleId 唯一规则 ID
     * @param acquireCount 要获取的令牌数量
     * @param params 参数列表
     * @return 令牌请求结果
     */
    TokenResult requestParamToken(Long ruleId, int acquireCount, Collection<Object> params);

    /**
     * 向远程令牌服务端请求获取并发令牌。
     *
     * @param clientAddress 请求所属客户端地址
     * @param ruleId 唯一规则 ID
     * @param acquireCount 要获取的令牌数量
     * @return 令牌请求结果
     */
    TokenResult requestConcurrentToken(String clientAddress,Long ruleId,int acquireCount);
    /**
     * 异步向远程令牌服务端请求释放并发令牌。
     *
     * @param tokenId 唯一令牌 ID
     */
    void releaseConcurrentToken(Long tokenId);
}
