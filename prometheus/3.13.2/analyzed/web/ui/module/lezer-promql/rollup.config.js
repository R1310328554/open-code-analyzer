// lezer-promql Rollup 配置：将 parser.js 打包为 CommonJS 与 ESM 双格式 dist 产物。

import path from "node:path"
import { nodeResolve } from "@rollup/plugin-node-resolve"

export default {
  input: "./src/parser.js",
  output: [{
    format: "cjs",
    file: "./dist/index.cjs"
  }, {
    format: "es",
    file: "./dist/index.es.js"
  }],
  external(id) { return !/^[.\/]/.test(id) && !path.isAbsolute(id) },
  plugins: [
    nodeResolve()
  ]
}
// Rollup 构建脚本为 @prometheus-io/lezer-promql 生成发布包。
