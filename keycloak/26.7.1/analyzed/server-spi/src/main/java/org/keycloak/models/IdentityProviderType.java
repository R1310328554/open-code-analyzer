package org.keycloak.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.keycloak.models.IdentityProviderCapability.USER_LINKING;

/**
 * 身份提供方类型：区分用户认证、客户端断言、信任材料等用途。
 */
public enum IdentityProviderType {

    /** 任意类型 */ ANY,
    /** 用户认证（支持账号关联） */ USER_AUTHENTICATION(USER_LINKING),
    /** 客户端断言 */ CLIENT_ASSERTION,
    /** 信任材料 */ TRUST_MATERIAL,
    /** 外部令牌交换 */ EXCHANGE_EXTERNAL_TOKEN(USER_LINKING),
    /** JWT 授权授予 */ JWT_AUTHORIZATION_GRANT(USER_LINKING);

    private final Set<IdentityProviderCapability> capabilities;

    IdentityProviderType(IdentityProviderCapability... capabilities) {
        if (capabilities == null || capabilities.length == 0) {
            this.capabilities = Collections.emptySet();
        } else {
            this.capabilities = Arrays.stream(capabilities).collect(Collectors.toSet());
        }
    }

    /** @return 该类型支持的能力集合 */
    public Set<IdentityProviderCapability> getCapabilities() {
        return capabilities;
    }

}
