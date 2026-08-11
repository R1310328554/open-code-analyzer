# 组合生成 *args 到 Tuple[_T0, ...] 的 @overload 映射（弥补 PEP-484 无法 unpack）。
# .. versionadded:: 2.0

# mypy: ignore-errors

# 元组长度 combinatoric overload 生成器：select()/session.get() 等 API

from __future__ import annotations

import importlib
import os
from pathlib import Path
import re
import sys
from tempfile import NamedTemporaryFile
import textwrap

from sqlalchemy.util.tool_support import code_writer_cmd

is_posix = os.name == "posix"


sys.path.append(str(Path(__file__).parent.parent))


# 在 START/END OVERLOADED FUNCTIONS 块内写入指定长度范围的 overload
def process_module(
    modname: str, filename: str, expected_number: int, cmd: code_writer_cmd
) -> str:
    # use tempfile in same path as the module, or at least in the
    # current working directory, so that black / zimports use
    # local pyproject.toml
    found = 0
    with (
        NamedTemporaryFile(
            mode="w",
            delete=False,
            suffix=".py",
        ) as buf,
        open(filename) as orig_py,
    ):
        indent = ""
        in_block = False
        current_fnname = given_fnname = None
        for line in orig_py:
            m = re.match(
                r"^( *)# START OVERLOADED FUNCTIONS ([\.\w_]+) ([\w_]+) (\d+)-(\d+)(?: \"(.+)\")?",  # noqa: E501
                line,
            )
            if m:
                found += 1
                indent = m.group(1)
                given_fnname = current_fnname = m.group(2)
                if current_fnname.startswith("self."):
                    use_self = True
                    current_fnname = current_fnname.split(".")[1]
                else:
                    use_self = False
                return_type = m.group(3)
                start_index = int(m.group(4))
                end_index = int(m.group(5))
                extra_args = m.group(6) or ""

                cmd.write_status(
                    f"Generating {start_index}-{end_index} overloads "
                    f"attributes for "
                    f"class {'self.' if use_self else ''}{current_fnname} "
                    f"-> {return_type}\n"
                )
                in_block = True
                buf.write(line)
                buf.write(
                    "\n    # code within this block is "
                    "**programmatically, \n"
                    "    # statically generated** by"
                    f" tools/{os.path.basename(__file__)}\n\n"
                )

                for num_args in range(start_index, end_index + 1):
                    combinations = [
                        [
                            f"__ent{arg}: _TCCA[_T{arg}]"
                            for arg in range(num_args)
                        ]
                    ]
                    for combination in combinations:
                        buf.write(
                            textwrap.indent(
                                f"""
@overload
def {current_fnname}(
    {'self, ' if use_self else ''}{", ".join(combination)}{extra_args}
) -> {return_type}[Tuple[{', '.join(f'_T{i}' for i in range(num_args))}]]:
    ...

""",  # noqa: E501
                                indent,
                            )
                        )

            if in_block and line.startswith(
                f"{indent}# END OVERLOADED FUNCTIONS {given_fnname}"
            ):
                in_block = False

            if not in_block:
                buf.write(line)
    if found != expected_number:
        raise Exception(
            f"{modname} processed {found}. expected {expected_number}"
        )
    return buf.name


# 导入目标模块、生成、格式化并写回源文件
def run_module(modname: str, count: int, cmd: code_writer_cmd) -> None:
    cmd.write_status(f"importing module {modname}\n")
    mod = importlib.import_module(modname)
    destination_path = mod.__file__
    assert destination_path is not None

    tempfile = process_module(modname, destination_path, count, cmd)

    cmd.run_zimports(tempfile)
    cmd.run_black(tempfile)
    cmd.write_output_file_from_tempfile(tempfile, destination_path)


# 按 entries 列表批量处理各模块的 overload 块
def main(cmd: code_writer_cmd) -> None:
    for modname, count in entries:
        if cmd.args.module in {"all", modname}:
            run_module(modname, count, cmd)


entries = [
    ("sqlalchemy.sql._selectable_constructors", 1),
    ("sqlalchemy.orm.session", 1),
    ("sqlalchemy.orm.query", 1),
    ("sqlalchemy.sql.selectable", 1),
    ("sqlalchemy.sql.dml", 3),
]

if __name__ == "__main__":
    cmd = code_writer_cmd(__file__)

    with cmd.add_arguments() as parser:
        parser.add_argument(
            "--module",
            choices=[n for n, _ in entries] + ["all"],
            default="all",
            help="Which file to generate. Default is to regenerate all files",
        )

    with cmd.run_program():
        main(cmd)
