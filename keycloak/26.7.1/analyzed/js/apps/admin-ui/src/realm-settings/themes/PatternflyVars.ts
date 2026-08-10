/**
 * Patternfly 5 主题变量定义与解析工具。
 * 将 Admin Console 可配置的主题色映射为 PF CSS 变量，并支持明暗主题与 {{color}} 依赖推导。
 */

// variable 为去掉 --pf-v5-global-- 前缀后的 PF5 变量名
// 依赖项使用 {{color}} 占位符，运行时替换为父级颜色的当前值
/** 支持明暗双模式的色值定义。 */
type Value = { light?: string; dark?: string };
/** 变量默认值：纯色字符串或按主题区分的对象。 */
export type DefaultValueType = string | Value;

/** 依赖父级主题色的衍生变量（如 primary 的 hover 态）。 */
type DependencyVariable = {
  name: string;
  defaultValue: DefaultValueType;
  variable: string | Value;
};

/** 展平后用于 UI 绑定的依赖变量（variable 已解析为单一字符串）。 */
type FlattenedDependencyVariable = {
  name: string;
  defaultValue: DefaultValueType;
  variable: string;
};

/** 顶层或可嵌套依赖的主题变量完整定义。 */
type VariableDefinition = {
  name: string;
  defaultValue: string | Value;
  variable: string | Value;
  dependencies?: DependencyVariable[];
};

/** 内置主题调色板：字体、语义色、背景与文本等 Patternfly 全局变量。 */
const variables: VariableDefinition[] = [
  {
    name: "font",
    defaultValue: '"RedHatText", helvetica, arial, sans-serif',
    variable: "FontFamily--text",
  },
  {
    name: "errorColor",
    defaultValue: { light: "#c9190b", dark: "#fe5142" },
    variable: "danger-color--100",
  },
  {
    name: "successColor",
    defaultValue: { light: "#3e8635", dark: "#5ba352" },
    variable: "success-color--100",
  },
  {
    name: "primaryColor",
    defaultValue: "#0066cc",
    variable: { light: "primary-color--100", dark: "primary-color--300" },
    dependencies: [
      {
        name: "primaryColorHover",
        defaultValue: "color-mix(in srgb, {{color}} 63%, black)",
        variable: "primary-color--200",
      },
      {
        name: "activeColor",
        defaultValue: {
          light: "{{color}}",
          dark: "color-mix(in srgb, {{color}} 78%, white)",
        },
        variable: "active-color--100",
      },
      // TODO in patternfly atm secondary buttons don't have a secondaryColor
      // see: https://github.com/patternfly/patternfly-react/issues/12238
      // {
      //   name: "secondaryColor",
      //   defaultValue: {
      //     light: "{{color}}",
      //     dark: "color-mix(in srgb, {{color}} 78%, white)",
      //   },
      //   variable: "primary-color--100",
      // },
    ],
  },
  {
    name: "linkColor",
    defaultValue: { light: "#0066cc", dark: "#1fa7f8" },
    variable: "link--Color",
    dependencies: [
      {
        name: "linkColorHover",
        defaultValue: {
          light: "color-mix(in srgb, {{color}} 63%, black)",
          dark: "color-mix(in srgb, {{color}} 63%, white)",
        },
        variable: "link--Color--hover",
      },
    ],
  },
  {
    name: "backgroundColor",
    defaultValue: { light: "#ffffff", dark: "#1b1d21" },
    variable: "BackgroundColor--light-100",
    dependencies: [
      {
        name: "backgroundColorAccent",
        defaultValue: "color-mix(in srgb, {{color}} 95%, white)",
        variable: { dark: "BackgroundColor--300" },
      },
      {
        name: "backgroundColorNav",
        defaultValue: {
          light: "color-mix(in srgb, {{color}} 14%, black)",
          dark: "color-mix(in srgb, {{color}} 99%, black)",
        },
        variable: {
          light: "BackgroundColor--dark-300",
          dark: "BackgroundColor--100",
        },
      },
      {
        name: "backgroundColorHeader",
        defaultValue: {
          light: "color-mix(in srgb, {{color}} 8%, black)",
          dark: "color-mix(in srgb, {{color}} 10%, black)",
        },
        variable: {
          light: "BackgroundColor--dark-100",
          dark: "palette--black-1000",
        },
      },
    ],
  },
  { name: "iconColor", defaultValue: "#f0f0f0", variable: "Color--light-200" },
  {
    name: "textColor",
    defaultValue: { light: "#151515", dark: "#e0e0e0" },
    variable: "Color--100",
    dependencies: [
      {
        name: "lightTextColor",
        defaultValue: { light: "#ffffff", dark: "{{color}}" },
        variable: "Color--light-100",
      },
      {
        name: "inputTextColor",
        defaultValue: { light: "{{color}}", dark: "{{color}}" },
        variable: "Color--dark-100",
      },
    ],
  },
  {
    name: "inputBackgroundColor",
    defaultValue: "#36373a",
    variable: { dark: "BackgroundColor--400" },
  },
];

