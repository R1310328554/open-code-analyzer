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
 * 强类型盐值，避免参数混淆。
 *
 * <p>实现 {@link Comparable} 以支持 SD-JWT VC 中的排序。</p>
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class SdJwtSalt implements Comparable<SdJwtSalt> {
    /** 盐值字符串。 */
    private final String salt;

    /** @param salt 盐值，不可为空 */
    public SdJwtSalt(String salt) {
        this.salt = SdJwtUtils.requireNonEmpty(salt, "salt must not be empty");
    }

    /** 便捷工厂方法。 */
    public static SdJwtSalt of(String salt) {
        return new SdJwtSalt(salt);
    }

    @Override
    public String toString() {
        return salt;
    }

    @Override
    public int compareTo(SdJwtSalt o) {
        return salt.compareTo(o.salt);
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof SdJwtSalt)) {
            return false;
        }

        SdJwtSalt sdJwtSalt = (SdJwtSalt) o;
        return Objects.equals(salt, sdJwtSalt.salt);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(salt);
    }
}
