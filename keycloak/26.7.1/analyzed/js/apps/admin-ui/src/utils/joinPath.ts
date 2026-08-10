/** 多段 URL/路径片段的安全拼接（规范化首尾斜杠）。 */
const PATH_SEPARATOR = "/";

/** 用 `/` 连接各段；首段保留 leading slash，末段保留 trailing slash。 */
export function joinPath(...paths: string[]) {
  const normalizedPaths = paths.map((path, index) => {
    const isFirst = index === 0;
    const isLast = index === paths.length - 1;

    // 非首段去掉 leading slash，避免双斜杠
    if (!isFirst && path.startsWith(PATH_SEPARATOR)) {
      path = path.slice(1);
    }

    // 非末段去掉 trailing slash
    if (!isLast && path.endsWith(PATH_SEPARATOR)) {
      path = path.slice(0, -1);
    }

    return path;
  }, []);

  return normalizedPaths.join(PATH_SEPARATOR);
}
