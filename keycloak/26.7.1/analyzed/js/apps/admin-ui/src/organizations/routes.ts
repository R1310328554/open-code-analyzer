import type { AppRouteObject } from "../routes";
import { AddOrganizationRoute } from "./routes/AddOrganization";
import { EditOrganizationRoute } from "./routes/EditOrganization";
import { OrganizationsRoute } from "./routes/Organizations";

/** 组织（Organizations）模块路由：列表、新建与编辑详情页。 */
const routes: AppRouteObject[] = [
  OrganizationsRoute,
  AddOrganizationRoute,
  EditOrganizationRoute,
];

export default routes;
