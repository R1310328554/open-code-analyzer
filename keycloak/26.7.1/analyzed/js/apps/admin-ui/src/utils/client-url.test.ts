/** convertClientToUrl：客户端 rootUrl/baseUrl 与运行环境组合逻辑的单元测试。 */
import { describe, expect, it } from "vitest";
import { convertClientToUrl } from "./client-url";

describe("convertClientToUrl", () => {
  it("returns base url when base url starts with http", () => {
    // 给定：绝对 baseUrl
    const baseUrl = "http://something";

    // 执行
    //@ts-ignore
    const result = convertClientToUrl({ baseUrl }, { serverBaseUrl: "" });

    // 断言：直接使用 baseUrl
    expect(result).toBe(baseUrl);
  });

  it("when root url constrains ${authAdminUrl}", () => {
    // 给定：管理端根 URL 占位符
    const rootUrl = "${authAdminUrl}";
    const baseUrl = "/else";

    // 执行
    const result = convertClientToUrl(
      { rootUrl, baseUrl },
      //@ts-ignore
      { adminBaseUrl: "/admin" },
    );

    // 断言：替换为 adminBaseUrl 并拼接 baseUrl
    expect(result).toBe("/admin/else");
  });

  it("when root url constrains ${authBaseUrl}", () => {
    // 给定：认证服务根 URL 占位符
    const rootUrl = "${authBaseUrl}";
    const baseUrl = "/something";

    // 执行
    const result = convertClientToUrl(
      { rootUrl, baseUrl },
      //@ts-ignore
      { serverBaseUrl: "/admin" },
    );

    // 断言
    expect(result).toBe("/admin/something");
  });

  it("when baseUrl when rootUrl is not set", () => {
    // 给定：无 rootUrl，仅相对 baseUrl
    const baseUrl = "/another";

    // 执行
    const result = convertClientToUrl(
      { rootUrl: undefined, baseUrl },
      //@ts-ignore
      { serverBaseUrl: "" },
    );

    // 断言：原样返回 baseUrl
    expect(result).toBe("/another");
  });

  it("when rootUrl starts with http and baseUrl is set", () => {
    // 给定：绝对 root + 相对 base
    const baseUrl = "/another";
    const rootUrl = "http://test.nl";

    // 执行
    const result = convertClientToUrl(
      { rootUrl, baseUrl },
      //@ts-ignore
      { serverBaseUrl: "" },
    );

    // 断言：joinPath 拼接
    expect(result).toBe("http://test.nl/another");
  });

  it("when rootUrl starts with http and baseUrl not set return it", () => {
    // 给定：仅绝对 rootUrl
    const rootUrl = "http://test.nl";

    // 执行
    const result = convertClientToUrl(
      { rootUrl, baseUrl: undefined },
      //@ts-ignore
      { serverBaseUrl: "" },
    );

    // 断言
    expect(result).toBe("http://test.nl");
  });
});
