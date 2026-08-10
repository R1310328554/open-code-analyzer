/**
 * Vitest 测试配置：合并主 Vite 配置并设置 Node 环境与全局测试 API。
 */
import { defineConfig, mergeConfig } from "vite";
import path from "path";
import baseConfig from "./vite.config";

export default defineConfig((configEnv) =>
  mergeConfig(
    baseConfig(configEnv),
    defineConfig({
      resolve: {
        alias: {
          "@": path.resolve(__dirname, "./src"),
          "@/gotypes": path.resolve(__dirname, "./codegen/gotypes.gen.ts"),
        },
      },
      test: {
        environment: "node",
        globals: true,
      },
    }),
  ),
);
