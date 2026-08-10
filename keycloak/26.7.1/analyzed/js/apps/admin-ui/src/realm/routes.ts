import type { AppRouteObject } from "../routes";
import { RealmRoute } from "./RealmRoutes";

/** 领域（Realm）管理模块路由：当前仅包含领域选择与切换入口。 */
const routes: AppRouteObject[] = [RealmRoute];

export default routes;