/** 当前解析的主题维度：明色或暗色。 */
type ThemeType = keyof Value;

/** 展平后的变量条目，供主题编辑器表单绑定；dependencies 保留直接子依赖。 */
export type FlattenedVariable = Omit<VariableDefinition, "dependencies"> & {
  parentName?: string;
  dependencies?: FlattenedDependencyVariable[];
};

/** 将 string | Value 解析为指定主题下的单一字符串值。 */
const convert = (v: string | Value | undefined, theme: ThemeType) =>
  typeof v === "string" ? v : v?.[theme];

/** 按主题展平变量树：跳过无对应 CSS 变量名的项，并将依赖项追加为独立条目。 */
const flattenVariables = (theme: ThemeType): FlattenedVariable[] => {
  const result: FlattenedVariable[] = [];

  variables.forEach((v) => {
    const defaultValue = convert(v.defaultValue, theme);
    const variable = convert(v.variable, theme);

    // 当前主题下无 CSS 变量名则跳过（仅适用于另一主题的条目）
    if (variable === undefined) return;

    const flattenedVar: FlattenedVariable = {
      name: v.name,
      defaultValue: defaultValue!,
      variable: variable!,
    };

    if (v.dependencies && v.dependencies.length > 0) {
      flattenedVar.dependencies = v.dependencies
        .map((dep) => {
          const depVariable = convert(dep.variable, theme);
          if (!depVariable) return null;
          return {
            name: dep.name,
            variable: depVariable,
            defaultValue: dep.defaultValue,
          };
        })
        .filter((dep): dep is FlattenedDependencyVariable => dep !== null);
    }

    result.push(flattenedVar);

    if (v.dependencies) {
      v.dependencies.forEach((dep) => {
        const depVariable = convert(dep.variable, theme);
        if (!depVariable) return;
        result.push({
          name: dep.name,
          defaultValue: dep.defaultValue,
          variable: depVariable,
          parentName: v.name,
        });
      });
    }
  });

  return result;
};

/** 返回明色主题下的展平变量列表。 */
export const lightTheme = (): FlattenedVariable[] => flattenVariables("light");

/** 返回暗色主题下的展平变量列表。 */
export const darkTheme = (): FlattenedVariable[] => flattenVariables("dark");

/**
 * 将任意 CSS 颜色表达式解析为 #rrggbb 十六进制字符串。
 * 支持已是 hex、color(srgb ...) 以及 rgb/rgba 计算结果。
 */
export function resolveColorToHex(colorValue: string) {
  // 已是合法 hex 则直接归一化返回
  if (/^#[0-9a-fA-F]{6}$/i.test(colorValue)) {
    return colorValue.toLowerCase();
  }

  const el = document.createElement("div");
  el.style.cssText = `position:absolute;left:-9999px;color:${colorValue}`;
  document.body.appendChild(el);
  const computed = getComputedStyle(el).color;
  el.remove();

  let r = 0,
    g = 0,
    b = 0;

  // 解析 color(srgb 0 0.252 0.504) 格式（分量 0–1）
  let matches = /color\(srgb\s+([\d.]+)\s+([\d.]+)\s+([\d.]+)/.exec(
    computed || "",
  );
  if (matches) {
    [, r, g, b] = matches.map(Number);
    r = Math.round(r * 255);
    g = Math.round(g * 255);
    b = Math.round(b * 255);
  } else {
    // 解析 rgb(r, g, b) 或 rgba(r, g, b, a) 格式
    matches = /rgba?\(\s*(\d+),\s*(\d+),\s*(\d+)/.exec(computed || "");
    if (matches) {
      [, r, g, b] = matches.map(Number);
    }
  }
  return "#" + [r, g, b].map((x) => x.toString(16).padStart(2, "0")).join("");
}

/**
 * 将默认值中的 {{color}} 占位符替换为父级颜色，并按主题选取 light/dark 分支。
 */
export function resolveColorReferences(
  colorValue: DefaultValueType,
  parentValue: string,
  theme: ThemeType,
): string {
  return convert(colorValue, theme)!.replace(/\{\{color\}\}/g, parentValue);
}
