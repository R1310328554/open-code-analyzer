// TypeScript SDK 打包配置：ESM/CJS 双格式与类型声明
import { defineConfig } from "tsup";

// tsup 入口、输出格式与 dts 生成选项
export default defineConfig({
  entry: ["src/index.ts"],
  format: ["esm", "cjs"],
  dts: true,
  clean: true,
  splitting: false,
});
