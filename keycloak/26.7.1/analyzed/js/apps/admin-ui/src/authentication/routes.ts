import type { AppRouteObject } from "../routes";
import {
  AuthenticationRoute,
  AuthenticationRouteWithTab,
} from "./routes/Authentication";
import { CreateFlowRoute } from "./routes/CreateFlow";
import { FlowRoute, FlowWithBuiltInRoute } from "./routes/Flow";

/** 认证模块路由表：列表、带标签页视图、创建流及流详情（含内置流）。 */
const routes: AppRouteObject[] = [
  AuthenticationRoute,
  AuthenticationRouteWithTab,
  CreateFlowRoute,
  FlowRoute,
  FlowWithBuiltInRoute,
];

export default routes;
