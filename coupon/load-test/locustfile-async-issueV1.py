import random
from locust import task, FastHttpUser

class CouponIssueV1(FastHttpUser):
    connection_timeout = 10.0
    network_timeout = 10.0

    # 부하 테스트를 위한 task 정의
    @task
    def issue(self):
        payload = {
            # "userId": 1, # 중복 요청 발급에 대한 테스트 목적으로 userId를 1로 고정
            "userId": random.randint(1, 100000000),
            "couponId": 1 # 이미 테이블에 1이 저장되어있다고 가정
        }
        with self.rest("POST", "/v1/issue-async", json=payload):
            pass