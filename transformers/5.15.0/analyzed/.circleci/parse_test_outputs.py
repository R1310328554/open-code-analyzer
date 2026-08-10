"""
解析 CircleCI / pytest 文本输出。
汇总 SKIPPED、FAILED、ERROR 行并按原因分组统计，便于 CI 快速定位失败模式。
"""

import argparse
import re


def parse_pytest_output(file_path):
    skipped_tests = {}
    skipped_count = 0
    with open(file_path, 'r', encoding='utf-8') as file:
        for line in file:
            match = re.match(r'^SKIPPED \[(\d+)\] (tests/.*): (.*)$', line)
            if match:
                skipped_count += 1
                test_file, test_line, reason = match.groups()
                skipped_tests[reason] = skipped_tests.get(reason, []) + [(test_file, test_line)]
    for k,v in sorted(skipped_tests.items(), key=lambda x:len(x[1])):
        print(f"{len(v):4} skipped because: {k}")
    print("Number of skipped tests:", skipped_count)

# 统计失败用例并按异常类型聚合；存在失败时以 exit(1) 结束。
def parse_pytest_failure_output(file_path):
    failed_tests = {}
    failed_count = 0
    with open(file_path, 'r', encoding='utf-8') as file:
        for line in file:
            match = re.match(r'^FAILED (tests/.*) - (.*): (.*)$', line)
            if match:
                failed_count += 1
                _, error, reason = match.groups()
                failed_tests[reason] = failed_tests.get(reason, []) + [error]
    for k,v in sorted(failed_tests.items(), key=lambda x:len(x[1])):
        print(f"{len(v):4} failed because `{v[0]}` -> {k}")
    print("Number of failed tests:", failed_count)
    if failed_count>0:
        exit(1)

# 统计 ERROR 级别错误（如 fixture/setup 失败）并分组展示。
def parse_pytest_errors_output(file_path):
    print(file_path)
    error_tests = {}
    error_count = 0
    with open(file_path, 'r', encoding='utf-8') as file:
        for line in file:
            match = re.match(r'^ERROR (tests/.*) - (.*): (.*)$', line)
            if match:
                error_count += 1
                _, test_error, reason = match.groups()
                error_tests[reason] = error_tests.get(reason, []) + [test_error]
    for k,v in sorted(error_tests.items(), key=lambda x:len(x[1])):
        print(f"{len(v):4} errored out because of `{v[0]}` -> {k}")
    print("Number of errors:", error_count)
    if error_count>0:
        exit(1)

# 命令行入口：按 --skip/--fail/--errors 选择解析模式。
def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--file", help="file to parse")
    parser.add_argument("--skip", action="store_true", help="show skipped reasons")
    parser.add_argument("--fail", action="store_true", help="show failed tests")
    parser.add_argument("--errors", action="store_true", help="show failed tests")
    args = parser.parse_args()

    if args.skip:
        parse_pytest_output(args.file)

    if args.fail:
        parse_pytest_failure_output(args.file)

    if args.errors:
        parse_pytest_errors_output(args.file)


if __name__ == "__main__":
    main()
