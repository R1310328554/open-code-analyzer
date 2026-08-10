// validation.ts — JSON Schema 按类型的 Zod 校验、错误树构建与 i18n 错误消息。

import z from 'zod';
import type { Translation } from '../i18n/translation-keys';
import { baseSchema, type JSONSchema } from './json-schema';

/** 校验 min/max（含 exclusive）区间是否一致且合法。 */
function refineRangeConsistency(
  min: number | undefined,
  isMinExclusive: boolean,
  max: number | undefined,
  isMaxExclusive: boolean,
): boolean {
  if (min !== undefined && max !== undefined && min > max) {
    return false;
  }
  if (isMinExclusive && isMaxExclusive && max - min < 2) {
    return false;
  }
  if ((isMinExclusive || isMaxExclusive) && max - min < 1) {
    return false;
  }
  return true;
}

/** 构建 string 类型约束的 Zod schema（长度、pattern、format、enum）。 */
const getJsonStringType = (t: Translation) =>
  z
    .object({
      minLength: z
        .number()
        .int({ message: t.typeValidationErrorIntValue })
        .min(0, { message: t.typeValidationErrorNegativeLength })
        .optional(),
      maxLength: z
        .number()
        .int({ message: t.typeValidationErrorIntValue })
        .min(0, { message: t.typeValidationErrorNegativeLength })
        .optional(),
      pattern: baseSchema.shape.pattern,
      format: baseSchema.shape.format,
      enum: baseSchema.shape.enum,
      contentMediaType: baseSchema.shape.contentMediaType, // TODO
      contentEncoding: baseSchema.shape.contentEncoding, // TODO
    })
    // If minLength and maxLength are both set, minLength must not be greater than maxLength.
    .refine(
      ({ minLength, maxLength }) =>
        refineRangeConsistency(minLength, false, maxLength, false),
      {
        message: t.stringValidationErrorLengthRange,
        path: ['length'],
      },
    );

/** 构建 number 类型约束的 Zod schema（范围、exclusive、enum 范围校验）。 */
const getJsonNumberType = (t: Translation) =>
  z
    .object({
      multipleOf: z
        .number()
        .positive({ message: t.typeValidationErrorPositive })
        .optional(),
      minimum: baseSchema.shape.minimum,
      maximum: baseSchema.shape.maximum,
      exclusiveMinimum: baseSchema.shape.exclusiveMinimum,
      exclusiveMaximum: baseSchema.shape.exclusiveMaximum,
      enum: baseSchema.shape.enum,
    })
    // If both minimum (or exclusiveMinimum) and maximum (or exclusiveMaximum) are set, minimum must not be greater than maximum.
    .refine(
      ({ minimum, exclusiveMinimum, maximum, exclusiveMaximum }) =>
        refineRangeConsistency(minimum, false, maximum, false) &&
        refineRangeConsistency(minimum, false, exclusiveMaximum, true) &&
        refineRangeConsistency(exclusiveMinimum, true, maximum, false) &&
        refineRangeConsistency(exclusiveMinimum, true, exclusiveMaximum, true),
      {
        message: t.numberValidationErrorMinMax,
        path: ['minMax'],
      },
    )
    // cannot set both exclusiveMinimum and minimum
    .refine(
      ({ minimum, exclusiveMinimum }) =>
        exclusiveMinimum === undefined || minimum === undefined,
      {
        message: t.numberValidationErrorBothExclusiveAndInclusiveMin,
        path: ['redundantMinimum'],
      },
    )
    // cannot set both exclusiveMaximum and maximum
    .refine(
      ({ maximum, exclusiveMaximum }) =>
        exclusiveMaximum === undefined || maximum === undefined,
      {
        message: t.numberValidationErrorBothExclusiveAndInclusiveMax,
        path: ['redundantMaximum'],
      },
    )
    // check that the enums are within min/max if they are set
    .refine(
      ({
        enum: enumValues,
        minimum,
        maximum,
        exclusiveMinimum,
        exclusiveMaximum,
      }) => {
        if (!enumValues || enumValues.length === 0) return true;
        return enumValues.every((val) => {
          if (typeof val !== 'number') return false;
          if (minimum !== undefined && val < minimum) return false;
          if (maximum !== undefined && val > maximum) return false;
          if (exclusiveMinimum !== undefined && val <= exclusiveMinimum)
            return false;
          if (exclusiveMaximum !== undefined && val >= exclusiveMaximum)
            return false;
          return true;
        });
      },
      {
        message: t.numberValidationErrorEnumOutOfRange,
        path: ['enum'],
      },
    );

