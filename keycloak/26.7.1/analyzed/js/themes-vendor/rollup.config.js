import { defineConfig } from "rollup";
import { nodeResolve } from "@rollup/plugin-node-resolve";
import commonjs from "@rollup/plugin-commonjs";
import replace from "@rollup/plugin-replace";
import terser from "@rollup/plugin-terser";
import path from "node:path";

/** 共用 Rollup 插件链：解析、CommonJS、生产环境替换与压缩 */
const plugins = [
  nodeResolve(),
  commonjs({
    strictRequires: "auto",
  }),
  replace({
    preventAssignment: true,
    // React 依赖 process.env.NODE_ENV 区分开发/生产分支；固定为 production 以剔除开发代码
    "process.env.NODE_ENV": '"production"',
  }),
  terser(),
];

/** 主题 vendor 资源输出根目录（Maven target/classes 下） */
const targetDir = "target/classes/theme/keycloak/common/resources/vendor";

/** @type{import("rollup").WarningHandlerWithDefault} */
function onwarn(warning, defaultHandler) {
  // 未解析的 import 视为构建错误，避免静默产出残缺 bundle
  if (warning.code === "UNRESOLVED_IMPORT") {
    throw new Error(`Unresolved import: ${warning.exporter}`);
  }

  defaultHandler(warning);
}

/** 将 React、ReactDOM 与 Web Crypto shim 分别打包到主题 vendor 目录 */
export default defineConfig([
  {
    input: [
      "node_modules/react/cjs/react.production.min.js",
      "node_modules/react/cjs/react-jsx-runtime.production.min.js",
    ],
    output: {
      dir: path.join(targetDir, "react"),
      format: "es",
    },
    plugins,
    onwarn,
  },
  {
    input: "node_modules/react-dom/cjs/react-dom.production.min.js",
    output: {
      dir: path.join(targetDir, "react-dom"),
      format: "es",
    },
    external: ["react"],
    plugins,
    onwarn,
  },
  {
    input: "src/main/js/web-crypto-shim.js",
    output: {
      dir: path.join(targetDir, "web-crypto-shim"),
      format: "es",
    },
    plugins,
    onwarn,
  },
]);
