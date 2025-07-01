package com.example.movie_booking_backend.web.controller;
import com.example.movie_booking_backend.model.dto.BindRequestDTO;
import com.example.movie_booking_backend.model.vo.AuthResultVO;
import com.example.movie_booking_backend.service.IUsersService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class QQAuthController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper(); // 添加这行

    @Autowired
    private IUsersService usersService;

    private final String userInfoUrl = "https://qq.wch666.com/api/get_user_info.php";

    @GetMapping("/qq/callback")
    public ResponseEntity<?> qqCallback(@RequestParam String code) {
        try {
            // 1. 打印接收到的code
            System.out.println("Received QQ code: " + code);

            // 2. 获取QQ信息
            String json = restTemplate.getForObject(userInfoUrl + "?code=" + code, String.class);
            System.out.println("QQ API response: " + json); // 添加这行打印

            Map<String, Object> qqInfo = objectMapper.readValue(json, Map.class);

            // 3. 打印QQ信息
            System.out.println("Parsed QQ info: " + qqInfo);

            // 4. 检查绑定状态
            AuthResultVO result = usersService.checkQQBind(
                    (String) qqInfo.get("open_id"),
                    (String) qqInfo.get("nickname"),
                    (String) qqInfo.get("figureurl_qq_2")
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace(); // 确保错误被打印
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    @PostMapping("/bind-qq")
    public ResponseEntity<AuthResultVO> bindQQAccount(@RequestBody BindRequestDTO request) {
        // 打印接收到的请求数据
        System.out.println("绑定请求数据: " + request);

        AuthResultVO result = usersService.bindQQAccount(request);

        // 打印返回结果
        System.out.println("绑定结果: " + result);
        return ResponseEntity.ok(result);
    }
}