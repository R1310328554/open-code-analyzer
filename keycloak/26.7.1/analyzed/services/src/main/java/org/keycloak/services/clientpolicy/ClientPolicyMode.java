package org.keycloak.services.clientpolicy;

/**
 * 客户端策略条件组合模式：决定多条 {@link ClientPolicyConditionProvider} 投票如何生效。
 */
public enum ClientPolicyMode {

    /**
     * 默认模式：至少一条条件为 yes、无 no；abstain 忽略。
     * Default condition mode. At least one condition must evaluate to `yes`. None of the conditions must evaluate to `no`. Conditions, which evaluate to `abstain` are ignored
     */
    DEFAULT,

    /**
     * 严格模式：全部条件必须为 yes，不允许 no 或 abstain。
     * All conditions must evaluate to `yes`. None of the conditions must evaluate to `no` or `abstain`.
     */
    STRICT
}
