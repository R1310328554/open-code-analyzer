/**
 * 从浏览器 pathname 解析组层级 id 路径段。
 * organizations 路由前缀更深，需跳过更多固定段；否则从第 3 段起为组 id 链。
 */
export const getId = (pathname: string) => {
  const pathParts = pathname.substring(1).split("/");
  const spliceStart = pathParts[1] === "organizations" ? 4 : 2;
  return pathParts.length > 1 ? pathParts.splice(spliceStart) : undefined;
};

/** 返回 pathname 中当前选中组（路径链末段）的 id。 */
export const getLastId = (pathname: string) => {
  const pathParts = getId(pathname);
  return pathParts ? pathParts[pathParts.length - 1] : undefined;
};
