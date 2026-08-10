import type { AppRouteObject } from "../routes";
import { GroupsRoute, GroupsWithIdRoute } from "./routes/Groups";

/** 组管理模块路由：组列表及带嵌套组 id 的详情路由。 */
const routes: AppRouteObject[] = [GroupsRoute, GroupsWithIdRoute];

export default routes;
