import { getInjectedEnvironment } from "@keycloak/keycloak-ui-shared";
import { type AccountEnvironment } from ".";

/** 从页面注入的全局配置读取 Account Console 运行时环境（realm、locale、特性开关等）。 */
export const environment = getInjectedEnvironment<AccountEnvironment>();
