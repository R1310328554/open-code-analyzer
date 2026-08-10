// @ts-check
/** Keycloak JS monorepo 的 ESLint 扁平配置：TypeScript 严格检查、React/Playwright 与团队风格规则。 */
import { fixupPluginRules } from "@eslint/compat";
import { FlatCompat } from "@eslint/eslintrc";
import eslint from "@eslint/js";
import playwright from "eslint-plugin-playwright";
import prettierRecommended from "eslint-plugin-prettier/recommended";
import reactCompiler from "eslint-plugin-react-compiler";
import reactHooks from "eslint-plugin-react-hooks";
import reactJsxRuntime from "eslint-plugin-react/configs/jsx-runtime.js";
import reactRecommended from "eslint-plugin-react/configs/recommended.js";
import tseslint from "typescript-eslint";

/** 兼容旧 eslintrc 插件（如 lodash）的适配层 */
const compat = new FlatCompat({
  baseDirectory: import.meta.dirname,
});

// eslint-disable-next-line @typescript-eslint/no-deprecated -- defineConfig() not yet available in this version
export default tseslint.config(
  {
    ignores: [
      "**/dist/",
      "**/lib/",
      "**/target/",
      "./apps/keycloak-server/server/",
    ],
  },
  eslint.configs.recommended,
  ...tseslint.configs.strictTypeChecked,
  ...tseslint.configs.stylisticTypeChecked,
  reactRecommended,
  reactJsxRuntime,
  prettierRecommended,
  ...compat.plugins("lodash"),
  {
    plugins: {
      "react-hooks": fixupPluginRules(reactHooks),
      "react-compiler": reactCompiler,
    },
    languageOptions: {
      parserOptions: {
        project: "./tsconfig.eslint.json",
        tsconfigRootDir: import.meta.dirname,
      },
    },
    settings: {
      react: {
        version: "18",
      },
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      "react-compiler/react-compiler": "warn",
      // ## 覆盖默认配置的规则（暂关闭，后续需逐项评估） ##
      "no-undef": "off",
      "no-unused-private-class-members": "off",
      "@typescript-eslint/array-type": "off",
      "@typescript-eslint/ban-ts-comment": "off",
      "@typescript-eslint/ban-tslint-comment": "off",
      "@typescript-eslint/ban-types": "off",
      "@typescript-eslint/consistent-indexed-object-style": "off",
      "@typescript-eslint/consistent-type-definitions": "off",
      "@typescript-eslint/dot-notation": "off",
      "@typescript-eslint/no-base-to-string": "off",
      "@typescript-eslint/no-confusing-non-null-assertion": "off",
      "@typescript-eslint/no-confusing-void-expression": "off",
      "@typescript-eslint/no-duplicate-type-constituents": "off",
      "@typescript-eslint/no-dynamic-delete": "off",
      "@typescript-eslint/no-explicit-any": "off",
      "@typescript-eslint/no-extraneous-class": "off",
      "@typescript-eslint/no-inferrable-types": "off",
      "@typescript-eslint/no-invalid-void-type": "off",
      "@typescript-eslint/no-misused-promises": "off",
      "@typescript-eslint/no-non-null-asserted-optional-chain": "off",
      "@typescript-eslint/no-non-null-assertion": "off",
      "@typescript-eslint/no-redundant-type-constituents": "off",
      "@typescript-eslint/no-unnecessary-boolean-literal-compare": "off",
      "@typescript-eslint/no-unnecessary-condition": "error",
      "@typescript-eslint/no-unnecessary-type-arguments": "off",
      "@typescript-eslint/no-unnecessary-type-assertion": "off",
      "@typescript-eslint/no-unnecessary-type-parameters": "off",
      "@typescript-eslint/no-unsafe-argument": "off",
      "@typescript-eslint/no-unsafe-assignment": "off",
      "@typescript-eslint/no-unsafe-call": "off",
      "@typescript-eslint/no-unsafe-enum-comparison": "off",
      "@typescript-eslint/no-unsafe-member-access": "off",
      "@typescript-eslint/no-unsafe-return": "off",
      "@typescript-eslint/no-useless-constructor": "off",
      "@typescript-eslint/no-useless-template-literals": "off",
      "@typescript-eslint/non-nullable-type-assertion-style": "off",
      "@typescript-eslint/only-throw-error": "off",
      "@typescript-eslint/prefer-for-of": "off",
      "@typescript-eslint/prefer-nullish-coalescing": "off",
      "@typescript-eslint/prefer-promise-reject-errors": "off",
      "@typescript-eslint/prefer-reduce-type-parameter": "off",
      "@typescript-eslint/prefer-ts-expect-error": "off",
      "@typescript-eslint/require-await": "off",
      "@typescript-eslint/restrict-plus-operands": "off",
      "@typescript-eslint/restrict-template-expressions": "off",
      "@typescript-eslint/unbound-method": "off",
      "@typescript-eslint/use-unknown-in-catch-callback-variable": "off",
      // ## 按团队偏好或已知问题定制的规则 ##
      // 禁止 React 默认导入，统一使用命名导入以保持风格一致
      "no-restricted-imports": [
        "error",
        {
          paths: [
            {
              name: "react",
              importNames: ["default"],
            },
          ],
        },
      ],
      // 私有成员须使用 #private 语法，禁止 accessibility="private"
      "no-restricted-syntax": [
        "error",
        {
          selector:
            ':matches(PropertyDefinition, MethodDefinition)[accessibility="private"]',
          message: "Use #private instead",
        },
      ],
      // 回调优先使用箭头函数
      "prefer-arrow-callback": "error",
      // react/prop-types 无法处理泛型 props，故关闭
      // https://github.com/yannickcr/eslint-plugin-react/issues/2777#issuecomment-814968432
      "react/prop-types": "off",
      // 单子元素时禁止多余 Fragment
      "react/jsx-no-useless-fragment": "error",
      // 禁止在组件内嵌套定义子组件，避免非预期 remount
      // See: https://react.dev/learn/your-first-component#nesting-and-organizing-components
      "react/no-unstable-nested-components": ["error", { allowAsProps: true }],
      // lodash 须按成员路径导入（如 lodash/map），利于 tree-shaking
      "lodash/import-scope": ["error", "member"],
    },
  },
  {
    ...playwright.configs["flat/recommended"],
    files: ["apps/account-ui/test/**", "apps/admin-ui/test/**"],
  },
  {
    files: ["libs/keycloak-admin-client/test/**"],
    rules: {
      "@typescript-eslint/no-unused-expressions": "off",
    },
  },
  {
    files: ["libs/keycloak-admin-client/src/**"],
    rules: {
      // Admin Client 类型定义中常见空对象接口
      "@typescript-eslint/no-empty-object-type": "off",
    },
  },
);
