package com.poc.ai.service;

import com.poc.ai.dto.ChatRequestDto;
import com.poc.ai.dto.ChatResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MentorChatService {

    private final ChatClient chatClient;
    private final GithubService githubService;
    private final VectorStore vectorStore;

    @Value("${chat.client.targetLanguage}")
    private String targetLanguage;

    @Value("${chat.client.topK:3}")
    private int topK;

    @Value("${chat.client.similarityThreshold:0.7}")
    private Double similarityThreshold;

    public ChatResponseDto sendMessage(ChatRequestDto dto, String username) {

        var contextData = retrieveContext(dto.message());

        var response = chatClient
                .prompt()
                .system(s -> s
                        .param("name", username)
                        .param("language", targetLanguage)
                )
                .user(u -> u
                        .param("context", contextData)
                        .param("user_message", dto.message())
                        .param("language", targetLanguage)
                )
                .advisors(a -> a.param("chat_memory_conversation_id", username))
                .tools(githubService)
                .call()
                .content();

        return new ChatResponseDto(buildResponse(response));
    }

    private String retrieveContext(String query) {
        try {
            List<Document> documents = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(query)
                            .topK(topK)
                            .similarityThreshold(similarityThreshold)
                            .build()
            );

            if (documents == null || documents.isEmpty()) {
                return "No internal context found. Use General Knowledge or Tools.";
            }

            return documents.stream()
                    .map(Document::getFormattedContent)
                    .collect(Collectors.joining("\n\n"));

        } catch (Exception e) {
            log.error("Error retrieving context from VectorStore", e);
            return "Context unavailable due to internal error.";
        }
    }

    private String buildResponse(String response) {
        return response != null ? response : "I'm sorry, I couldn't process that. Let's try again.";
    }
}