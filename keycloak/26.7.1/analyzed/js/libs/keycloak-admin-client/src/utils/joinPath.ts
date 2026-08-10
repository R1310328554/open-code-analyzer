const PATH_SEPARATOR = "/";

/**
 * 拼接 URL 路径段，自动去除中间段的首尾多余斜杠。
 * 首段保留 leading slash，末段保留 trailing slash 的语义由调用方控制。
 */
export function joinPath(...paths: string[]) {
  const normalizedPaths = paths.map((path, index) => {
    const isFirst = index === 0;
    const isLast = index === paths.length - 1;

    // Strip out any leading slashes from the path.
    // 非首段去掉 leading `/`，避免双斜杠
    if (!isFirst && path.startsWith(PATH_SEPARATOR)) {
      path = path.slice(1);
    }

    // Strip out any trailing slashes from the path.
    // 非末段去掉 trailing `/`
    if (!isLast && path.endsWith(PATH_SEPARATOR)) {
      path = path.slice(0, -1);
    }

    return path;
  }, []);

  return normalizedPaths.join(PATH_SEPARATOR);
}
