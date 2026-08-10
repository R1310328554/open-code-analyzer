/** 带 URI 编码的路由路径生成，供链接与导航安全使用。 */
import { generatePath, type PathParam } from "react-router-dom";

/**
 * 路径模板中占位参数的对象类型。
 *
 * @example
 * const params: PathParams<"/user/:id"> = { id: "123" };
 */
export type PathParams<Path extends string> = {
  [key in PathParam<Path>]: string;
};

/**
 * 在 react-router generatePath 基础上对每个参数做 encodeURIComponent。
 *
 * @param originalPath 路由模板，如 `/user/:id`
 * @param params 占位符取值
 *
 * @example
 * const path = "/user/:id";
 * const params = { id: "123" };
 * const encodedPath = generateEncodedPath(path, params);
 * // encodedPath 为 "/user/123"
 */
export function generateEncodedPath<Path extends string>(
  originalPath: Path,
  params: PathParams<Path>,
): string {
  // 克隆参数，避免修改调用方对象
  const encodedParams = structuredClone(params);

  // 逐个编码，防止特殊字符破坏 URL
  for (const key in encodedParams) {
    const pathKey = key as PathParam<Path>;
    encodedParams[pathKey] = encodeURIComponent(encodedParams[pathKey]);
  }

  return generatePath(originalPath, encodedParams);
}
