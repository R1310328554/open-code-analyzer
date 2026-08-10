package org.keycloak.config;


/**
 * 配置项支持级别枚举，标识选项的成熟度与稳定性。
 */
public enum ConfigSupportLevel {
    /** 已弃用，不建议在新部署中使用。 */
    DEPRECATED,
    /** 实验性，API 与行为可能变更。 */
    EXPERIMENTAL,
    /** 预览版，功能基本可用但尚未完全稳定。 */
    PREVIEW,
    /** 正式支持，推荐在生产环境使用。 */
    SUPPORTED
}
