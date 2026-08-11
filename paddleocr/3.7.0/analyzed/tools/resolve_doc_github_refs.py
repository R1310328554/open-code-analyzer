# 文档构建前占位符替换：将 docs 目录中文本文件里的 GitHub source-ref 占位符
# 批量替换为实际 commit/tag，避免文档链接指向不稳定分支
#!/usr/bin/env python3
import argparse
from pathlib import Path


# 参与占位符扫描与替换的文本类扩展名集合
TEXT_SUFFIXES = {".md", ".yml", ".yaml"}


# 递归遍历 root 下 md/yml/yaml 文件，将 placeholder 替换为 source_ref
# source_ref 须为非空且无空白字符的 Git ref；返回被修改的文件路径列表
def resolve_placeholders(root, placeholder, source_ref):
    if (
        not source_ref
        or source_ref.strip() != source_ref
        or any(c.isspace() for c in source_ref)
    ):
        raise ValueError("source_ref must be a non-empty ref without whitespace")

    root = Path(root)
    changed = []
    for path in sorted(root.rglob("*")):
        if not path.is_file() or path.suffix not in TEXT_SUFFIXES:
            continue
        content = path.read_text(encoding="utf-8")
        if placeholder not in content:
            continue
        path.write_text(content.replace(placeholder, source_ref), encoding="utf-8")
        changed.append(path)
    return changed


# CLI 入口：--root 指定文档根目录，--placeholder 与 --source-ref 必填
def main(argv=None):
    parser = argparse.ArgumentParser(
        description="Resolve docs GitHub source-ref placeholders before building docs."
    )
    parser.add_argument("--root", default="docs", help="Directory to rewrite.")
    parser.add_argument("--placeholder", required=True)
    parser.add_argument("--source-ref", required=True)
    args = parser.parse_args(argv)

    changed = resolve_placeholders(
        args.root,
        placeholder=args.placeholder,
        source_ref=args.source_ref,
    )
    print(f"Resolved {len(changed)} file(s) under {args.root}.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
