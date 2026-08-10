// utils.ts — 知识库设置页分块方法说明图：按 chunk_method 映射静态资源路径。

/** 生成 chunk-method/{prefix}-0N 形式的图片资源名列表。 */
const getImageName = (prefix: string, length: number) =>
  new Array(length)
    .fill(0)
    .map((x, idx) => `chunk-method/${prefix}-0${idx + 1}`);

/** 各解析/分块方法对应的说明插图路径数组。 */
export const ImageMap = {
  book: getImageName('book', 4),
  laws: getImageName('law', 2),
  manual: getImageName('manual', 4),
  picture: getImageName('media', 2),
  naive: getImageName('naive', 2),
  paper: getImageName('paper', 2),
  presentation: getImageName('presentation', 2),
  qa: getImageName('qa', 2),
  resume: getImageName('resume', 2),
  table: getImageName('table', 2),
  one: getImageName('one', 2),
  knowledge_graph: getImageName('knowledge-graph', 2),
  tag: getImageName('tag', 2),
};
