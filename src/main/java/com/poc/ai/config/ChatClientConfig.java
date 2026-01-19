package com.poc.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ChatClientConfig {

    @Value("${chat.client.modelName}")
    private String modelName;
    @Value("${chat.client.temperature}")
    private Double temperature;
    @Value("${chat.client.maxMessages}")
    private int maxMessages;

    @Value("classpath:promptTemplates/systemPromptTemplate.st")
    private Resource systemPromptResource;

    @Value("classpath:promptTemplates/userPromptTemplate.st")
    private Resource userPromptResource;

    @Bean
    ChatClient mentorChatClient(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory
    ) {
        var loggerAdvisor = new SimpleLoggerAdvisor();

        var memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        var chatOptions = OllamaOptions.builder()
                .model(modelName)
                .temperature(temperature)
                .build();

        return chatClientBuilder
                .defaultOptions(chatOptions)
                .defaultAdvisors(loggerAdvisor, memoryAdvisor)
                .defaultSystem(systemPromptResource)
                .defaultUser(userPromptResource)
                .build();
    }

    @Bean
    ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .maxMessages(maxMessages)
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .build();
    }
}