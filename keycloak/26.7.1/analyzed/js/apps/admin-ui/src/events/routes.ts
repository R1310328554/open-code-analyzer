import type { AppRouteObject } from "../routes";
import { EventsRoute, EventsRouteWithTab } from "./routes/Events";

/** 事件审计模块路由：事件列表主路由及带 Tab 的子路由。 */
const routes: AppRouteObject[] = [EventsRoute, EventsRouteWithTab];

export default routes;
