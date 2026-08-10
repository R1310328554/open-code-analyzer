// interface.ts — Pipeline 日志、DSL 组件图与分块数据结构类型定义。

import { PipelineResultSearchParams } from './constant';

/** DSL 组件运行时参数：输入输出、重试、超时与调试字段。 */
export interface ComponentParams {
  debug_inputs: Record<string, any>;
  delay_after_error: number;
  description: string;
  exception_default_value: any;
  exception_goto: any;
  exception_method: any;
  inputs: Record<string, any>;
  field_name: string;
  max_retries: number;
  message_history_window_size: number;
  outputs: {
    _created_time: Record<string, any>;
    _elapsed_time: Record<string, any>;
    name: Record<string, any>;
    output_format: { type: string; value: string };
    json: { type: string; value: string };
  };
  persist_logs: boolean;
  timeout: number;
}

interface ComponentObject {
  component_name: string;
  params: ComponentParams;
}
/** 单个 Pipeline 组件：上下游 ID、component_name 与 params。 */
export interface IDslComponent {
  downstream: Array<string>;
  obj: ComponentObject;
  upstream: Array<string>;
}

interface NodeData {
  label: string;
  name: string;
  form?: {
    outputs?: Record<
      string,
      {
        type: string;
        value: string | Array<Record<string, any>> | number;
      }
    >;
    setups?: Array<Record<string, any>>;
    chunk_token_size?: number;
    delimiters?: Array<{
      value: string;
    }>;
    overlapped_percent?: number;
  };
}

interface EdgeData {
  isHovered: boolean;
}

interface Position {
  x: number;
  y: number;
}

interface Measured {
  height: number;
  width: number;
}

interface Node {
  data: NodeData;
  dragging: boolean;
  id: string;
  measured: Measured;
  position: Position;
  selected: boolean;
  sourcePosition: string;
  targetPosition: string;
  type: string;
}

interface Edge {
  data: EdgeData;
  id: string;
  source: string;
  sourceHandle: string;
  target: string;
  targetHandle: string;
}
interface GraphData {
  edges: Edge[];
  nodes: Node[];
}

/** 知识库 Pipeline 文件处理日志的完整后端响应结构。 */
export interface IPipelineFileLogDetail {
  avatar: string;
  create_date: string;
  create_time: number;
  document_id: string;
  document_name: string;
  document_suffix: string;
  document_type: string;
  dsl: {
    components: {
      [key: string]: IDslComponent;
    };
    graph: GraphData;
    task_id: string;
    path: Array<string>;
  };
  id: string;
  kb_id: string;
  operation_status: string;
  parser_id: string;
  pipeline_id: string;
  pipeline_title: string;
  process_begin_at: string;
  process_duration: number;
  progress: number;
  progress_msg: string;
  source_from: string;
  status: string;
  task_type: string;
  tenant_id: string;
  update_date: string;
  update_time: number;
}

/** 检索分块实体：正文、位置、关键词与 tag_feas 等。 */
export interface IChunk {
  /** 是否启用该分块：0 禁用，1 启用 */
  available_int?: number; // Whether to enable, 0: not enabled, 1: enabled
  chunk_id?: string;
  content_with_weight?: string;
  doc_id?: string;
  doc_name?: string;
  image_id?: string;
  important_kwd?: string[];
  /** 关联问句关键词 */
  question_kwd?: string[]; // keywords
  tag_kwd?: string[];
  positions: number[][];
  tag_feas?: Record<string, number>;
  text: string;
}

/** 编程式跳转到 Dataflow 结果页时携带的路由参数。 */
export interface NavigateToDataflowResultProps {
  id: string;
  [PipelineResultSearchParams.KnowledgeId]?: string;
  [PipelineResultSearchParams.DocumentId]: string;
  [PipelineResultSearchParams.AgentId]?: string;
  [PipelineResultSearchParams.AgentTitle]?: string;
  [PipelineResultSearchParams.IsReadOnly]?: string;
  [PipelineResultSearchParams.Type]: string;
  [PipelineResultSearchParams.CreatedBy]?: string;
  [PipelineResultSearchParams.DocumentExtension]?: string;
}
