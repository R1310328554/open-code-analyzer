package org.keycloak.models;

/**
 * 控制 IdP 在账户控制台中的可见性策略。
 *
 * @author Réda Housni Alaoui
 */
public enum IdentityProviderShowInAccountConsole {
	/** 始终显示 */ ALWAYS,
	/** 仅已关联时显示 */ WHEN_LINKED,
	/** 从不显示 */ NEVER
}
