import type ClientRepresentation from "@keycloak/keycloak-admin-client/lib/defs/clientRepresentation";
import type { TFunction } from "i18next";

/**
 * 判断客户端是否为领域内部客户端（realm client），用于认证领域本身而非第三方应用。
 */
export const isRealmClient = (client: ClientRepresentation): boolean =>
  client.attributes?.["realm_client"] === true.toString();

/**
 * 将协议标识符转换为 i18n 可读名称；未知协议原样返回。
 */
export const getProtocolName = (t: TFunction<"clients">, protocol: string) => {
  switch (protocol) {
    case "openid-connect":
      return t("protocolTypes.openid-connect");
    case "saml":
      return t("protocolTypes.saml");
    case "oid4vc":
      return t("protocolTypes.oid4vc");
    default:
      return protocol;
  }

  return protocol;
};

/** 客户端授权上下文表达式中可选的内置属性与枚举值（供策略编辑器选用）。 */
export const defaultContextAttributes = [
  {
    key: "custom",
    name: "Custom Attribute...",
    custom: true,
  },
  {
    key: "kc.identity.authc.method",
    name: "Authentication Method",
    values: [
      {
        key: "pwd",
        name: "Password",
      },
      {
        key: "otp",
        name: "One-Time Password",
      },
      {
        key: "kbr",
        name: "Kerberos",
      },
    ],
  },
  {
    key: "kc.realm.name",
    name: "Realm",
  },
  {
    key: "kc.time.date_time",
    name: "Date/Time (MM/dd/yyyy hh:mm:ss)",
  },
  {
    key: "kc.client.network.ip_address",
    name: "Client IPv4 Address",
  },
  {
    key: "kc.client.network.host",
    name: "Client Host",
  },
  {
    key: "kc.client.user_agent",
    name: "Client/User Agent",
  },
];
