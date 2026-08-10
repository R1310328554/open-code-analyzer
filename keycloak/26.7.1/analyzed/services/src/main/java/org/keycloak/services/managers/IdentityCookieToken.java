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
package org.keycloak.services.managers;

import org.keycloak.TokenCategory;
import org.keycloak.representations.AccessToken;

/**
 * 身份 Cookie 令牌。
 * <p>继承 {@link AccessToken}，类别为 {@link TokenCategory#INTERNAL}，用于内部身份 Cookie 场景。</p>
 */
public class IdentityCookieToken extends AccessToken {

    /** {@inheritDoc} 返回内部令牌类别 */
    /** {@inheritDoc} 返回内部令牌类别 */
    @Override
    public TokenCategory getCategory() {
        return TokenCategory.INTERNAL;
    }

}
