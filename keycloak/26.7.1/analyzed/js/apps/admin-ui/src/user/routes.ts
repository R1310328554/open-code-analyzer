/** 管理控制台「用户」模块的路由表聚合导出。 */
import type { AppRouteObject } from "../routes";
import { AddUserRoute } from "./routes/AddUser";
import { UserRoute } from "./routes/User";
import { UsersRoute, UsersRouteWithTab } from "./routes/Users";

/** 新增用户、用户列表（含 Tab）、单个用户详情等子路由。 */
const routes: AppRouteObject[] = [
  AddUserRoute,
  UsersRoute,
  UsersRouteWithTab,
  UserRoute,
];

export default routes;
