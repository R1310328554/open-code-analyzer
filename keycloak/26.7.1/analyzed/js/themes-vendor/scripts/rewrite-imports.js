import fs from "node:fs/promises";
import path from "node:path";

/** Rollup 产出后修正 react-jsx-runtime 中对 react 的相对 import 路径 */
const targetDir = "target/classes/theme/keycloak/common/resources/vendor";

replaceContents(
  path.join(targetDir, "react/react-jsx-runtime.production.min.js"),
  '"./react.production.min.js"',
  '"react"',
);

/** 读取文件并将 search 字符串替换为 replace 后写回 */
async function replaceContents(filePath, search, replace) {
  const file = await fs.readFile(filePath, "utf8");
  const newFile = file.replace(search, replace);

  await fs.writeFile(filePath, newFile);
}
