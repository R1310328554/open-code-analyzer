/**
 * 服务器特性（Feature）开关的 REST 表示，用于查询或切换预览/实验性功能。
 */
export default interface FeatureRepresentation {
  /** 特性内部名称（如 admin-fine-grained-authz） */
  name: string;
  /** 管理界面或文档中展示的可读标签 */
  label: string;
  /** 特性分类（默认启用、预览、实验性等） */
  type: FeatureType;
  /** 当前是否已启用 */
  enabled: boolean;
  /** 是否已标记为废弃 */
  deprecated?: boolean;
  /** 启用本特性前必须先启用的依赖特性名称列表 */
  dependencies: string[];
}

/** 特性生命周期与默认启用策略的分类枚举 */
export enum FeatureType {
  /** 默认启用且为稳定特性 */
  Default = "DEFAULT",
  /** 默认禁用，需显式开启 */
  DisabledByDefault = "DISABLED_BY_DEFAULT",
  /** 预览特性，默认启用 */
  Preview = "PREVIEW",
  /** 预览特性，默认禁用 */
  PreviewDisabledByDefault = "PREVIEW_DISABLED_BY_DEFAULT",
  /** 实验性特性，可能随时变更或移除 */
  Experimental = "EXPERIMENTAL",
  /** 已废弃特性，建议迁移替代方案 */
  Deprecated = "DEPRECATED",
}
