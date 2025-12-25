// 修改 WebSocketConfig.java
package com.neuedu.system.demoai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
public class WebSocketConfig {

    /**
     * 注入ServerEndpointExporter，
     * 这个bean会自动注册使用@ServerEndpoint注解声明的websocket endpoint
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 增加WebSocket连接配置，设置缓冲区大小
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // 设置文本消息缓冲区大小
        container.setMaxTextMessageBufferSize(8192000);
        // 设置二进制消息缓冲区大小
        container.setMaxBinaryMessageBufferSize(8192000);
        // 设置空闲超时时间（毫秒）
        container.setMaxSessionIdleTimeout(600000L);
        return container;
    }
}