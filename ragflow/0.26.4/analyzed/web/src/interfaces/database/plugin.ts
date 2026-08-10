// plugin.ts — LLM 工具/插件元数据：参数 schema 与展示文案。

/** LLM 可用工具列表。 */
export type ILLMTools = ILLMToolMetadata[];

/** 单个 LLM 工具：名称、展示信息与参数 Map。 */
export interface ILLMToolMetadata {
    name: string;
    displayName: string;
    displayDescription: string;
    parameters: Map<string, ILLMToolParameter>;
}

/** 工具参数：JSON Schema type 与展示描述。 */
export interface ILLMToolParameter {
    type: string;
    displayDescription: string;
}
