// Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// 官方 API 支持的 OCR / 文档解析模型枚举
export enum Model {
  PPOCRv5 = "PP-OCRv5",
  PPOCRv5Latin = "PP-OCRv5-latin",
  PPOCRv6 = "PP-OCRv6",
  PPStructureV3 = "PP-StructureV3",
  PaddleOCRVL = "PaddleOCR-VL",
  PaddleOCRVL15 = "PaddleOCR-VL-1.5",
  PaddleOCRVL16 = "PaddleOCR-VL-1.6",
}

// 纯 OCR 模型集合（PP-OCR 系列）
const OCR_MODELS = new Set<string>([Model.PPOCRv5, Model.PPOCRv5Latin, Model.PPOCRv6]);
// 文档解析模型集合（Structure / VL 系列）
const DOCUMENT_PARSING_MODELS = new Set<string>([
  Model.PPStructureV3,
  Model.PaddleOCRVL,
  Model.PaddleOCRVL15,
  Model.PaddleOCRVL16,
]);
// 视觉语言（VL）文档理解模型子集
const VL_MODELS = new Set<string>([
  Model.PaddleOCRVL,
  Model.PaddleOCRVL15,
  Model.PaddleOCRVL16,
]);

// 判断模型名是否为 OCR 专用模型
export function isOCRModel(
  model: string
): model is Model.PPOCRv5 | Model.PPOCRv5Latin | Model.PPOCRv6 {
  return OCR_MODELS.has(model);
}

// 判断模型是否属于文档解析类
export function isDocumentParsingModel(model: string): boolean {
  return DOCUMENT_PARSING_MODELS.has(model);
}

// 判断模型是否为 VL 视觉语言模型
export function isVLModel(model: string): boolean {
  return VL_MODELS.has(model);
}

// PP-OCR 推理可选参数（检测/识别阈值、可视化等）
export interface OCROptions {
  useDocOrientationClassify?: boolean;
  useDocUnwarping?: boolean;
  useTextlineOrientation?: boolean;
  textDetLimitSideLen?: number;
  textDetLimitType?: string;
  textDetThresh?: number;
  textDetBoxThresh?: number;
  textDetUnclipRatio?: number;
  textRecScoreThresh?: number;
  visualize?: boolean;
  [key: string]: unknown;
}

// PP-StructureV3 版面解析与表格/公式识别选项
export interface PPStructureV3Options {
  useDocOrientationClassify?: boolean;
  useDocUnwarping?: boolean;
  useTextlineOrientation?: boolean;
  useSealRecognition?: boolean;
  useTableRecognition?: boolean;
  useFormulaRecognition?: boolean;
  useChartRecognition?: boolean;
  useRegionDetection?: boolean;
  layoutThreshold?: number | Record<string, number>;
  layoutNms?: boolean;
  layoutUnclipRatio?: number | number[] | Record<string, number>;
  layoutMergeBboxesMode?: string | Record<string, string>;
  formatBlockContent?: boolean;
  textDetLimitSideLen?: number;
  textDetLimitType?: string;
  textDetThresh?: number;
  textDetBoxThresh?: number;
  textDetUnclipRatio?: number;
  textRecScoreThresh?: number;
  useWiredTableCellsTransToHtml?: boolean;
  useWirelessTableCellsTransToHtml?: boolean;
  useTableOrientationClassify?: boolean;
  useOcrResultsWithTableCells?: boolean;
  useE2eWiredTableRecModel?: boolean;
  useE2eWirelessTableRecModel?: boolean;
  markdownIgnoreLabels?: string[];
  prettifyMarkdown?: boolean;
  showFormulaNumber?: boolean;
  returnMarkdownImages?: boolean;
  outputFormats?: string[];
  visualize?: boolean;
  [key: string]: unknown;
}

// PaddleOCR-VL 多模态文档解析与生成参数
export interface PaddleOCRVLOptions {
  useDocOrientationClassify?: boolean;
  useDocUnwarping?: boolean;
  useLayoutDetection?: boolean;
  useChartRecognition?: boolean;
  useSealRecognition?: boolean;
  useOcrForImageBlock?: boolean;
  layoutThreshold?: number | Record<string, number>;
  layoutNms?: boolean;
  layoutUnclipRatio?: number | number[] | Record<string, number>;
  layoutMergeBboxesMode?: string | Record<string, string>;
  layoutShapeMode?: "rect" | "quad" | "poly" | "auto";
  promptLabel?: "ocr" | "formula" | "table" | "chart" | "seal" | "spotting";
  formatBlockContent?: boolean;
  repetitionPenalty?: number;
  temperature?: number;
  topP?: number;
  minPixels?: number;
  maxPixels?: number;
  maxNewTokens?: number;
  vlmExtraArgs?: Record<string, unknown>;
  mergeLayoutBlocks?: boolean;
  markdownIgnoreLabels?: string[];
  prettifyMarkdown?: boolean;
  showFormulaNumber?: boolean;
  restructurePages?: boolean;
  mergeTables?: boolean;
  relevelTitles?: boolean;
  returnMarkdownImages?: boolean;
  outputFormats?: string[];
  visualize?: boolean;
  [key: string]: unknown;
}

// 文档解析请求的 options 联合类型
export type DocParsingOptions = PPStructureV3Options | PaddleOCRVLOptions;

// OCR 任务提交请求体（URL 或本地路径二选一）
export interface OCRRequest {
  model?: Model | string;
  fileUrl?: string;
  filePath?: string;
  pageRanges?: string;
  batchId?: string;
  options?: OCROptions;
}

// 文档解析任务提交请求体
export interface DocParsingRequest {
  model?: Model | string;
  fileUrl?: string;
  filePath?: string;
  pageRanges?: string;
  batchId?: string;
  options?: DocParsingOptions;
}

// SDK 客户端构造选项：token、超时、自定义 fetch
export interface ClientOptions {
  token?: string;
  baseUrl?: string;
  timeout?: number;
  requestTimeout?: number;
  pollTimeout?: number;
  clientPlatform?: string;
  fetch?: typeof fetch;
}

// 保存远程资源到本地时的覆盖与文件名选项
export interface SaveResourceOptions {
  overwrite?: boolean;
  filename?: string;
}
