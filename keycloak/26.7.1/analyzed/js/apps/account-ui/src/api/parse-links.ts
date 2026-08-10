/** HTTP Link 头解析结果：上一页与下一页的分页查询参数。 */
export type Links = {
  prev?: Record<string, string>;
  next?: Record<string, string>;
};

/**
 * 从 HTTP 响应的 Link 头解析分页链接。
 * 将 rel=prev/next 对应 URL 的查询参数提取为键值对，供客户端翻页使用。
 */
export function parseLinks(response: Response): Links {
  const linkHeader = response.headers.get("link");

  if (!linkHeader) {
    return {};
  }

  // 按逗号分割多个 Link 条目
  const links = linkHeader.split(/,\s*</);
  return links.reduce<Links>((acc: Links, link: string) => {
    const matcher = /<?([^>]*)>(.*)/.exec(link);
    if (!matcher) return {};
    const linkUrl = matcher[1];
    const rel = /\s*(.+)\s*=\s*"?([^"]+)"?/.exec(matcher[2]);
    if (rel) {
      const link: Record<string, string> = {};
      // 将 URL 查询参数转为平面对象
      for (const [key, value] of new URL(linkUrl).searchParams.entries()) {
        link[key] = value;
      }
      acc[rel[2] as keyof Links] = link;
    }
    return acc;
  }, {});
}
