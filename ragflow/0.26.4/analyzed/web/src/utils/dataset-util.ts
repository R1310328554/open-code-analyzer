/**
 * dataset-util.ts — 知识库解析器类型判断：知识图谱与 Naive 分块策略。
 */

import { DocumentParserType } from '@/constants/knowledge';

/** 判断解析器是否为 KnowledgeGraph（知识图谱）类型。 */
export function isKnowledgeGraphParser(parserId: DocumentParserType) {
  return parserId === DocumentParserType.KnowledgeGraph;
}

/** 判断解析器是否为 Naive（通用分块）类型。 */
export function isNaiveParser(parserId: DocumentParserType) {
  return parserId === DocumentParserType.Naive;
}
