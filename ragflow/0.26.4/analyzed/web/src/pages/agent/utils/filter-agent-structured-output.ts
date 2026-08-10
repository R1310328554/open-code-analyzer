// filter-agent-structured-output.ts — Agent structured_output JSON Schema 子树类型探测。

import { JSONSchema } from '@/components/jsonjoy-builder';
import { getStructuredDatatype } from '@/utils/canvas-util';
import { get, isPlainObject, toLower } from 'lodash';
import { JsonSchemaDataType } from '../constant';

/** 判断 value 的 compositeDataType 是否在 types 列表中（忽略大小写）。 */
function predicate(types: string[], value: unknown) {
  return types.some(
    (x) =>
      toLower(x) === toLower(getStructuredDatatype(value).compositeDataType),
  );
}

/** 递归检查 schema 树是否含指定 compositeDataType 的子节点。 */
export function hasSpecificTypeChild(
  data: Record<string, any> | Array<any>,
  types: string[] = [],
) {
  if (Array.isArray(data)) {
    for (const value of data) {
      if (isPlainObject(value) && predicate(types, value)) {
        return true;
      }
      if (hasSpecificTypeChild(value, types)) {
        return true;
      }
    }
  }

  if (isPlainObject(data)) {
    for (const value of Object.values(data)) {
      if (
        isPlainObject(value) &&
        predicate(types, value) &&
        get(data, 'type') !== JsonSchemaDataType.Array
      ) {
        return true;
      }

      if (hasSpecificTypeChild(value, types)) {
        return true;
      }
    }
  }

  return false;
}

/** 判断 schema 是否含 Array 类型子字段。 */
export function hasArrayChild(data: Record<string, any> | Array<any>) {
  return hasSpecificTypeChild(data, [JsonSchemaDataType.Array]);
}

/** 判断 JSON Schema 是否定义非空 properties。 */
export function hasJsonSchemaChild(data: JSONSchema) {
  const properties = get(data, 'properties') ?? {};
  return isPlainObject(properties) && Object.keys(properties).length > 0;
}
