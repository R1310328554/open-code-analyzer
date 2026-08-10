import type { AppRouteObject } from "../routes";
import { ClientScopeRoute } from "./routes/ClientScope";
import { ClientScopesRoute } from "./routes/ClientScopes";
import { MapperRoute } from "./routes/Mapper";
import { NewClientScopeRoute } from "./routes/NewClientScope";

/** 客户端作用域模块路由：新建、映射器、详情与列表页。 */
const routes: AppRouteObject[] = [
  NewClientScopeRoute,
  MapperRoute,
  ClientScopeRoute,
  ClientScopesRoute,
];

export default routes;
