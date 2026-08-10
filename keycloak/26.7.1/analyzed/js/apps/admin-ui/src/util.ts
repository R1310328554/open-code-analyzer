/** 管理控制台通用工具：表单转换、导出、表格格式化与显示名解析等。 */
import type ClientRepresentation from "@keycloak/keycloak-admin-client/lib/defs/clientRepresentation";
import type { ProviderRepresentation } from "@keycloak/keycloak-admin-client/lib/defs/serverInfoRepesentation";
import { isBundleKey, label } from "@keycloak/keycloak-ui-shared";
import type { IFormatter, IFormatterValueType } from "@patternfly/react-table";
import { saveAs } from "file-saver";
import { flatten } from "flat";
import { TFunction } from "i18next";
import { cloneDeep } from "lodash-es";
import { FieldValues, Path, PathValue, UseFormSetValue } from "react-hook-form";
import {
  KeyValueType,
  arrayToKeyValue,
  keyValueToArray,
} from "./components/key-value-form/key-value-convert";
import { ReplaceString } from "./utils/types";

/** 按 order 降序、同序按 id 字典序排列 Provider id 列表。 */
export const sortProviders = (providers: {
  [index: string]: ProviderRepresentation;
}) => {
  return [...new Map(Object.entries(providers).sort(sortProvider)).keys()];
};

/** Provider 条目比较器：优先 order，其次键名字符串序。 */
const sortProvider = (
  a: [string, ProviderRepresentation],
  b: [string, ProviderRepresentation],
) => {
  let s1, s2;
  if (a[1].order !== b[1].order) {
    s1 = b[1].order;
    s2 = a[1].order;
  } else {
    s1 = a[0];
    s2 = b[0];
  }
  if (s1 < s2) {
    return -1;
  } else if (s1 > s2) {
    return 1;
  } else {
    return 0;
  }
};

/** 将字符串中的空格替换为连字符，用作表单键等。 */
export const toKey = (value: string) => value.replace(/\s/g, "-");

/** 导出客户端 JSON 文件（去除 id 与 mapper id 以便复用配置）。 */
export const exportClient = (client: ClientRepresentation): void => {
  const clientCopy = cloneDeep(client);
  delete clientCopy.id;

  if (clientCopy.protocolMappers) {
    for (let i = 0; i < clientCopy.protocolMappers.length; i++) {
      delete clientCopy.protocolMappers[i].id;
    }
  }

  saveAs(
    new Blob([prettyPrintJSON(clientCopy)], {
      type: "application/json",
    }),
    clientCopy.clientId + ".json",
  );
};

/** 字符串首字母大写（TypeScript 字面量类型保留）。 */
export const toUpperCase = <T extends string>(name: T) =>
  (name.charAt(0).toUpperCase() + name.slice(1)) as Capitalize<T>;

/** 对象是否像「属性 map」：至少有一个非空数组值。 */
const isAttributesObject = (value: any) =>
  Object.values(value).filter(
    (value) => Array.isArray(value) && value.length >= 1,
  ).length !== 0;

/** 值是否为键值对数组（含 key/value 字段）。 */
const isAttributeArray = (value: any) => {
  if (!Array.isArray(value)) {
    return false;
  }

  return value.some(
    (e) => Object.hasOwn(e, "key") && Object.hasOwn(e, "value"),
  );
};

/** 对象是否无任何自有键。 */
const isEmpty = (obj: any) => Object.keys(obj).length === 0;

/** 将带点属性路径转为表单字段名（首段后 beerify）。 */
export function convertAttributeNameToForm<T>(
  name: string,
): PathValue<T, Path<T>> {
  const index = name.indexOf(".");
  return `${name.substring(0, index)}.${beerify(name.substring(index + 1))}` as PathValue<
    T,
    Path<T>
  >;
}

/** 将路径中的 `.` 替换为 🍺，避免 react-hook-form 嵌套路径冲突。 */
export const beerify = <T extends string>(name: T) =>
  name.replaceAll(".", "🍺") as ReplaceString<T, ".", "🍺">;

/** beerify 的逆操作，提交前还原真实属性名。 */
export const debeerify = <T extends string>(name: T) =>
  name.replaceAll("🍺", ".") as ReplaceString<T, "🍺", ".">;

