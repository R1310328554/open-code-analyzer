/** URL 路径段分隔符。 */
const PATH_SEPARATOR = "/";

/**
 * 安全拼接多个 URL 路径段。
 * 除首段外去除前导斜杠，除末段外去除尾随斜杠，再用 "/" 连接。
 */
export function joinPath(...paths: string[]) {
  const normalizedPaths = paths.map((path, index) => {
    const isFirst = index === 0;
    const isLast = index === paths.length - 1;

    // 非首段：去掉前导斜杠，避免双斜杠
    if (!isFirst && path.startsWith(PATH_SEPARATOR)) {
      path = path.slice(1);
    }

    // 非末段：去掉尾随斜杠
    if (!isLast && path.endsWith(PATH_SEPARATOR)) {
      path = path.slice(0, -1);
    }

    return path;
  }, []);

  return normalizedPaths.join(PATH_SEPARATOR);
}
