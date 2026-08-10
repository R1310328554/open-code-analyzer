import react from "@vitejs/plugin-react-swc";
import path from "path";
import { defineConfig, loadEnv } from "vite";
import { checker } from "vite-plugin-checker";
import dts from "vite-plugin-dts";

// https://vitejs.dev/config/
/** Account UI 的 Vite 构建配置：支持开发服务器与可发布的 ES 库两种模式。 */
export default defineConfig(({ mode }) => {
  // 从项目根目录加载环境变量（含 LIB 开关）
  const env = loadEnv(mode, process.cwd(), "");
  // 库模式下将 React 相关包标记为外部依赖，避免重复打包
  const external = ["react", "react/jsx-runtime", "react-dom"];
  const plugins = [react(), checker({ typescript: true })];
  // 非库模式以 main.tsx 为入口；库模式由 lib.entry 指定
  const input = env.LIB ? undefined : "src/main.tsx";
  if (env.LIB) {
    external.push("react-router-dom");
    external.push("react-i18next");
    // 库构建时生成 .d.ts 类型入口
    plugins.push(dts({ insertTypesEntry: true }));
  }
  const lib = env.LIB
    ? {
        copyPublicDir: false,
        outDir: "lib",
        lib: {
          entry: path.resolve(__dirname, "src/index.ts"),
          formats: ["es"],
        },
      }
    : {
        // 应用模式输出到 Keycloak 主题资源目录，供服务端静态托管
        outDir: "target/classes/theme/keycloak.v3/account/resources",
      };
  return {
    base: "",
    server: {
      origin: "http://localhost:5173",
      port: 5173,
    },
    build: {
      ...lib,
      sourcemap: true,
      target: "esnext",
      modulePreload: false,
      cssMinify: "lightningcss",
      manifest: true,
      rollupOptions: {
        input,
        external,
      },
    },
    plugins,
  };
});
