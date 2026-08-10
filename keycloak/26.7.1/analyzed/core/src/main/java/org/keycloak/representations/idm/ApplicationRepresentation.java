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

/**
 * 已弃用的 Application 资源表示，功能由 {@link ClientRepresentation} 取代。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Deprecated
public class ApplicationRepresentation extends ClientRepresentation {
    /** 应用显示名称。 */
    protected String name;
    /** 已弃用的声明（claim）配置。 */
    @Deprecated
    protected ClaimRepresentation claims;

    /** @return 应用名称 */
    public String getName() {
        return name;
    }

    /** @param name 应用名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 声明配置 */
    public ClaimRepresentation getClaims() {
        return claims;
    }

    /** @param claims 声明配置 */
    public void setClaims(ClaimRepresentation claims) {
        this.claims = claims;
    }
}
