/**
 * 服务端 Profile 特性开关检测 Hook。
 * 结合 ServerInfo 中的 features 列表与当前管理员权限，决定 UI 是否展示某实验/可选特性。
 */
import { useServerInfo } from "../context/server-info/ServerInfoProvider";
import { useAccess } from "../context/access/Access";

/** Keycloak Admin UI 关心的 Profile 特性枚举（与服务器 feature 名一致）。 */
export enum Feature {
  AccountV3 = "ACCOUNT_V3",
  AdminFineGrainedAuthz = "ADMIN_FINE_GRAINED_AUTHZ",
  AdminFineGrainedAuthzV2 = "ADMIN_FINE_GRAINED_AUTHZ_V2",
  ClientPolicies = "CLIENT_POLICIES",
  Kerberos = "KERBEROS",
  ParameterizedScopes = "PARAMETERIZED_SCOPES",
  DPoP = "DPOP",
  DeviceFlow = "DEVICE_FLOW",
  TransientUsers = "TRANSIENT_USERS",
  ClientTypes = "CLIENT_TYPES",
  DeclarativeUI = "DECLARATIVE_UI",
  Organizations = "ORGANIZATION",
  OpenId4VCI = "OID4VC_VCI",
  QuickTheme = "QUICK_THEME",
  StandardTokenExchangeV2 = "TOKEN_EXCHANGE_STANDARD_V2",
  JWTAuthorizationGrant = "JWT_AUTHORIZATION_GRANT",
  Passkeys = "PASSKEYS",
  ClientAuthFederated = "CLIENT_AUTH_FEDERATED",
  Workflows = "WORKFLOWS",
  StepUpAuthenticationSaml = "STEP_UP_AUTHENTICATION_SAML",
  Ssf = "SSF",
  ScimApi = "SCIM_API",
  IdentityBrokeringAPIV1 = "IDENTITY_BROKERING_API_V1",
  IdentityBrokeringAPIV2 = "IDENTITY_BROKERING_API_V2",
}

/** 去掉特性名末尾 _V数字 后缀，便于 V1/V2 等等价比较。 */
export const unversionedName = (name: string) => name.replace(/_V\d+$/, "");

/**
 * 返回 isFeatureEnabled：特性在服务器已启用且当前用户具备访问该特性 UI 的权限时为 true。
 */
export default function useIsFeatureEnabled() {
  const { features } = useServerInfo();
  const { hasAccess } = useAccess();

  /** 部分特性除服务器开关外还需额外 realm 权限（如组织管理）。 */
  const hasFeatureAccess = (feature: Feature) => {
    switch (feature) {
      case Feature.Organizations:
        return hasAccess(({ hasAny }) =>
          hasAny("manage-realm", "query-organizations"),
        );
      default:
        return true;
    }
  };

  return function isFeatureEnabled(feature: Feature) {
    if (!features) {
      return false;
    }
    return features
      .filter((f) => f.enabled && hasFeatureAccess(f.name as Feature))
      .map((f) => f.name)
      .includes(feature);
  };
}

/**
 * 与 useIsFeatureEnabled 互补：按去版本化后的名称判断特性是否被禁用。
 * 用于隐藏已废弃或未启用的 V1 菜单项等场景。
 */
export function useIsFeatureDisabled() {
  const { features } = useServerInfo();

  return function isFeatureDisabled(feature: Feature) {
    if (!features) {
      return true;
    }
    return !features.some(
      (f) => f.enabled && unversionedName(f.name!) === unversionedName(feature),
    );
  };
}
