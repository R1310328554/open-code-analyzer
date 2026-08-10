import { CallOptions } from "../api/methods";
import { MenuItem } from "../root/PageNav";
import { joinPath } from "../utils/joinPath";

/**
 * 从静态资源加载账户控制台自定义导航内容（content.json）。
 * 用于扩展或覆盖默认侧边栏菜单项。
 */
export default async function fetchContentJson(
  opts: CallOptions,
): Promise<MenuItem[]> {
  const response = await fetch(
    joinPath(opts.context.environment.resourceUrl, "/content.json"),
    opts,
  );
  return await response.json();
}
