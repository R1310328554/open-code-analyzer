/**
 * Workflows（工作流）功能模块路由聚合。
 * 导出列表页与详情页路由对象，由主 routes 树挂载。
 */
import type { AppRouteObject } from "../routes";
import { WorkflowsRoute } from "./routes/Workflows";
import { WorkflowDetailRoute } from "./routes/WorkflowDetail";

const routes: AppRouteObject[] = [WorkflowsRoute, WorkflowDetailRoute];

export default routes;
