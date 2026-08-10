import type { AppRouteObject } from "../routes";
import { SessionsRoute } from "./routes/Sessions";

/** 会话管理模块路由：在线用户/客户端会话列表与撤销操作。 */
const routes: AppRouteObject[] = [SessionsRoute];

export default routes;
