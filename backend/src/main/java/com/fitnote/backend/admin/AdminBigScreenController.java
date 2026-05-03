package com.fitnote.backend.admin;

import com.fitnote.backend.common.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminBigScreenController {

    private final BigScreenService bigScreenService;

    public AdminBigScreenController(BigScreenService bigScreenService) {
        this.bigScreenService = bigScreenService;
    }

    @GetMapping("/big-screen")
    public ApiResponse<Map<String, Object>> screen() {
        return ApiResponse.ok(bigScreenService.getBigScreen());
    }
}
