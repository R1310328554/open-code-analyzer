import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import license from "rollup-plugin-license";
import path from "path";

// Prometheus Mantine UI 的 Vite 构建与开发服务器配置。
// https://vitejs.dev/config/
export default defineConfig({
  base: '',
  plugins: [react()],
  build: {
    rollupOptions: {
      plugins: [
// rollup-plugin-license 收集打包进 bundle 的第三方许可证，满足归因要求。
        // Collect the licenses of all third-party packages that end up in the
// 许可证汇总写入 dist/assets/third-party-licenses.txt 随 Prometheus 分发。
        // bundle and write them to a single file that is embedded and shipped
        // with Prometheus, satisfying their attribution requirements.
        license({
          thirdParty: {
            includePrivate: false,
            output: {
              file: path.resolve(
                __dirname,
                "dist",
                "assets",
                "third-party-licenses.txt"
              ),
            },
          },
        }),
      ],
    },
  },
// 开发服务器将 /api 与 /-/ 代理到本地 Prometheus 9090 端口。
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:9090",
      },
      "/-/": {
        target: "http://localhost:9090",
      },
      // "/api": {
      //   target: "https://prometheus.demo.do.prometheus.io/",
      //   changeOrigin: true,
      // },
      // "/-/": {
      //   target: "https://prometheus.demo.do.prometheus.io/",
      //   changeOrigin: true,
      // },
    },
  },
});
