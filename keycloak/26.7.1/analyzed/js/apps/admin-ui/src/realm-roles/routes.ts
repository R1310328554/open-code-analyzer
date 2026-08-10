import type { AppRouteObject } from "../routes";
import { AddRoleRoute } from "./routes/AddRole";
import { RealmRoleRoute } from "./routes/RealmRole";
import { RealmRolesRoute } from "./routes/RealmRoles";

/** 领域角色模块路由：角色列表、新建与角色详情（含复合角色等子页）。 */
const routes: AppRouteObject[] = [
  RealmRolesRoute,
  AddRoleRoute,
  RealmRoleRoute,
];

export default routes;
