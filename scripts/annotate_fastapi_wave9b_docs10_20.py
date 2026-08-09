#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-9b docs_src [10:20]."""
from __future__ import annotations

import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "fastapi/0.141.1"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = [
    ln.strip()
    for ln in Path("/tmp/fastapi_w9b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "docs_src/dependencies/tutorial008_py310.py": [
        (
            "from fastapi import Depends",
            '"""教程 008：yield 依赖嵌套——A→B→C 链式注入，子依赖自动获得父依赖 yield 的值。"""\n\nfrom fastapi import Depends',
        ),
        (
            "async def dependency_a():",
            'async def dependency_a():\n    """最外层 yield 依赖：创建 dep_a，响应后 finally 调用 close。"""',
        ),
        (
            "    dep_a = generate_dep_a()",
            "    dep_a = generate_dep_a()  # 模拟获取需清理的资源",
        ),
        (
            "        yield dep_a",
            "        yield dep_a  # 将 dep_a 提供给下游 Depends",
        ),
        (
            "        dep_a.close()",
            "        dep_a.close()  # yield 之后执行清理",
        ),
        (
            "async def dependency_b(dep_a=Depends(dependency_a)):",
            'async def dependency_b(dep_a=Depends(dependency_a)):\n    """Depends(dependency_a) 先运行 A，再把 yield 出的 dep_a 注入本函数。"""',
        ),
        (
            "    dep_b = generate_dep_b()",
            "    dep_b = generate_dep_b()  # 基于 dep_a 创建下一层资源",
        ),
        (
            "        dep_b.close(dep_a)",
            "        dep_b.close(dep_a)  # 清理时可使用上游依赖产物",
        ),
        (
            "async def dependency_c(dep_b=Depends(dependency_b)):",
            'async def dependency_c(dep_b=Depends(dependency_b)):\n    """整条链：C 依赖 B，B 依赖 A；FastAPI 按拓扑顺序解析。"""',
        ),
        (
            "    dep_c = generate_dep_c()",
            "    dep_c = generate_dep_c()  # 最内层资源",
        ),
        (
            "        dep_c.close(dep_b)",
            "        dep_c.close(dep_b)  # 由内向外依次执行 finally",
        ),
    ],
    "docs_src/dependencies/tutorial008_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 008（Annotated）：嵌套 yield 依赖链，用 Annotated 声明类型与 Depends。"""\n\nfrom typing import Annotated',
        ),
        (
            "async def dependency_a():",
            'async def dependency_a():\n    """与 tutorial008 相同的最外层 yield 依赖。"""',
        ),
        (
            "        yield dep_a",
            "        yield dep_a  # 产出 DepA 实例",
        ),
        (
            "        dep_a.close()",
            "        dep_a.close()  # 请求结束后清理",
        ),
        (
            "async def dependency_b(dep_a: Annotated[DepA, Depends(dependency_a)]):",
            'async def dependency_b(dep_a: Annotated[DepA, Depends(dependency_a)]):\n    """Annotated 同时标注 DepA 类型与 Depends(dependency_a) 元数据。"""',
        ),
        (
            "        dep_b.close(dep_a)",
            "        dep_b.close(dep_a)  # finally 中引用上游 dep_a",
        ),
        (
            "async def dependency_c(dep_b: Annotated[DepB, Depends(dependency_b)]):",
            'async def dependency_c(dep_b: Annotated[DepB, Depends(dependency_b)]):\n    """链末端依赖；Annotated 写法便于 IDE 与类型检查。"""',
        ),
        (
            "        dep_c.close(dep_b)",
            "        dep_c.close(dep_b)  # 嵌套依赖的清理顺序与 Depends 版一致",
        ),
    ],
    "docs_src/dependencies/tutorial008b_py310.py": [
        (
            "from fastapi import Depends, FastAPI, HTTPException",
            '"""教程 008b：yield 依赖内捕获路径操作抛出的异常并转为 HTTPException。"""\n\nfrom fastapi import Depends, FastAPI, HTTPException',
        ),
        (
            "data = {",
            "# Rick and Morty 主题模拟数据\n"
            "data = {",
        ),
        (
            "class OwnerError(Exception):",
            'class OwnerError(Exception):\n    """自定义业务异常：物品所有者不匹配。"""',
        ),
        (
            "def get_username():",
            'def get_username():\n    """yield 依赖：路径操作中 raise OwnerError 时在此 except 并转为 400。"""',
        ),
        (
            "        yield \"Rick\"",
            '        yield "Rick"  # 模拟当前登录用户',
        ),
        (
            "    except OwnerError as e:",
            "    except OwnerError as e:  # 捕获路径操作在 yield 之后抛出的 OwnerError",
        ),
        (
            "        raise HTTPException(status_code=400, detail=f\"Owner error: {e}\")",
            '        raise HTTPException(status_code=400, detail=f"Owner error: {e}")  # 转为 HTTP 错误响应',
        ),
        (
            "def get_item(item_id: str, username: str = Depends(get_username)):",
            'def get_item(item_id: str, username: str = Depends(get_username)):\n    """若 item 的 owner 与 username 不符则 raise OwnerError，由依赖捕获。"""',
        ),
        (
            "        raise OwnerError(username)",
            "        raise OwnerError(username)  # 在 yield 之后抛出，进入依赖的 except",
        ),
    ],
    "docs_src/dependencies/tutorial008b_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 008b（Annotated）：yield 依赖捕获 OwnerError 并转 HTTPException。"""\n\nfrom typing import Annotated',
        ),
        (
            "class OwnerError(Exception):",
            'class OwnerError(Exception):\n    """所有者校验失败时抛出的自定义异常。"""',
        ),
        (
            "def get_username():",
            'def get_username():\n    """yield 用户名；except OwnerError 将业务异常包装为 HTTPException。"""',
        ),
        (
            "        yield \"Rick\"",
            '        yield "Rick"  # 注入到路径操作的 username 参数',
        ),
        (
            "    except OwnerError as e:",
            "    except OwnerError as e:  # 路径操作 raise 后回到依赖的 except 块",
        ),
        (
            "def get_item(item_id: str, username: Annotated[str, Depends(get_username)]):",
            'def get_item(item_id: str, username: Annotated[str, Depends(get_username)]):\n    """Annotated 声明 username 来自 get_username yield 依赖。"""',
        ),
        (
            "        raise OwnerError(username)",
            "        raise OwnerError(username)  # 触发依赖内异常处理逻辑",
        ),
    ],
    "docs_src/dependencies/tutorial008c_py310.py": [
        (
            "from fastapi import Depends, FastAPI, HTTPException",
            '"""教程 008c：yield 依赖捕获 InternalError 但不重新抛出（仅打印，异常被“吞掉”）。"""\n\nfrom fastapi import Depends, FastAPI, HTTPException',
        ),
        (
            "class InternalError(Exception):",
            'class InternalError(Exception):\n    """内部业务异常；本示例演示捕获后不 re-raise 的行为。"""',
        ),
        (
            "def get_username():",
            'def get_username():\n    """捕获 InternalError 后只 print，不 raise——客户端可能收不到该错误。"""',
        ),
        (
            "        yield \"Rick\"",
            '        yield "Rick"  # 正常注入用户名',
        ),
        (
            "    except InternalError:",
            "    except InternalError:  # 捕获但不 re-raise，演示“吞异常”反模式",
        ),
        (
            "        print(\"Oops, we didn't raise again, Britney 😱\")",
            '        print("Oops, we didn\'t raise again, Britney 😱")  # 仅日志，异常不再向上传播',
        ),
        (
            "def get_item(item_id: str, username: str = Depends(get_username)):",
            'def get_item(item_id: str, username: str = Depends(get_username)):\n    """访问 portal-gun 时 raise InternalError，依赖会捕获但不转 HTTP 响应。"""',
        ),
        (
            "        raise InternalError(",
            "        raise InternalError(  # 在 yield 之后抛出，进入依赖 except",
        ),
    ],
    "docs_src/dependencies/tutorial008c_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 008c（Annotated）：演示 yield 依赖吞掉 InternalError 而不 re-raise。"""\n\nfrom typing import Annotated',
        ),
        (
            "class InternalError(Exception):",
            'class InternalError(Exception):\n    """路径操作抛出的内部异常。"""',
        ),
        (
            "def get_username():",
            'def get_username():\n    """except InternalError 后仅打印，不 raise HTTPException。"""',
        ),
        (
            "    except InternalError:",
            "    except InternalError:  # 与 Depends 版相同：捕获后不向上抛出",
        ),
        (
            "def get_item(item_id: str, username: Annotated[str, Depends(get_username)]):",
            'def get_item(item_id: str, username: Annotated[str, Depends(get_username)]):\n    """Annotated 注入 username；InternalError 由依赖静默处理。"""',
        ),
    ],
    "docs_src/dependencies/tutorial008d_py310.py": [
        (
            "from fastapi import Depends, FastAPI, HTTPException",
            '"""教程 008d：yield 依赖捕获 InternalError 后 re-raise，让 FastAPI 正常处理异常。"""\n\nfrom fastapi import Depends, FastAPI, HTTPException',
        ),
        (
            "class InternalError(Exception):",
            'class InternalError(Exception):\n    """需在依赖中捕获并重新抛出的内部异常。"""',
        ),
        (
            "def get_username():",
            'def get_username():\n    """捕获 InternalError 做清理/日志后 raise，异常继续向上传播。"""',
        ),
        (
            "    except InternalError:",
            "    except InternalError:  # 可在 re-raise 前执行清理或记录",
        ),
        (
            "        print(\"We don't swallow the internal error here, we raise again 😎\")",
            '        print("We don\'t swallow the internal error here, we raise again 😎")  # 记录后 re-raise',
        ),
        (
            "        raise",
            "        raise  # 裸 raise 保留原始 traceback，FastAPI 可转为 500 等",
        ),
        (
            "def get_item(item_id: str, username: str = Depends(get_username)):",
            'def get_item(item_id: str, username: str = Depends(get_username)):\n    """portal-gun 触发 InternalError；依赖 re-raise 后由框架处理。"""',
        ),
    ],
    "docs_src/dependencies/tutorial008d_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 008d（Annotated）：yield 依赖捕获后 re-raise InternalError。"""\n\nfrom typing import Annotated',
        ),
        (
            "def get_username():",
            'def get_username():\n    """except 中 raise 裸异常，与 tutorial008d Depends 版行为一致。"""',
        ),
        (
            "        raise",
            "        raise  # 重新抛出，不吞掉路径操作中的 InternalError",
        ),
        (
            "def get_item(item_id: str, username: Annotated[str, Depends(get_username)]):",
            'def get_item(item_id: str, username: Annotated[str, Depends(get_username)]):\n    """Annotated 写法；InternalError 经依赖 re-raise 后由 FastAPI 处理。"""',
        ),
    ],
    "docs_src/dependencies/tutorial008e_py310.py": [
        (
            "from fastapi import Depends, FastAPI",
            '"""教程 008e：Depends(scope="function") 在路径操作返回后立即执行 yield 后/finally 清理。"""\n\nfrom fastapi import Depends, FastAPI',
        ),
        (
            "def get_username():",
            'def get_username():\n    """默认 scope 为 request；scope=\\"function\\" 时 handler 结束即触发 finally。"""',
        ),
        (
            "        yield \"Rick\"",
            '        yield "Rick"  # 注入当前用户名',
        ),
        (
            "        print(\"Cleanup up before response is sent\")",
            '        print("Cleanup up before response is sent")  # finally：在响应发送前执行清理',
        ),
        (
            "def get_user_me(username: str = Depends(get_username, scope=\"function\")):",
            'def get_user_me(username: str = Depends(get_username, scope="function")):\n    """scope=\\"function\\"：handler 返回后立刻运行依赖的 finally，不必等整个请求结束。"""',
        ),
    ],
    "docs_src/dependencies/tutorial008e_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 008e（Annotated）：Depends(..., scope="function") 提前结束 yield 依赖生命周期。"""\n\nfrom typing import Annotated',
        ),
        (
            "def get_username():",
            'def get_username():\n    """yield 用户名；finally 在 function scope 下于 handler 返回后执行。"""',
        ),
        (
            "        print(\"Cleanup up before response is sent\")",
            '        print("Cleanup up before response is sent")  # 早于默认 request scope 的清理时机',
        ),
        (
            "def get_user_me(username: Annotated[str, Depends(get_username, scope=\"function\")]):",
            'def get_user_me(username: Annotated[str, Depends(get_username, scope="function")]):\n    """Annotated + scope=\\"function\\"：类型与依赖元数据合一，清理在函数级提前触发。"""',
        ),
    ],
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def apply_replacements(text: str, rel: str) -> str:
    for old, new in FILE_REPLACEMENTS.get(rel, []):
        if old not in text:
            if has_chinese(text):
                continue
            raise ValueError(f"Pattern not found in {rel}:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def annotate_file(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise FileNotFoundError(f"missing original: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    if not dst.exists() or not has_chinese(dst.read_text(encoding="utf-8")):
        shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    text = apply_replacements(text, rel)
    if not has_chinese(text):
        raise ValueError(f"No Chinese content after annotation: {rel}")
    dst.write_text(text, encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        try:
            annotate_file(rel)
            ok += 1
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if failures:
        print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
        return 1
    subprocess.run(
        [
            sys.executable,
            str(ROOT / "scripts/mark_batch_done.py"),
            "--project",
            "fastapi",
            "--version",
            "0.141.1",
            "--note",
            "wave9b",
            *BATCH_FILES,
        ],
        check=True,
    )
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    batch["remaining_pending"] = len(
        [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Marked {len(BATCH_FILES)} files done in queue (note=wave9b)")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
