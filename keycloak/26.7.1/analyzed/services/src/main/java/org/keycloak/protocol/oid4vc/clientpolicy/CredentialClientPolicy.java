package org.keycloak.protocol.oid4vc.clientpolicy;

import org.keycloak.protocol.oid4vc.model.CredentialScopeRepresentation;


/**
 * 凭证 Scope 上的声明式客户端策略抽象基类。
 * <p>将策略名称、Scope 属性键、期望类型/值与默认值绑定，由子类读取当前 Scope 属性。</p>
 * @param <T> 策略值类型
 */
public abstract class CredentialClientPolicy<T> {

    private final String name;
    private final String attrName;
    private final Class<T> type;
    private final T expectedValue;
    private final T defaultValue;

    /**
     * @param name 策略显示名称
     * @param attrName 凭证 Scope 属性键
     * @param type 值类型
     * @param expectedValue 策略期望满足的值
     * @param defaultValue 属性缺失时的默认值
     */
    public CredentialClientPolicy(String name, String attrName, Class<T> type, T expectedValue, T defaultValue) {
        this.name = name;
        this.attrName = attrName;
        this.expectedValue = expectedValue;
        this.defaultValue = defaultValue;
        this.type = type;
    }

    /** @return 策略名称 */
    public String getName() {
        return name;
    }

    /** @return Scope 属性键名 */
    public String getAttrName() {
        return attrName;
    }

    /** @return 策略值 Java 类型 */
    public Class<T> getType() {
        return type;
    }

    /** @return 策略期望的值 */
    public T getExpectedValue() {
        return expectedValue;
    }

    /** @return 属性未设置时的默认值 */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * 从凭证 Scope 表示读取当前策略值（可应用默认值）。
     * @param credScope 凭证 Scope 表示
     * @return 当前有效策略值
     */
    public abstract T getCurrentValue(CredentialScopeRepresentation credScope);
}
