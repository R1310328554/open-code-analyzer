/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.sdjwt;

import java.util.Objects;

/**
 * SD-JWT 声明的抽象基类，封装声明名称并提供 {@link SdJwtClaim} 的通用实现。
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 *
 */
public abstract class AbstractSdJwtClaim implements SdJwtClaim {
    /** 声明名称。 */
    private final SdJwtClaimName claimName;

    /** @param claimName 声明名称 */
    public AbstractSdJwtClaim(SdJwtClaimName claimName) {
        this.claimName = claimName;
    }

    @Override
    public SdJwtClaimName getClaimName() {
        return claimName;
    }

    @Override
    public String getClaimNameAsString() {
        return claimName.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractSdJwtClaim)) {
            return false;
        }

        AbstractSdJwtClaim that = (AbstractSdJwtClaim) o;
        return Objects.equals(claimName, that.claimName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(claimName);
    }
}
