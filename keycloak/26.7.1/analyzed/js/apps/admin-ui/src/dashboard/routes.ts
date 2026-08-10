import type { AppRouteObject } from "../routes";
import {
  DashboardRoute,
  DashboardRouteWithRealm,
  DashboardRouteWithTab,
} from "./routes/Dashboard";

/** 仪表盘模块路由：默认入口、带领域上下文及 Tab 子页。 */
const routes: AppRouteObject[] = [
  DashboardRoute,
  DashboardRouteWithRealm,
  DashboardRouteWithTab,
];

export default routes;
