// WebSocketTestController.java
package com.neuedu.system.demoai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class WebSocketTestController {

    @GetMapping("/ws-status")
    public String testWebSocket() {
        return "WebSocket服务运行正常";
    }

    @GetMapping("/port")
    public String getPort() {
        return "当前端口: 9091";
    }
}