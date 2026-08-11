// Demo 应用 Vite 配置：开发态别名指向 core 源码，启用 COOP/COEP 以支持 SharedArrayBuffer
import { resolve } from "node:path";
import { defineConfig } from "vite";

  // serve 时将 @paddleocr/paddleocr-js 映射到 packages/core 源码便于热更新
export default defineConfig(({ command }) => ({
  resolve: {
    alias:
      command === "serve"
        ? {
            "@paddleocr/paddleocr-js/viz": resolve(
              __dirname,
              "../../packages/core/src/viz/index.ts"
            ),
            "@paddleocr/paddleocr-js": resolve(__dirname, "../../packages/core/src/index.ts")
          }
        : {}
  },
    // Worker 打包为 ES module 格式
  worker: {
    format: "es"
  },
    // 开发服务器响应头：same-origin + credentialless 满足 ORT WASM 多线程要求
  server: {
    headers: {
      "Cross-Origin-Opener-Policy": "same-origin",
      "Cross-Origin-Embedder-Policy": "credentialless"
    }
  },
  preview: {
    headers: {
      "Cross-Origin-Opener-Policy": "same-origin",
      "Cross-Origin-Embedder-Policy": "credentialless"
    }
  }
}));
