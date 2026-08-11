// paddleocr-js monorepo ESLint 扁平配置：区分 src 严格类型检查与测试/配置文件宽松规则
import eslint from "@eslint/js";
import tseslint from "typescript-eslint";
import globals from "globals";

  // 组合 ignores、recommended 与分文件的 TypeScript 规则集
export default tseslint.config(
  {
    ignores: ["**/dist", "**/node_modules", "**/coverage", "**/.cache"]
  },
  eslint.configs.recommended,
  {
    // 源码目录启用 strictTypeChecked 与 browser globals
    files: ["packages/**/src/**/*.ts", "apps/**/src/**/*.ts"],
    extends: [...tseslint.configs.strictTypeChecked],
    languageOptions: {
      globals: { ...globals.browser },
      parserOptions: {
        project: "./tsconfig.eslint.json",
        tsconfigRootDir: import.meta.dirname
      }
    }
  },
  {
    // 测试文件关闭 unsafe-* 等噪声规则
    files: ["packages/**/test/**/*.ts"],
    extends: [...tseslint.configs.recommendedTypeChecked],
    languageOptions: {
      globals: { ...globals.browser },
      parserOptions: {
        project: "./tsconfig.eslint.json",
        tsconfigRootDir: import.meta.dirname
      }
    },
    rules: {
      "@typescript-eslint/no-unsafe-assignment": "off",
      "@typescript-eslint/no-unsafe-argument": "off",
      "@typescript-eslint/no-unsafe-member-access": "off",
      "@typescript-eslint/no-unsafe-call": "off",
      "@typescript-eslint/no-unsafe-return": "off",
      "@typescript-eslint/no-explicit-any": "off",
      "@typescript-eslint/require-await": "off",
      "@typescript-eslint/no-extraneous-class": "off",
      "@typescript-eslint/unbound-method": "off"
    }
  },
  {
    // 配置脚本同时暴露 browser 与 node 全局变量
    files: ["apps/**/*.js", "*.config.{js,ts}", "packages/**/*.config.*"],
    languageOptions: {
      globals: { ...globals.browser, ...globals.node }
    }
  }
);
