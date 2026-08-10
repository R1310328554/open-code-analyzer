/**
 * Account Console 公共模块入口。
 * 导出页面组件、API 类型与方法、环境类型及通用 React 工具钩子。
 */
import { BaseEnvironment } from "@keycloak/keycloak-ui-shared";

export { PersonalInfo } from "./personal-info/PersonalInfo";
export { Header } from "./root/Header";
export { PageNav } from "./root/PageNav";
export { DeviceActivity } from "./account-security/DeviceActivity";
export { LinkedAccounts } from "./account-security/LinkedAccounts";
export { SigningIn } from "./account-security/SigningIn";
export type {
  AccountLinkUriRepresentation,
  Client,
  ClientRepresentation,
  ConsentRepresentation,
  ConsentScopeRepresentation,
  CredentialContainer,
  CredentialMetadataRepresentation,
  CredentialRepresentation,
  CredentialTypeMetadata,
  DeviceRepresentation,
  Group,
  LinkedAccountRepresentation,
  Permission,
  Permissions,
  Resource,
  Scope,
  SessionRepresentation,
  UserProfileAttributeMetadata,
  UserProfileMetadata,
  UserRepresentation,
} from "./api/representations";
export { Applications } from "./applications/Applications";
export { EmptyRow } from "./components/datalist/EmptyRow";
export { Page } from "./components/page/Page";
export { ContentComponent } from "./content/ContentComponent";
export { Groups } from "./groups/Groups";
export { EditTheResource } from "./resources/EditTheResource";
export { PermissionRequest } from "./resources/PermissionRequest";
export { Resources } from "./resources/Resources";
export { ResourcesTab } from "./resources/ResourcesTab";
export { ResourceToolbar } from "./resources/ResourceToolbar";
export { SharedWith } from "./resources/SharedWith";
export { Organizations } from "./organizations/Organizations";
export { ShareTheResource } from "./resources/ShareTheResource";
export {
  deleteConsent,
  deleteSession,
  getApplications,
  getCredentials,
  getDevices,
  getGroups,
  getLinkedAccounts,
  getPermissionRequests,
  getPersonalInfo,
  getSupportedLocales,
  savePersonalInfo,
  unLinkAccount,
} from "./api/methods";
/** Account Console 运行时环境：基址、语言、来源应用与特性开关。 */
export type AccountEnvironment = BaseEnvironment & {
  /** The URL to the root of the account console. */
  /** 账户控制台根 URL。 */
  baseUrl: string;
  /** The locale of the user */
  /** 当前用户语言区域。 */
  locale: string;
  /** Name of the referrer application in the back link */
  /** 返回链接中来源应用的显示名称。 */
  referrerName?: string;
  /** UR to the referrer application in the back link */
  /** 返回链接中来源应用的 URL。 */
  referrerUrl?: string;
  /** Feature flags */
  /** 由服务端启用的功能特性集合。 */
  features: Feature;
};

/** 账户控制台各功能模块的开关状态。 */
export type Feature = {
  isRegistrationEmailAsUsername: boolean;
  isEditUserNameAllowed: boolean;
  isLinkedAccountsEnabled: boolean;
  isMyResourcesEnabled: boolean;
  deleteAccountAllowed: boolean;
  updateEmailFeatureEnabled: boolean;
  updateEmailActionEnabled: boolean;
  isViewApplicationsEnabled: boolean;
  isViewGroupsEnabled: boolean;
  isViewOrganizationsEnabled: boolean;
  isOid4VciEnabled: boolean;
};

export { KeycloakProvider, useEnvironment } from "@keycloak/keycloak-ui-shared";
export { useAccountAlerts } from "./utils/useAccountAlerts";
export { usePromise } from "./utils/usePromise";
export { routes } from "./routes";
