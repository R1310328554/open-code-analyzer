// schema-editor.ts — JSON Schema 对象/数组编辑辅助：属性增删改、字段校验与结构遍历。

import type {
  JSONSchema,
  NewField,
  ObjectJSONSchema,
} from '../types/json-schema';
import { isBooleanSchema, isObjectSchema } from '../types/json-schema';

/** 对象 schema 中的单个属性：名称、子 schema 与是否必填。 */
export type Property = {
  name: string;
  schema: JSONSchema;
  required: boolean;
};

/** 深拷贝 JSON Schema（优先 structuredClone，回退 JSON 序列化）。 */
export function copySchema<T extends JSONSchema>(schema: T): T {
  if (typeof structuredClone === 'function') return structuredClone(schema);
  return JSON.parse(JSON.stringify(schema));
}

/**
 * 在对象 schema 中更新指定属性的子 schema。
 */
export function updateObjectProperty(
  schema: ObjectJSONSchema,
  propertyName: string,
  propertySchema: JSONSchema,
): ObjectJSONSchema {
  if (!isObjectSchema(schema)) return schema;

  const newSchema = copySchema(schema);
  if (!newSchema.properties) {
    newSchema.properties = {};
  }

  newSchema.properties[propertyName] = propertySchema;
  return newSchema;
}

/**
 * 从对象 schema 中删除属性，并同步移除 required 列表中的同名项。
 */
export function removeObjectProperty(
  schema: ObjectJSONSchema,
  propertyName: string,
): ObjectJSONSchema {
  if (!isObjectSchema(schema) || !schema.properties) return schema;

  const newSchema = copySchema(schema);
  const { [propertyName]: _, ...remainingProps } = newSchema.properties;
  newSchema.properties = remainingProps;

  // Also remove from required array if present
  if (newSchema.required) {
    newSchema.required = newSchema.required.filter(
      (name) => name !== propertyName,
    );
  }

  return newSchema;
}

/**
 * 切换对象属性在 required 数组中的必填状态。
 */
export function updatePropertyRequired(
  schema: ObjectJSONSchema,
  propertyName: string,
  required: boolean,
): ObjectJSONSchema {
  if (!isObjectSchema(schema)) return schema;

  const newSchema = copySchema(schema);
  if (!newSchema.required) {
    newSchema.required = [];
  }

  if (required) {
    // Add to required array if not already there
    if (!newSchema.required.includes(propertyName)) {
      newSchema.required.push(propertyName);
    }
  } else {
    // Remove from required array
    newSchema.required = newSchema.required.filter(
      (name) => name !== propertyName,
    );
  }

  return newSchema;
}

/**
 * 更新数组 schema 的 items 子 schema。
 */
export function updateArrayItems(
  schema: JSONSchema,
  itemsSchema: JSONSchema,
): JSONSchema {
  if (isObjectSchema(schema) && schema.type === 'array') {
    return {
      ...schema,
      items: itemsSchema,
    };
  }
  return schema;
}

/**
 * 根据 NewField 表单数据生成对应类型的 JSON Schema 片段。
 */
export function createFieldSchema(field: NewField): JSONSchema {
  const { type, description, validation } = field;
  if (isObjectSchema(validation)) {
    return {
      type,
      description,
      ...validation,
    };
  }
  return validation;
}

/**
 * 校验字段名非空且符合 JavaScript 标识符命名规则。
 */
export function validateFieldName(name: string): boolean {
  if (!name || name.trim() === '') {
    return false;
  }

  // Check that the name doesn't contain invalid characters for property names
  const validNamePattern = /^[a-zA-Z_$][a-zA-Z0-9_$]*$/;
  return validNamePattern.test(name);
}

/**
 * 将对象 schema 的 properties 转为 Property 数组（含 required 标记）。
 */
export function getSchemaProperties(schema: JSONSchema): Property[] {
  if (!isObjectSchema(schema) || !schema.properties) return [];

  const required = schema.required || [];

  return Object.entries(schema.properties).map(([name, propSchema]) => ({
    name,
    schema: propSchema,
    required: required.includes(name),
  }));
}

/**
 * 从数组 schema 提取 items 子 schema，非数组则返回 null。
 */
export function getArrayItemsSchema(schema: JSONSchema): JSONSchema | null {
  if (isBooleanSchema(schema)) return null;
  if (schema.type !== 'array') return null;

  return schema.items || null;
}

/**
 * 判断 schema 是否包含可展开子节点（对象属性或数组内嵌对象）。
 */
export function hasChildren(schema: JSONSchema): boolean {
  if (!isObjectSchema(schema)) return false;

  if (schema.type === 'object' && schema.properties) {
    return Object.keys(schema.properties).length > 0;
  }

  if (schema.type === 'array' && schema.items && isObjectSchema(schema.items)) {
    return schema.items.type === 'object' && !!schema.items.properties;
  }

  return false;
}
