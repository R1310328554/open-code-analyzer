/** util 模块表单转换与显示名解析的单元测试。 */
import { describe, expect, it } from "vitest";
import {
  convertAttributeNameToForm,
  convertFormValuesToObject,
  convertToFormValues,
  resolveDisplayName,
} from "./util";

/** 测试中 beerify 占位符，与 util 内 🍺 一致。 */
const TOKEN = "🍺";

describe("Tests the form convert util functions", () => {
  it("convert to form values", () => {
    const given = {
      name: "client",
      other: { one: "1", two: "2" },
      attributes: { one: ["1"] },
    };
    const values: { [index: string]: any } = {};
    const spy = (name: string, value: any) => (values[name] = value);

    // 执行：API 对象扁平化为 react-hook-form 字段
    convertToFormValues(given, spy);

    // 断言：attributes 转为键值数组，其余字段原样
    expect(values).toEqual({
      name: "client",
      other: { one: "1", two: "2" },
      attributes: [{ key: "one", value: "1" }],
    });
  });

  it("convert save values", () => {
    const given = {
      name: "client",
      attributes: [{ key: "one", value: "1" }],
      config: { [`one${TOKEN}two`]: "3" },
    };

    // 执行：表单提交值还原为 API 结构
    const values = convertFormValuesToObject(given);

    // 断言：beerify 键还原为带点路径
    expect(values).toEqual({
      name: "client",
      attributes: { one: ["1"] },
      config: { "one.two": "3" },
    });
  });

  it("convert attributes flatten", () => {
    const given = {
      name: "test",
      description: "",
      type: "default",
      attributes: {
        [`display${TOKEN}on${TOKEN}consent${TOKEN}screen`]: "true",
        [`include${TOKEN}in${TOKEN}token${TOKEN}scope`]: "true",
        [`gui${TOKEN}order`]: "1",
        [`consent${TOKEN}screen${TOKEN}text`]: "",
      },
    };

    // 执行：扁平 attributes 提交转换
    const values = convertFormValuesToObject(given);

    // 断言：嵌套属性名 debeerify 为服务端格式
    expect(values).toEqual({
      name: "test",
      description: "",
      type: "default",
      attributes: {
        "display.on.consent.screen": "true",
        "include.in.token.scope": "true",
        "gui.order": "1",
        "consent.screen.text": "",
      },
    });
  });

  it("convert flatten attributes to object", () => {
    const given = {
      attributes: {
        "display.on.consent.screen": "true",
        "include.in.token.scope": "true",
        "gui.order": "1",
        "consent.screen.text": "",
      },
    };
    const values: { [index: string]: any } = {};
    const spy = (name: string, value: any) => (values[name] = value);

    // 执行：带点属性名加载到表单
    convertToFormValues(given, spy);

    // 断言：生成 attributes.xxx 形式的 beerify 字段名
    expect(values).toEqual({
      [`attributes.display${TOKEN}on${TOKEN}consent${TOKEN}screen`]: "true",
      [`attributes.include${TOKEN}in${TOKEN}token${TOKEN}scope`]: "true",
      [`attributes.gui${TOKEN}order`]: "1",
      [`attributes.consent${TOKEN}screen${TOKEN}text`]: "",
    });
  });

  it("convert empty to empty object", () => {
    const given = { attributes: [{ key: "", value: "" }] };

    // 执行：空键值对应被丢弃
    const values = convertFormValuesToObject(given);

    // 断言：attributes 为空对象
    expect(values).toEqual({
      attributes: {},
    });
  });

  it("convert single element arrays to string", () => {
    const given = {
      config: {
        group: ["one"],
        "another.nested": ["value"],
      },
    };
    const values: { [index: string]: any } = {};
    const spy = (name: string, value: any) => (values[name] = value);

    // 执行：单元素数组在表单中展平为标量
    convertToFormValues(given, spy);

    // 断言
    expect(values).toEqual({
      "config.group": "one",
      [`config.another${TOKEN}nested`]: "value",
    });
  });

  it("should convert attribute name to form", () => {
    const given = "attributes.some.strange.attribute";

    // 执行：属性路径转表单字段名
    const form = convertAttributeNameToForm(given);

    // 断言：首段保留，后续段 beerify
    expect(form).toEqual(`attributes.some${TOKEN}strange${TOKEN}attribute`);
  });
});

/** 模拟 i18n，用于 resolveDisplayName 测试。 */
const mockT = ((key: string) => {
  const translations: Record<string, string> = {
    custom: "Custom Attribute",
    myRealmName: "My Translated Realm",
  };
  return translations[key] ?? key;
}) as any;

describe("resolveDisplayName", () => {
  it("returns plain display name without translating", () => {
    expect(resolveDisplayName(mockT, "My Realm", "fallback")).toBe("My Realm");
  });

  it("does not translate display name that matches a translation key", () => {
    expect(resolveDisplayName(mockT, "custom", "fallback")).toBe("custom");
  });

  it("translates display name when it is a bundle key", () => {
    expect(resolveDisplayName(mockT, "${myRealmName}", "fallback")).toBe(
      "My Translated Realm",
    );
  });

  it("returns fallback when display name is undefined", () => {
    expect(resolveDisplayName(mockT, undefined, "my-realm")).toBe("my-realm");
  });

  it("returns fallback when display name is empty", () => {
    expect(resolveDisplayName(mockT, "", "my-realm")).toBe("my-realm");
  });
});
