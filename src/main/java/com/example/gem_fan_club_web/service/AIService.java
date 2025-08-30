package com.example.gem_fan_club_web.service;

import com.example.gem_fan_club_web.dto.AiChatRequest;
import com.example.gem_fan_club_web.dto.AiResponse;
import com.example.gem_fan_club_web.dto.ChatMessage; // 引入我们创建的DTO
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class AIService {


    private final RestTemplate restTemplate;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    // 通过构造函数注入RestTemplate
    public AIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 调用Python AI服务获取回答 (已升级为支持历史记录)
     * @param question 用户的最新问题
     * @param history 聊天历史记录
     * @return AI的回答
     */
    public String getAnswer(String question, List<ChatMessage> history) { // 参数类型改为List<ChatMessage>以获得更好的类型安全

        // 1. 创建并填充请求体对象
        AiChatRequest requestPayload = new AiChatRequest();
        requestPayload.setQuestion(question);
        requestPayload.setHistory(history);

        log.info("正在向AI服务 [{}] 发送请求...", aiServiceUrl);

        try {
            // 2. 使用RestTemplate发起POST请求
            // 第一个参数是URL，第二个是请求体，第三个是期望返回的Java类型
            AiResponse response = restTemplate.postForObject(aiServiceUrl, requestPayload, AiResponse.class);
            // 3. 处理响应
            if (response != null && response.getAnswer() != null) {
                log.info("成功接收到AI服务的响应。{}", response.getAnswer());
                return response.getAnswer();
            } else {
                log.warn("AI服务返回了空的响应。");
                return "抱歉，AI好像没有给我回应耶...";
            }
        } catch (RestClientException e) {
            // 4. 健壮的异常处理，捕获网络错误、服务不可用等问题
            log.error("调用AI服务失败: {}", e.getMessage());
            return "哎呀，我和我的AI小伙伴暂时失联了，请稍后再试试吧！";
        }
    }
}