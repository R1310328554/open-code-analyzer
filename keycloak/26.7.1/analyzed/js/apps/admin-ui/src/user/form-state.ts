/** 用户表单与 Keycloak API 用户表示之间的双向转换逻辑。 */
import UserRepresentation from "@keycloak/keycloak-admin-client/lib/defs/userRepresentation";
import {
  KeyValueType,
  arrayToKeyValue,
  keyValueToArray,
} from "../components/key-value-form/key-value-convert";
import { beerify, debeerify } from "../util";

/** 用户编辑表单字段类型，属性以键值对形式供 UI 编辑。 */
export type UserFormFields = Omit<
  UIUserRepresentation,
  "attributes" | "userProfileMetadata" | "unmanagedAttributes"
> & {
  attributes?: KeyValueType[] | Record<string, string | string[]>;
  unmanagedAttributes?: KeyValueType[] | Record<string, string | string[]>;
};

/** 扩展 API 用户表示，保留未托管属性以便表单区分展示。 */
export interface UIUserRepresentation extends UserRepresentation {
  unmanagedAttributes?: Record<string, string[]>;
}

/** 将 API 用户对象转为表单可编辑结构（属性名 beerify、未托管属性转键值数组）。 */
export function toUserFormFields(data: UIUserRepresentation): UserFormFields {
  const attributes: Record<string, string | string[]> = {};
  Object.entries(data.attributes || {}).forEach(
    ([k, v]) => (attributes[beerify(k)] = v),
  );

  const unmanagedAttributes = arrayToKeyValue(data.unmanagedAttributes);
  return { ...data, attributes, unmanagedAttributes };
}

/** 将表单值还原为 API 用户表示，合并属性并校验托管/未托管不冲突。 */
export function toUserRepresentation(
  data: UserFormFields,
): UIUserRepresentation {
  const username = data.username?.trim();
  const attributes = Array.isArray(data.attributes)
    ? keyValueToArray(data.attributes)
    : Object.fromEntries(
        Object.entries(data.attributes || {}).map(([k, v]) => [
          debeerify(k),
          v,
        ]),
      );
  const unmanagedAttributes = Array.isArray(data.unmanagedAttributes)
    ? keyValueToArray(data.unmanagedAttributes)
    : data.unmanagedAttributes;

  // 未托管属性不得与 User Profile 已托管属性重名
  for (const key in unmanagedAttributes) {
    if (Object.hasOwn(attributes, key)) {
      throw Error(
        `Attribute ${key} is a managed attribute and is already available from the user details.`,
      );
    }
  }

  return {
    ...data,
    username,
    attributes: { ...unmanagedAttributes, ...attributes },
    unmanagedAttributes: undefined,
  };
}

/** 从属性集合中剔除已由 User Profile 未托管区管理的键。 */
export function filterManagedAttributes(
  attributes: Record<string, string[]> = {},
  unmanagedAttributes: Record<string, string[]> = {},
) {
  return Object.fromEntries(
    Object.entries(attributes).filter(
      ([key]) => !Object.hasOwn(unmanagedAttributes, key),
    ),
  );
}
