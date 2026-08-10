// empty-dsl.ts — Dataflow 空白画布种子 DSL，与 React 解耦供纯函数/测试引用。

import { FileId, initialParserValues } from '@/pages/agent/constant/pipeline';

// 起始 File 节点在 DSL 中的组件名
const FILE_OPERATOR = 'File';

// Dataflow 种子 DSL：独立于 UI Hook，避免测试/运行时引入 React 依赖。
/** 默认 Dataflow 图：File 起始节点 → Parser 节点及空 components/retrieval 等。 */
export const DataflowEmptyDsl = {
  graph: {
    nodes: [
      {
        id: FileId,
        type: 'beginNode',
        position: {
          x: 50,
          y: 200,
        },
        data: {
          label: FILE_OPERATOR,
          name: FILE_OPERATOR,
        },
        sourcePosition: 'left',
        targetPosition: 'right',
      },
      {
        data: {
          form: initialParserValues,
          label: 'Parser',
          name: 'Parser_0',
        },
        dragging: false,
        id: 'Parser:HipSignsRhyme',
        measured: {
          height: 57,
          width: 200,
        },
        position: {
          x: 316.99524094206413,
          y: 195.39629819663406,
        },
        selected: true,
        sourcePosition: 'right',
        targetPosition: 'left',
        type: 'parserNode',
      },
    ],
    edges: [
      {
        id: 'xy-edge__Filestart-Parser:HipSignsRhymeend',
        source: FileId,
        sourceHandle: 'start',
        target: 'Parser:HipSignsRhyme',
        targetHandle: 'end',
      },
    ],
  },
  components: {
    [FILE_OPERATOR]: {
      obj: {
        component_name: FILE_OPERATOR,
        params: {},
      },
      downstream: [],
      upstream: [],
    },
  },
  retrieval: [],
  history: [],
  path: [],
  globals: {},
  variables: [],
};
