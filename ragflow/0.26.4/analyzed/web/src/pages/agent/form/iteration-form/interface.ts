// interface.ts — Iteration 节点 outputs 的数组/对象两种表示类型。

/** 表单侧 outputs 列表：每项含 name、ref 与可选 type。 */
export type OutputArray = Array<{ name: string; ref: string; type?: string }>;
/** 画布 DSL 侧 outputs 映射：键为输出名，值为 ref/type。 */
export type OutputObject = Record<string, { ref: string; type?: string }>;
