package jsh.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "시스템 API", description = "서버 상태 확인 및 테스트용 API")
@RestController
public class RootController {

    @Operation(summary = "서버 접속 확인", description = "루트 경로 접속 시 서버 상태를 반환합니다.")
    @GetMapping("/")
    public String index() {
        return "JSH Board API Server is Running";
    }

    @Operation(summary = "헬스 체크", description = "모니터링 도구에서 서버 활성화 상태를 확인하는 용도입니다.")
    @GetMapping("/api/health")
    public Map<String, String> healthCheck() {
        return Map.of(
                "status", "UP",
                "message", "Service is healthy"
        );
    }
}
