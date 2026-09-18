"""
TestHub 性能测试引擎
基于 Locust, 支持并发压测、响应时间统计、TPS/QPS 计算
"""
import json
import time
from typing import Dict, List, Any, Optional
from dataclasses import dataclass, field


@dataclass
class PerfRequest:
    name: str
    method: str
    url: str
    headers: Optional[Dict] = None
    body: Optional[str] = None
    weight: int = 1  # 请求权重


@dataclass
class PerfConfig:
    users: int = 10          # 并发用户数
    spawn_rate: int = 2      # 每秒启动用户数
    run_time: int = 60       # 运行时长(秒)
    host: str = ""           # 目标主机
    requests: List[PerfRequest] = field(default_factory=list)


@dataclass
class PerfStats:
    total_requests: int = 0
    failures: int = 0
    avg_response_time: float = 0.0
    min_response_time: float = 0.0
    max_response_time: float = 0.0
    p50: float = 0.0
    p90: float = 0.0
    p95: float = 0.0
    p99: float = 0.0
    rps: float = 0.0  # 每秒请求数
    error_rate: float = 0.0


class PerfTestEngine:
    """性能测试引擎 - 使用 Locust 进行压测"""

    def run(self, config: PerfConfig) -> Dict[str, Any]:
        """
        执行性能测试
        返回统计结果
        """
        from locust import HttpUser, task, between, events
        from locust.runners import MasterRunner
        import gevent

        # 构建 Locust User 类
        request_configs = config.requests

        class TestHubUser(HttpUser):
            wait_time = between(0.1, 0.5)
            host = config.host

            @task
            def execute_requests(self):
                for req in request_configs:
                    with self.client.request(
                        method=req.method,
                        url=req.url,
                        headers=req.headers,
                        data=req.body,
                        name=req.name,
                        catch_response=True,
                    ) as response:
                        if response.status_code >= 400:
                            response.failure(f"状态码: {response.status_code}")
                        else:
                            response.success()

        # 使用 Locust 的编程 API 运行
        from locust.env import Environment
        env = Environment(user_classes=[TestHubUser])
        env.create_local_runner()

        # 启动压测
        env.runner.start(config.users, spawn_rate=config.spawn_rate)
        gevent.sleep(config.run_time)
        env.runner.stop()

        # 从 Locust 内置统计获取结果
        total = env.runner.stats.total
        stats = PerfStats(
            total_requests=total.num_requests,
            failures=total.num_failures,
            avg_response_time=total.avg_response_time,
            min_response_time=total.min_response_time or 0,
            max_response_time=total.max_response_time,
            p50=total.get_response_time_percentile(50),
            p90=total.get_response_time_percentile(90),
            p95=total.get_response_time_percentile(95),
            p99=total.get_response_time_percentile(99),
            rps=total.total_rps,
            error_rate=(total.num_failures / total.num_requests * 100) if total.num_requests > 0 else 0,
        )
        return stats.__dict__

    def _calc_stats(self, records: List[Dict]) -> Dict[str, Any]:
        if not records:
            return {}

        response_times = [r["response_time"] for r in records]
        response_times.sort()
        n = len(response_times)

        success_count = sum(1 for r in records if r["success"])
        failures = n - success_count

        total_duration = sum(response_times) / 1000.0  # 转换为秒

        stats = PerfStats(
            total_requests=n,
            failures=failures,
            avg_response_time=sum(response_times) / n,
            min_response_time=min(response_times),
            max_response_time=max(response_times),
            p50=self._percentile(response_times, 50),
            p90=self._percentile(response_times, 90),
            p95=self._percentile(response_times, 95),
            p99=self._percentile(response_times, 99),
            rps=n / total_duration if total_duration > 0 else 0,
            error_rate=(failures / n) * 100,
        )

        return stats.__dict__

    @staticmethod
    def _percentile(sorted_data: List[float], p: float) -> float:
        n = len(sorted_data)
        k = (p / 100) * (n - 1)
        f = int(k)
        c = f + 1 if f + 1 < n else f
        return sorted_data[f] + (sorted_data[c] - sorted_data[f]) * (k - f)


def run_perf_test_simple(url: str, method: str = "GET", users: int = 10, duration: int = 30):
    """简单性能测试入口"""
    config = PerfConfig(
        users=users,
        run_time=duration,
        host="",
        requests=[PerfRequest(name="test", method=method, url=url)],
    )
    engine = PerfTestEngine()
    return engine.run(config)


if __name__ == "__main__":
    # 示例: 对登录接口进行性能测试
    result = run_perf_test_simple(
        url="http://localhost:8088/api/auth/login",
        method="POST",
        users=20,
        duration=30,
    )
    print("性能测试结果:")
    print(json.dumps(result, indent=2, ensure_ascii=False))
