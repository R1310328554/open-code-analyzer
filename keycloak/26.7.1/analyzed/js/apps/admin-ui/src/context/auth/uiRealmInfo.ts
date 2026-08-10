/**
 * 领域级 UI 元信息：对该领域所有管理员可见的运行时开关与能力描述。
 */
export interface UiRealmInfo {
  /** 是否至少启用了一个用户存储/用户配置 Provider */
  userProfileProvidersEnabled?: boolean;
}