/** 将 API/领域对象写入 react-hook-form（扁平化 config/attributes）。 */
export function convertToFormValues<T extends FieldValues>(
  obj: FieldValues,
  setValue: UseFormSetValue<T>,
) {
  Object.entries(obj).map((entry) => {
    const [key, value] = entry as [Path<T>, any];
    if (key === "attributes" && isAttributesObject(value)) {
      setValue(key, arrayToKeyValue(value as Record<string, string[]>));
    } else if (key === "config" || key === "attributes") {
      if (!isEmpty(value)) {
        const flattened: any = flatten(value, { safe: true });
        const convertedValues = Object.entries(flattened).map(([key, value]) =>
          Array.isArray(value) && value.length === 1
            ? [key, value[0]]
            : [key, value],
        );

        convertedValues.forEach(([k, v]) =>
          setValue(`${key}.${beerify(k)}` as Path<T>, v),
        );
      } else {
        setValue(key, undefined as PathValue<T, Path<T>>);
      }
    } else {
      setValue(key, value);
    }
  });
}

/** 将表单值还原为 API 可接受的对象（debeerify、键值数组转 map）。 */
export function convertFormValuesToObject<T extends Record<string, any>, G = T>(
  obj: T,
): G {
  const result: any = {};
  Object.entries(obj).map(([key, value]) => {
    if (isAttributeArray(value)) {
      result[key] = keyValueToArray(value as KeyValueType[]);
    } else if (key === "config" || key === "attributes") {
      result[key] = Object.fromEntries(
        Object.entries(
          (value as Record<string, unknown> | undefined) || {},
        ).map(([k, v]) => [debeerify(k), v]),
      );
    } else {
      result[key] = value;
    }
  });
  return result;
}

/** PatternFly 表格：空单元格显示 em dash。 */
export const emptyFormatter =
  (): IFormatter => (data?: IFormatterValueType) => {
    return data ? data : "—";
  };

/** PatternFly 表格：整串转为首字母大写。 */
export const upperCaseFormatter =
  (): IFormatter => (data?: IFormatterValueType) => {
    const value = data?.toString();

    return (value ? toUpperCase(value) : undefined) as string;
  };

/** PatternFly 表格：首字母大写、其余小写。 */
export const capitalizeFirstLetterFormatter =
  (): IFormatter => (data?: IFormatterValueType) => {
    const value = data?.toString();

    return (
      value
        ? value.charAt(0).toUpperCase() + value.slice(1).toLowerCase()
        : undefined
    ) as string;
  };

/** 仅保留拉丁字母的正则（用于用户名等校验）。 */
export const alphaRegexPattern = /[^A-Za-z]/g;

/** 常见邮箱格式校验正则。 */
export const emailRegexPattern =
  /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

/** Key Provider SPI 的 Java 类型标识。 */
export const KEY_PROVIDER_TYPE = "org.keycloak.keys.KeyProvider";

/** JSON 美化输出（两空格缩进）。 */
export const prettyPrintJSON = (value: any) => JSON.stringify(value, null, 2);

/**
 * 解析显示名：bundle 键 `${key}` 走 i18n，否则原样或 fallback。
 */
export const resolveDisplayName = (
  t: TFunction,
  displayName?: string,
  fallback = "",
) => {
  if (displayName && isBundleKey(displayName)) {
    return label(t, displayName);
  }
  return displayName || fallback;
};

/** URL 末尾保证有且仅有一个 `/`。 */
export const addTrailingSlash = (url: string) =>
  url.endsWith("/") ? url : url + "/";

/** 将 locale 代码转为指定展示语言下的可读语言名。 */
export const localeToDisplayName = (locale: string, displayLocale: string) => {
  try {
    return new Intl.DisplayNames([displayLocale], { type: "language" }).of(
      // 旧版 zh-CN/zh-TW 映射到新 BCP47 标签；存量 locale 迁移后可删除
      locale === "zh-CN" ? "zh-HANS" : locale === "zh-TW" ? "zh-HANT" : locale,
    );
  } catch {
    return locale;
  }
};
