package org.keycloak.protocol.oid4vc.clientpolicy;

import java.util.Objects;
import java.util.Optional;

import org.keycloak.protocol.oid4vc.model.CredentialScopeRepresentation;

/**
 * 布尔型凭证客户端策略：比较 Scope 属性当前值与期望值。
 * <p>常用于开关类策略，例如是否强制 Credential Offer。</p>
 */
public class PredicateCredentialClientPolicy extends CredentialClientPolicy<Boolean> {

    /**
     * @param name 策略名称
     * @param key Scope 属性键
     * @param exp 期望值
     * @param def 默认值
     */
    public PredicateCredentialClientPolicy(String name, String key, Boolean exp, Boolean def) {
        super(name, key, Boolean.class, exp, def);
    }

    /** {@inheritDoc} 解析布尔属性，缺失时使用默认值。 */
    public Boolean getCurrentValue(CredentialScopeRepresentation credScope) {
        Boolean scopeValue = Optional.ofNullable(credScope.getAttribute(getAttrName()))
                .map(Boolean::parseBoolean)
                .orElse(getDefaultValue());
        return scopeValue;
    }

    /**
     * 判断 Scope 当前策略值是否等于期望值。
     * @param credScope 凭证 Scope 表示
     * @return 满足策略时 {@code true}
     */
    public Boolean validate(CredentialScopeRepresentation credScope) {
        Boolean scopeValue = credScope.getCredentialPolicyValue(this);
        return Objects.equals(scopeValue, getExpectedValue());
    }

}
