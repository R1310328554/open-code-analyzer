/**
 * Keycloak Admin UI 的 Vite 构建与开发服务器配置。
 * 支持两种模式：独立 SPA 开发/打包，以及 LIB=true 时输出可嵌入的 ES 库。
 */
import react from "@vitejs/plugin-react-swc";
import { readFileSync } from "node:fs";
import path from "path";
import { getProperties } from "properties-file";
import { defineConfig, loadEnv } from "vite";
import { checker } from "vite-plugin-checker";
import dts from "vite-plugin-dts";
import { configDefaults } from "vitest/config";

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  // 库模式下由宿主应用提供这些 peer 依赖
  const external = ["react", "react/jsx-runtime", "react-dom"];
  const plugins = [
    react(),
    checker({ typescript: true }),
    {
      // 开发时将 message-bundle 虚拟模块解析为英文 properties 对象
      name: "message-bundle-transformer",
      resolveId(source) {
        if (source === "message-bundle") {
          return source;
        }
        return null;
      },
      load(id) {
        if (id === "message-bundle") {
          let properties = {};
          if (process.env.NODE_ENV === "development") {
            properties = getProperties(
              readFileSync(
                "./maven-resources/theme/keycloak.v2/admin/messages/messages_en.properties",
              ),
            );
          }
          return {
            code: `export default ${JSON.stringify(properties)};`,
          };
        }
        return null;
      },
    },
  ];
  const input = env.LIB ? undefined : "src/main.tsx";
  if (env.LIB) {
    external.push("react-router-dom");
    external.push("react-i18next");
    plugins.push(dts({ insertTypesEntry: true }));
  }

  const lib = env.LIB
    ? {
        // npm 包构建：输出到 lib/，入口 index.ts
        outDir: "lib",
        lib: {
          entry: path.resolve(__dirname, "src/index.ts"),
          formats: ["es"],
        },
      }
    : {
        // 主题资源构建：打入 Keycloak 服务器 theme 目录
        outDir: "target/classes/theme/keycloak.v2/admin/resources",
        external: ["src/index.ts"],
      };
  return {
    base: "",
    server: {
      origin: "http://localhost:5174",
      port: 5174,
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
    test: {
      watch: false,
      environment: "jsdom",
      exclude: [...configDefaults.exclude, "./test/**"],
      server: {
        deps: {
          inline: [/@patternfly\/.*/],
        },
      },
    },
  };
});
