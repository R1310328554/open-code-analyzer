/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.representations.idm;

import java.util.List;

/**
 * 创建 Client Initial Access Token 的请求体，指定有效期、可用次数及允许的 Web Origins。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClientInitialAccessCreatePresentation {

    /** 令牌过期时间（秒），自创建起计。 */
    private Integer expiration;

    /** 令牌允许注册客户端的最大次数。 */
    private Integer count;

    /** 允许使用该令牌注册客户端的 Web Origins 白名单。 */
    private List<String> webOrigins;

    /** 默认无参构造器。 */
    public ClientInitialAccessCreatePresentation() {
    }

    /**
     * 构造指定过期时间与可用次数的创建请求。
     *
     * @param expiration 过期秒数
     * @param count 最大可用次数
     */
    public ClientInitialAccessCreatePresentation(Integer expiration, Integer count) {
        this.expiration = expiration;
        this.count = count;
    }

    /** @return 过期秒数 */
    public Integer getExpiration() {
        return expiration;
    }

    /** @param expiration 过期秒数 */
    public void setExpiration(Integer expiration) {
        this.expiration = expiration;
    }

    /** @return 最大可用次数 */
    public Integer getCount() {
        return count;
    }

    /** @param count 最大可用次数 */
    public void setCount(Integer count) {
        this.count = count;
    }

    /** @return Web Origins 白名单 */
    public List<String> getWebOrigins() {
        return webOrigins;
    }

    /** @param webOrigins Web Origins 白名单 */
    public void setWebOrigins(List<String> webOrigins) {
        this.webOrigins = webOrigins;
    }
}
