#!/usr/bin/env python3
# 扫描 Markdown 文档，拒绝指向不稳定 GitHub 分支/标签的源码链接
#!/usr/bin/env python3
import argparse
import re
from pathlib import Path
from typing import NamedTuple


# 文档链接违规记录：文件路径、行号、Git ref 与完整 URL
class Violation(NamedTuple):
    path: Path
    line_number: int
    ref: str
    url: str


# 编译匹配 github.com/{repo}/blob|tree/{ref} 的正则
def _compile_link_pattern(repo_slug):
    escaped_repo = re.escape(repo_slug)
    return re.compile(
        rf"https://github\.com/{escaped_repo}/(?:blob|tree)/(?P<ref>[^/\s)\]\"'<>]+)"
        rf"(?P<rest>/[^\s)\]\"'<>]*)?"
    )


# 递归扫描 root 下 *.md，收集 ref 落在 forbidden_refs 的链接
def find_forbidden_links(root, repo_slug, forbidden_refs):
    root = Path(root)
    forbidden_refs = set(forbidden_refs)
    link_pattern = _compile_link_pattern(repo_slug)
    violations = []

    for path in sorted(root.rglob("*.md")):
        if not path.is_file():
            continue
        for line_number, line in enumerate(
            path.read_text(encoding="utf-8").splitlines(), 1
        ):
            for match in link_pattern.finditer(line):
                ref = match.group("ref")
                if ref in forbidden_refs:
                    violations.append(
                        Violation(
                            path=path,
                            line_number=line_number,
                            ref=ref,
                            url=match.group(0),
                        )
                    )
    return violations


# CLI 入口：--root、--repo-slug、--forbidden-ref，有违规则 exit 1
def main(argv=None):
    parser = argparse.ArgumentParser(
        description="Reject docs links that point to moving GitHub source refs."
    )
    parser.add_argument("--root", default="docs", help="Directory to scan.")
    parser.add_argument(
        "--repo-slug", required=True, help="Example: PaddlePaddle/PaddleOCR"
    )
    parser.add_argument(
        "--forbidden-ref",
        action="append",
        required=True,
        help="Moving source ref to reject. Can be passed multiple times.",
    )
    args = parser.parse_args(argv)

    violations = find_forbidden_links(
        args.root,
        repo_slug=args.repo_slug,
        forbidden_refs=set(args.forbidden_ref),
    )
    if violations:
        for violation in violations:
            print(
                f"{violation.path}:{violation.line_number}: "
                f"forbidden GitHub ref '{violation.ref}' in {violation.url}"
            )
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