/** 构建 array 类型约束的 Zod schema（minItems/maxItems、contains）。 */
const getJsonArrayType = (t: Translation) =>
  z
    .object({
      minItems: z
        .number()
        .int({ message: t.typeValidationErrorIntValue })
        .min(0, { message: t.typeValidationErrorNegativeLength })
        .optional(),
      maxItems: z
        .number()
        .int({ message: t.typeValidationErrorIntValue })
        .min(0, { message: t.typeValidationErrorNegativeLength })
        .optional(),
      uniqueItems: z.boolean().optional(),
      minContains: z
        .number()
        .int({ message: t.typeValidationErrorIntValue })
        .min(0, { message: t.typeValidationErrorNegativeLength })
        .optional(),
      maxContains: z
        .number()
        .int({ message: t.typeValidationErrorIntValue })
        .min(0, { message: t.typeValidationErrorNegativeLength })
        .optional(),
    })
    // If both minItems and maxItems are set, minItems must not be greater than maxItems.
    .refine(
      ({ minItems, maxItems }) =>
        refineRangeConsistency(minItems, false, maxItems, false),
      {
        message: t.arrayValidationErrorMinMax,
        path: ['minmax'],
      },
    )
    // If both minContains and maxContains are set, minContains must not be greater than maxContains.
    .refine(
      ({ minContains, maxContains }) =>
        refineRangeConsistency(minContains, false, maxContains, false),
      {
        message: t.arrayValidationErrorContainsMinMax,
        path: ['minmaxContains'],
      },
    );

/** 构建 object 类型约束的 Zod schema（minProperties/maxProperties）。 */
const getJsonObjectType = (t: Translation) =>
  z
    .object({
      minProperties: z
        .number()
        .int({ message: t.typeValidationErrorIntValue })
        .min(0, { message: t.typeValidationErrorNegativeLength })
        .optional(),
      maxProperties: z
        .number()
        .int({ message: t.typeValidationErrorIntValue })
        .min(0, { message: t.typeValidationErrorNegativeLength })
        .optional(),
    })
    // If both minProperties and maxProperties are set, minProperties must not be greater than maxProperties.
    .refine(
      ({ minProperties, maxProperties }) =>
        refineRangeConsistency(minProperties, false, maxProperties, false),
      {
        message: t.objectValidationErrorMinMax,
        path: ['minmax'],
      },
    );

/** 按 JSON Schema type 字符串返回对应 Zod 校验器，未知类型为 z.any()。 */
export function getTypeValidation(type: string, t: Translation) {
  const jsonTypesValidation: Record<string, z.ZodTypeAny> = {
    string: getJsonStringType(t),
    number: getJsonNumberType(t),
    array: getJsonArrayType(t),
    object: getJsonObjectType(t),
  };

  return jsonTypesValidation[type] || z.any();
}

/** 单节点 schema 类型校验结果：success 与 Zod issues。 */
export interface TypeValidationResult {
  success: boolean;
  errors?: z.core.$ZodIssue[];
}

/** 对 schema 片段按 type 执行 Zod safeParse 校验。 */
export function validateSchemaByType(
  schema: unknown,
  type: string,
  t: Translation,
): TypeValidationResult {
  const zodSchema = getTypeValidation(type, t);
  const result = zodSchema.safeParse(schema);
  if (result.success) {
    return { success: true };
  } else {
    return { success: false, errors: result.error.issues };
  }
}

/** 校验树节点：属性名、本节点校验、子节点及累计错误数。 */
export interface ValidationTreeNode {
  name: string;
  validation: TypeValidationResult;
  children: Record<string, ValidationTreeNode>;
  cumulativeChildrenErrors: number; // Total errors in this node and all its descendants
}

