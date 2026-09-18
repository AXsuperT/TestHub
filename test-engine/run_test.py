"""
TestHub 测试引擎统一入口
支持: 接口测试 / UI测试 / 性能测试
"""
import sys
import json
import argparse
from pathlib import Path

# 添加模块路径
sys.path.insert(0, str(Path(__file__).parent))

from api_test.api_test_engine import ApiTestEngine, ApiCase, load_cases_from_json
from ui_test.ui_test_engine import UiTestEngine, UiTestCase, UiStep
from perf_test.perf_test_engine import PerfTestEngine, PerfConfig, PerfRequest


def run_api_test(cases_file: str, base_url: str = "") -> dict:
    """运行接口测试"""
    engine = ApiTestEngine(base_url=base_url)
    cases = load_cases_from_json(cases_file)
    result = engine.execute_suite(cases)
    return result


def run_ui_test(case_config: dict) -> dict:
    """运行UI测试"""
    engine = UiTestEngine(headless=True)
    steps = [UiStep(**s) for s in case_config.get("steps", [])]
    case = UiTestCase(
        id=case_config.get("id", 0),
        name=case_config.get("name", ""),
        browser=case_config.get("browser", "chrome"),
        steps=steps,
    )
    return engine.execute(case)


def run_perf_test(config: dict) -> dict:
    """运行性能测试"""
    engine = PerfTestEngine()
    requests = [PerfRequest(**r) for r in config.get("requests", [])]
    perf_config = PerfConfig(
        users=config.get("users", 10),
        spawn_rate=config.get("spawn_rate", 2),
        run_time=config.get("run_time", 60),
        host=config.get("host", ""),
        requests=requests,
    )
    return engine.run(perf_config)


def main():
    parser = argparse.ArgumentParser(description="TestHub 测试引擎")
    parser.add_argument("--type", required=True, choices=["api", "ui", "perf"], help="测试类型")
    parser.add_argument("--config", required=True, help="配置文件路径(JSON)")
    parser.add_argument("--base-url", default="", help="接口测试基础URL")

    args = parser.parse_args()

    with open(args.config, "r", encoding="utf-8") as f:
        config = json.load(f)

    if args.type == "api":
        result = run_api_test(args.config, args.base_url)
    elif args.type == "ui":
        result = run_ui_test(config)
    elif args.type == "perf":
        result = run_perf_test(config)
    else:
        print(f"不支持的测试类型: {args.type}")
        sys.exit(1)

    print(json.dumps(result, indent=2, ensure_ascii=False, default=str))


if __name__ == "__main__":
    main()
