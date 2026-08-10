package org.keycloak.models;

/**
 * 身份提供方映射器同步模式。
 */
public enum IdentityProviderMapperSyncMode {
    /** 继承 IdP 级同步模式 */ INHERIT,
    /** 遗留模式 */ LEGACY,
    /** 导入模式：首次登录导入属性 */ IMPORT,
    /** 强制模式：每次登录强制同步 */ FORCE
}
