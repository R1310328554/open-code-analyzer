/**
 * UI 扩展 SPI 的 Java 接口全限定名，用于识别自定义页面与标签页提供方。
 * Admin Console 通过 Server Info 中的 provider 元数据匹配这些类型。
 */
/** 自定义整页扩展的 Provider 接口标识。 */
export const PAGE_PROVIDER = "org.keycloak.services.ui.extend.UiPageProvider";
/** 在现有页面内挂载标签页的 Provider 接口标识。 */
export const TAB_PROVIDER = "org.keycloak.services.ui.extend.UiTabProvider";
