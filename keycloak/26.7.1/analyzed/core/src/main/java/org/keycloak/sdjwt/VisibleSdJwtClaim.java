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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 明文可见的 SD-JWT 顶层声明，无需选择性披露。
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class VisibleSdJwtClaim extends AbstractSdJwtClaim {
    /** 声明值。 */
    private final JsonNode claimValue;

    /** @param claimName 声明名称 */
    public VisibleSdJwtClaim(SdJwtClaimName claimName, JsonNode claimValue) {
        super(claimName);
        this.claimValue = claimValue;
    }

    @Override
    public JsonNode getVisibleClaimValue(String hashAlgo) {
        return claimValue;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VisibleSdJwtClaim)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        VisibleSdJwtClaim that = (VisibleSdJwtClaim) o;
        return Objects.equals(claimValue, that.claimValue);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(claimValue);
        return result;
    }

    /** 创建构建器实例。 */
    public static Builder builder() {
        return new Builder();
    }

    /** {@link VisibleSdJwtClaim} 构建器。 */
    public static class Builder {
        private SdJwtClaimName claimName;
        private JsonNode claimValue;

        public Builder withClaimName(String claimName) {
            this.claimName = new SdJwtClaimName(claimName);
            return this;
        }

        public Builder withClaimValue(JsonNode claimValue) {
            this.claimValue = claimValue;
            return this;
        }

        public VisibleSdJwtClaim build() {
            claimName = Objects.requireNonNull(claimName, "claimName must not be null");
            claimValue = Objects.requireNonNull(claimValue, "claimValue must not be null");
            return new VisibleSdJwtClaim(claimName, claimValue);
        }
    }

    @Override
    public List<String> getDisclosureStrings() {
        return Collections.emptyList();
    }
}
