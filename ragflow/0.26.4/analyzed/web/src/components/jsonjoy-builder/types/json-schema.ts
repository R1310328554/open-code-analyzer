// json-schema.ts — JSON Schema 核心类型：Zod 定义、递归 schema 与类型守卫工具。

import { z } from 'zod';

// JSON Schema 基础类型枚举
const simpleTypes = [
  'string',
  'number',
  'integer',
  'boolean',
  'object',
  'array',
  'null',
] as const;

// baseSchema：Zod 为单一真相源，覆盖 draft 元数据与校验关键字
/** JSON Schema 非递归基础字段的 Zod 定义。 @public */
export const baseSchema = z.object({
  // Base schema properties
  $id: z.string().optional(),
  $schema: z.string().optional(),
  $ref: z.string().optional(),
  $anchor: z.string().optional(),
  $dynamicRef: z.string().optional(),
  $dynamicAnchor: z.string().optional(),
  $vocabulary: z.record(z.string(), z.boolean()).optional(),
  $comment: z.string().optional(),
  title: z.string().optional(),
  description: z.string().optional(),
  default: z.unknown().optional(),
  deprecated: z.boolean().optional(),
  readOnly: z.boolean().optional(),
  writeOnly: z.boolean().optional(),
  examples: z.array(z.unknown()).optional(),
  type: z.union([z.enum(simpleTypes), z.array(z.enum(simpleTypes))]).optional(),

  // String validations
  minLength: z.number().int().min(0).optional(),
  maxLength: z.number().int().min(0).optional(),
  pattern: z.string().optional(),
  format: z.string().optional(),
  contentMediaType: z.string().optional(),
  contentEncoding: z.string().optional(),

  // Number validations
  multipleOf: z.number().positive().optional(),
  minimum: z.number().optional(),
  maximum: z.number().optional(),
  exclusiveMinimum: z.number().optional(),
  exclusiveMaximum: z.number().optional(),

  // Array validations
  minItems: z.number().int().min(0).optional(),
  maxItems: z.number().int().min(0).optional(),
  uniqueItems: z.boolean().optional(),
  minContains: z.number().int().min(0).optional(),
  maxContains: z.number().int().min(0).optional(),

  // Object validations
  required: z.array(z.string()).optional(),
  minProperties: z.number().int().min(0).optional(),
  maxProperties: z.number().int().min(0).optional(),
  dependentRequired: z.record(z.string(), z.array(z.string())).optional(),

  // Value validations
  const: z.unknown().optional(),
  enum: z.array(z.unknown()).optional(),
});

// JSONSchema：baseSchema 推断类型 + 递归组合子 schema
/** 完整 JSON Schema 类型（boolean 或对象形态，含递归引用）。 @public */
export type JSONSchema =
  | boolean
  | (z.infer<typeof baseSchema> & {
      // Recursive properties
      $defs?: Record<string, JSONSchema>;
      contentSchema?: JSONSchema;
      items?: JSONSchema;
      prefixItems?: JSONSchema[];
      contains?: JSONSchema;
      unevaluatedItems?: JSONSchema;
      properties?: Record<string, JSONSchema>;
      patternProperties?: Record<string, JSONSchema>;
      additionalProperties?: JSONSchema | boolean;
      propertyNames?: JSONSchema;
      dependentSchemas?: Record<string, JSONSchema>;
      unevaluatedProperties?: JSONSchema;
      allOf?: JSONSchema[];
      anyOf?: JSONSchema[];
      oneOf?: JSONSchema[];
      not?: JSONSchema;
      if?: JSONSchema;
      then?: JSONSchema;
      else?: JSONSchema;
    });

// jsonSchemaType：lazy 递归 Zod，用于运行时校验完整 schema 树
/** 可递归校验 JSONSchema 的 Zod 联合类型（对象 | boolean）。 */
export const jsonSchemaType: z.ZodType<JSONSchema> = z.lazy(() =>
  z.union([
    baseSchema.extend({
      $defs: z.record(z.string(), jsonSchemaType).optional(),
      contentSchema: jsonSchemaType.optional(),
      items: jsonSchemaType.optional(),
      prefixItems: z.array(jsonSchemaType).optional(),
      contains: jsonSchemaType.optional(),
      unevaluatedItems: jsonSchemaType.optional(),
      properties: z.record(z.string(), jsonSchemaType).optional(),
      patternProperties: z.record(z.string(), jsonSchemaType).optional(),
      additionalProperties: z.union([jsonSchemaType, z.boolean()]).optional(),
      propertyNames: jsonSchemaType.optional(),
      dependentSchemas: z.record(z.string(), jsonSchemaType).optional(),
      unevaluatedProperties: jsonSchemaType.optional(),
      allOf: z.array(jsonSchemaType).optional(),
      anyOf: z.array(jsonSchemaType).optional(),
      oneOf: z.array(jsonSchemaType).optional(),
      not: jsonSchemaType.optional(),
      if: jsonSchemaType.optional(),
      // biome-ignore lint/suspicious/noThenProperty: This is a required property name in JSON Schema
      then: jsonSchemaType.optional(),
      else: jsonSchemaType.optional(),
    }),
    z.boolean(),
  ]),
);

// 从 simpleTypes 派生的 schema 类型字面量联合
/** JSON Schema 简单类型：string | number | integer | boolean | object | array | null。 */
export type SchemaType = (typeof simpleTypes)[number];

/** 可视化编辑器「新增字段」表单的输入结构。 */
export interface NewField {
  name: string;
  type: SchemaType;
  description: string;
  required: boolean;
  validation?: ObjectJSONSchema;
}

/** Schema 编辑器 React 状态：当前 schema、字段树与增删改回调。 */
export interface SchemaEditorState {
  schema: JSONSchema;
  fieldInfo: {
    type: SchemaType;
    properties: Array<{
      name: string;
      path: string[];
      schema: JSONSchema;
      required: boolean;
    }>;
  } | null;
  handleAddField: (newField: NewField, parentPath?: string[]) => void;
  handleEditField: (path: string[], updatedField: NewField) => void;
  handleDeleteField: (path: string[]) => void;
  handleSchemaEdit: (schema: JSONSchema) => void;
}

/** 非 boolean 形态的 JSON Schema 对象类型别名。 */
export type ObjectJSONSchema = Exclude<JSONSchema, boolean>;

/** 类型守卫：判断 schema 是否为 boolean 简写形式。 */
export function isBooleanSchema(schema: JSONSchema): schema is boolean {
  return typeof schema === 'boolean';
}

/** 类型守卫：schema 为对象形态（非 boolean）。 */
export function isObjectSchema(schema: JSONSchema): schema is ObjectJSONSchema {
  return !isBooleanSchema(schema);
}

/** boolean schema 转为 { type: 'null' }，否则原样返回。 */
export function asObjectSchema(schema: JSONSchema): ObjectJSONSchema {
  return isObjectSchema(schema) ? schema : { type: 'null' };
}
/** 安全读取 schema.description，boolean 时返回空串。 */
export function getSchemaDescription(schema: JSONSchema): string {
  return isObjectSchema(schema) ? schema.description || '' : '';
}

/** 仅对对象 schema 执行 fn，boolean 时返回 defaultValue。 */
export function withObjectSchema<T>(
  schema: JSONSchema,
  fn: (schema: ObjectJSONSchema) => T,
  defaultValue: T,
): T {
  return isObjectSchema(schema) ? fn(schema) : defaultValue;
}
