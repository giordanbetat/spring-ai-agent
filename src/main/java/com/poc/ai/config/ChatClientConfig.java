package com.poc.ai.config;

import com.poc.ai.rag.PIIMaskingDocumentPostProcessor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
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
    @Value("${chat.client.maxTokens}")
    private int maxTokens;
    @Value("${chat.client.topK}")
    private int topK;
    @Value("${chat.client.maxMessages}")
    private int maxMessages;
    @Value("${chat.client.similarityThreshold}")
    private Double similarityThreshold;

    @Value("classpath:promptTemplates/systemPromptTemplate.st")
    private Resource systemPromptResource;

    @Value("classpath:promptTemplates/userPromptTemplate.st")
    private Resource userPromptResource;

    @Bean
    ChatClient mentorChatClient(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            RetrievalAugmentationAdvisor retrievalAugmentationAdvisor
    ) {
        var loggerAdvisor = new SimpleLoggerAdvisor();

        var memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        var chatOptions = ChatOptions.builder()
                .model(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();

        return chatClientBuilder
                .defaultOptions(chatOptions)
                .defaultAdvisors(loggerAdvisor, memoryAdvisor, retrievalAugmentationAdvisor)
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

    @Bean
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(
            VectorStore vectorStore,
            PIIMaskingDocumentPostProcessor piiMaskingDocumentPostProcessor
    ) {

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(
                        VectorStoreDocumentRetriever.builder()
                                .vectorStore(vectorStore)
                                .topK(topK)
                                .similarityThreshold(similarityThreshold)
                                .build()
                )
                .documentPostProcessors(piiMaskingDocumentPostProcessor)
                .build();
    }
}