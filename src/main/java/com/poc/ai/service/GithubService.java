package com.poc.ai.service;

import com.poc.ai.infrastructure.client.GithubClient;
import com.poc.ai.infrastructure.client.dto.GithubSearchRequestDto;
import com.poc.ai.infrastructure.client.dto.GithubSearchResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubService {
    private final GithubClient githubClient;

    @Tool(name = "githubSearchFunction", description = "Get repository stars and forks. Input: query string.")
    public GithubSearchResponseDto search(GithubSearchRequestDto request) {
        log.info("TOOL CALLED: Searching for '{}'", request.query());
        return new GithubSearchResponseDto(githubClient.searchRepositories(request.query()));
    }
}
