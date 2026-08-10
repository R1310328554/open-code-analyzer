import { getInjectedEnvironment } from "@keycloak/keycloak-ui-shared";
import type { Environment } from "./environment-types";

/** 从页面注入脚本读取的全局环境对象，供 Admin UI 模块共享访问。 */
export const environment = getInjectedEnvironment<Environment>();
