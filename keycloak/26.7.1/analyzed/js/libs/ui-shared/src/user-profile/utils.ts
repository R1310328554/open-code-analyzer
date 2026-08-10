import { UserProfileAttributeMetadata } from "@keycloak/keycloak-admin-client/lib/defs/userProfileMetadata";
import UserRepresentation from "@keycloak/keycloak-admin-client/lib/defs/userRepresentation";
import { TFunction } from "i18next";
import { FieldPath } from "react-hook-form";

/** 用户自定义属性键值对（表单中的 attributes 数组项） */
export type KeyValueType = { key: string; value: string };

/** 用户表单字段：在 UserRepresentation 基础上调整 attributes 与 metadata 形态 */
export type UserFormFields = Omit<
  UserRepresentation,
  "attributes" | "userProfileMetadata"
> & {
  attributes?: KeyValueType[] | Record<string, string | string[]>;
};

/** 服务端返回的单字段校验错误 */
type FieldError = {
  field: string;
  errorMessage: string;
  params?: unknown[];
};

/** 多字段错误数组包装 */
type ErrorArray = { errors?: FieldError[] };

/** User Profile API 校验失败时的错误结构 */
export type UserProfileError = {
  responseData: ErrorArray | FieldError;
};

/** 判断 displayName 是否为 i18n 资源 bundle 占位符（形如 `${key}`） */
export const isBundleKey = (displayName: unknown) => {
  return displayName && typeof displayName === "string"
    ? displayName.includes("${")
    : false;
};
/** 去掉 bundle 占位符外层的 `${` 与 `}` */
const unWrap = (key: string) => key.substring(2, key.length - 1);

/**
 * 解析字段标签：支持 i18n bundle key、fallback 与可选 prefix。
 * @param t i18next 翻译函数
 * @param text 原始 displayName 或 bundle key
 * @param fallback 无 text 时的回退文案
 * @param prefix 翻译 key 前缀（如 `profile`）
 */
export const label = (
  t: TFunction,
  text: string | undefined,
  fallback?: string,
  prefix?: string,
) => {
  const value = text || fallback;
  const bundleKey = isBundleKey(value) ? unWrap(value!) : value;
  const key = prefix ? `${prefix}.${bundleKey}` : bundleKey;
  return t(key || "");
};

/** 根据 User Profile 属性元数据生成本地化标签 */
export const labelAttribute = (
  t: TFunction,
  attribute: UserProfileAttributeMetadata,
) => label(t, attribute.displayName, attribute.name);

/** UserRepresentation 顶层字段，非 attributes 命名空间 */
const ROOT_ATTRIBUTES = ["username", "firstName", "lastName", "email"];

/** 判断属性名是否为顶层根字段（无需 `attributes.` 前缀） */
export const isRootAttribute = (attr?: string) =>
  attr && ROOT_ATTRIBUTES.includes(attr);

/**
 * 将 User Profile 属性名映射为 react-hook-form 字段路径。
 * 点号在路径中用 🍺 占位，避免嵌套路径歧义。
 */
export const fieldName = (name?: string) =>
  `${isRootAttribute(name) ? "" : "attributes."}${name?.replaceAll(
    ".",
    "🍺",
  )}` as FieldPath<UserFormFields>;

/** 属性名中的 `.` 替换为 🍺（beerify），供表单注册使用 */
export const beerify = <T extends string>(name: T) =>
  name.replaceAll(".", "🍺");

/** 将 🍺 还原为 `.`（debeerify），提交前恢复真实属性名 */
export const debeerify = <T extends string>(name: T) =>
  name.replaceAll("🍺", ".");

/**
 * 将 User Profile 服务端校验错误写入 react-hook-form 的 setError。
 * 支持单条与 errors 数组两种响应格式。
 */
export function setUserProfileServerError<T>(
  error: UserProfileError,
  setError: (field: keyof T, params: object) => void,
  t: TFunction,
) {
  (
    ((error.responseData as ErrorArray).errors !== undefined
      ? (error.responseData as ErrorArray).errors
      : [error.responseData]) as FieldError[]
  ).forEach((e) => {
    const params = Object.assign(
      {},
      e.params?.map((p) => (isBundleKey(p) ? t(unWrap(p as string)) : p)),
    );
    setError(fieldName(e.field) as keyof T, {
      message: t(
        isBundleKey(e.errorMessage) ? unWrap(e.errorMessage) : e.errorMessage,
        {
          /* eslint-disable @typescript-eslint/no-misused-spread */
          ...params,
          defaultValue: e.errorMessage || e.field,
        },
      ),
      type: "server",
    });
  });
}

/** 根据 User Profile 元数据判断属性是否必填 */
export function isRequiredAttribute({
  required,
}: UserProfileAttributeMetadata): boolean {
  // Check if required is true
  return required as boolean;
}

/** 类型守卫：判断 unknown 是否为 UserProfileError */
export function isUserProfileError(error: unknown): error is UserProfileError {
  // Check if the error is an object with a 'responseData' property.
  if (
    typeof error !== "object" ||
    error === null ||
    !("responseData" in error)
  ) {
    return false;
  }

  const { responseData } = error;

  if (isFieldError(responseData)) {
    return true;
  }

  // Check if 'responseData' is an object with an 'errors' property that is an array.
  if (
    typeof responseData !== "object" ||
    responseData === null ||
    !("errors" in responseData) ||
    !Array.isArray(responseData.errors)
  ) {
    return false;
  }

  // Check if all errors are field errors.
  return responseData.errors.every(isFieldError);
}

/** 类型守卫：判断是否为单字段 FieldError 结构 */
function isFieldError(error: unknown): error is FieldError {
  // Check if the error is an object.
  if (typeof error !== "object" || error === null) {
    return false;
  }

  // Check if the error object has a 'field' property that is a string.
  if (!("field" in error) || typeof error.field !== "string") {
    return false;
  }

  // Check if the error object has an 'errorMessage' property that is a string.
  if (!("errorMessage" in error) || typeof error.errorMessage !== "string") {
    return false;
  }

  return true;
}
