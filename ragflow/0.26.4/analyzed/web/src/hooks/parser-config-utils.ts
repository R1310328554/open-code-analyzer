// parser-config-utils.ts — 解析器/Raptor 配置提取：已知字段归位，未知字段合并进 ext。

/**
 * 解析器与 Raptor 配置工具：从配置对象提取已知字段，
 * 将其余未知键合并到 ext 以支持灵活扩展。
 */

/**
 * 提取 Raptor 配置：归一化 clustering_method/tree_builder，其余字段并入 ext。
 * @param raptorConfig - Raptor 配置对象
 * @returns 处理后的 Raptor 配置
 */
export const extractRaptorConfigExt = (
  raptorConfig: Record<string, any> | undefined,
) => {
  if (!raptorConfig) return raptorConfig;
  const {
    use_raptor,
    prompt,
    max_token,
    threshold,
    max_cluster,
    random_seed,
    scope,
    clustering_method,
    tree_builder,
    auto_disable_for_structured_data,
    ext,
    ...raptorExt
  } = raptorConfig;
  const extClusteringMethod = ext?.clustering_method;
  const normalizedClusteringMethod =
    clustering_method ?? extClusteringMethod ?? 'gmm';
  const normalizedTreeBuilder = tree_builder ?? ext?.tree_builder ?? 'raptor';

  return {
    use_raptor,
    prompt,
    max_token,
    threshold,
    max_cluster,
    random_seed,
    scope,
    auto_disable_for_structured_data,
    ext: {
      ...ext,
      ...raptorExt,
      clustering_method: normalizedClusteringMethod,
      tree_builder: normalizedTreeBuilder,
    },
  };
};

/**
 * 提取 Parser 配置：拆分 parent_child、递归处理 raptor，未知字段并入 ext。
 * @param parserConfig - 解析器配置对象
 * @returns 处理后的 Parser 配置
 */
export const extractParserConfigExt = (
  parserConfig: Record<string, any> | undefined,
) => {
  if (!parserConfig) return parserConfig;
  const {
    auto_keywords,
    auto_questions,
    chunk_token_num,
    delimiter,
    graphrag,
    html4excel,
    layout_recognize,
    raptor,
    tag_kb_ids,
    topn_tags,
    filename_embd_weight,
    task_page_size,
    pages,
    children_delimiter,
    use_parent_child,
    enable_children,
    ext,
    ...parserExt
  } = parserConfig;
  return {
    auto_keywords,
    auto_questions,
    chunk_token_num,
    delimiter,
    graphrag,
    html4excel,
    layout_recognize,
    raptor: extractRaptorConfigExt(raptor),
    tag_kb_ids,
    topn_tags,
    filename_embd_weight,
    task_page_size,
    pages,
    parent_child: enable_children
      ? {
          children_delimiter,
          use_parent_child: use_parent_child ?? enable_children,
        }
      : undefined,
    ext: { ...ext, ...parserExt },
  };
};