/** 递归遍历 schema 树，为各节点构建 ValidationTreeNode 并汇总错误计数。 */
export function buildValidationTree(
  schema: JSONSchema,
  t: Translation,
): ValidationTreeNode {
  // Helper to determine a concrete type string from a schema.type which may be string | string[] | undefined
  const deriveType = (sch: unknown): string | undefined => {
    if (!sch || typeof sch !== 'object') return undefined;
    const declared = (sch as Record<string, unknown>).type;
    if (typeof declared === 'string') return declared;
    if (
      Array.isArray(declared) &&
      declared.length > 0 &&
      typeof declared[0] === 'string'
    )
      return declared[0];
    return undefined;
  };

  // TODO confirm assumption below:
  // Handle boolean schemas: true => always valid, false => always invalid
  if (typeof schema === 'boolean') {
    const validation: TypeValidationResult =
      schema === true
        ? { success: true }
        : {
            success: false,
            errors: [
              {
                code: 'custom',
                message: t.validatorErrorSchemaValidation,
                path: [],
              } as unknown as z.core.$ZodIssue,
            ],
          };

    const node: ValidationTreeNode = {
      name: String(schema),
      validation,
      children: {},
      cumulativeChildrenErrors: validation.success
        ? 0
        : validation.errors?.length ?? 0,
    };

    return node;
  }

  // schema is an object-shaped JSONSchema
  const sch = schema as Record<string, unknown>;
  const currentType = deriveType(sch);

  const validation = validateSchemaByType(schema, currentType, t);

  const children: Record<string, ValidationTreeNode> = {};

  // Traverse object properties
  if (currentType === 'object') {
    const properties = sch.properties;
    if (properties && typeof properties === 'object') {
      for (const [propName, propSchema] of Object.entries(
        properties as Record<string, JSONSchema>,
      )) {
        children[propName] = buildValidationTree(propSchema, t);
      }
    }
    // handle dependentSchemas, patternProperties etc. if present (shallow support)
    if (sch.patternProperties && typeof sch.patternProperties === 'object') {
      for (const [patternName, patternSchema] of Object.entries(
        sch.patternProperties as Record<string, JSONSchema>,
      )) {
        children[`pattern:${patternName}`] = buildValidationTree(
          patternSchema,
          t,
        );
      }
    }
  }

  // Traverse array items / prefixItems
  if (currentType === 'array') {
    const items = sch.items;
    if (Array.isArray(items)) {
      items.forEach((it, idx) => {
        children[`items[${idx}]`] = buildValidationTree(it, t);
      });
    } else if (items) {
      children.items = buildValidationTree(items as JSONSchema, t);
    }

    if (Array.isArray(sch.prefixItems)) {
      (sch.prefixItems as JSONSchema[]).forEach((it, idx) => {
        children[`prefixItems[${idx}]`] = buildValidationTree(it, t);
      });
    }
  }

  // Handle combinators: allOf / anyOf / oneOf / not (shallow traversal)
  const combinators: Array<'allOf' | 'anyOf' | 'oneOf'> = [
    'allOf',
    'anyOf',
    'oneOf',
  ];
  for (const comb of combinators) {
    const arr = sch[comb];
    if (Array.isArray(arr)) {
      arr.forEach((subSchema, idx) => {
        children[[comb, idx].join(':')] = buildValidationTree(
          subSchema as JSONSchema,
          t,
        );
      });
    }
  }

  if (sch.not) {
    children.not = buildValidationTree(sch.not as JSONSchema, t);
  }

  // $defs / definitions / dependentSchemas (shallow)
  if (sch.$defs && typeof sch.$defs === 'object') {
    for (const [defName, defSchema] of Object.entries(
      sch.$defs as Record<string, JSONSchema>,
    )) {
      children[`$defs:${defName}`] = buildValidationTree(defSchema, t);
    }
  }

  // definitions is the older name for $defs, so we support both
  const definitions = (sch as Record<string, unknown>).definitions;
  if (definitions && typeof definitions === 'object') {
    for (const [defName, defSchema] of Object.entries(
      definitions as Record<string, JSONSchema>,
    )) {
      children[`definitions:${defName}`] = buildValidationTree(defSchema, t);
    }
  }

  // Compute cumulative error counts (own + all descendants)
  const ownErrors = validation.success ? 0 : validation.errors?.length ?? 0;
  const childrenErrors = Object.values(children).reduce(
    (sum, child) => sum + child.cumulativeChildrenErrors,
    0,
  );

  return {
    name: currentType,
    validation,
    children,
    cumulativeChildrenErrors: ownErrors + childrenErrors,
  };
}
