package org.keycloak.models;

/**
 * 身份提供方级用户属性同步模式。
 */
public enum IdentityProviderSyncMode {
    /** 遗留模式 */ LEGACY,
    /** 导入模式 */ IMPORT,
    /** 强制同步模式 */ FORCE
}
