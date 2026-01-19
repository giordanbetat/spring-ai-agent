package com.poc.ai.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Component
@Slf4j
@RequiredArgsConstructor
public class GithubClient {

    public static final String ITEMS = "items";
    private final RestClient githubRestClient;
    private static final String SEARCH_PATH = "/search/repositories";
    private static final int PAGE_SIZE = 10;

    private static final List<String> EXCLUDED_KEYWORDS = List.of(
            "book", "livro", "course", "curso", "tutorial",
            "interview", "entrevista", "roadmap", "cheatsheet",
            "collection", "learning", "study"
    );

    public String searchRepositories(String query) {
        String cleanQuery = sanitizeQuery(query);
        log.info("Calling GitHub API for query: '{}'", query);

        try {
            var root = githubRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(SEARCH_PATH)
                            .queryParam("q", cleanQuery)
                            .queryParam("sort", "stars")
                            .queryParam("order", "desc")
                            .queryParam("per_page", PAGE_SIZE)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) ->
                            log.error("GitHub API Client Error: {}", res.getStatusCode())
                    )
                    .body(JsonNode.class);

            return formatResponse(root);

        } catch (Exception e) {
            log.error("Error interacting with GitHub API", e);
            return "Unable to search GitHub at the moment. Error: " + e.getMessage();
        }
    }

    private String formatResponse(JsonNode root) {
        if (root == null || !root.has(ITEMS) || root.get(ITEMS).isEmpty()) {
            return "No repositories found matching your criteria.";
        }

        var summaries = new ArrayList<String>();

        StreamSupport.stream(root.get(ITEMS).spliterator(), false)
                .filter(this::isSoftwareLibrary)
                .limit(5)
                .forEach(item -> {
                    var name = item.path("full_name").asText("Unknown");
                    int stars = item.path("stargazers_count").asInt(0);
                    var desc = truncate(item.path("description").asText("No description"));
                    var url = item.path("html_url").asText("#");

                    summaries.add(String.format("- **%s** (%d): %s [Link](%s)", name, stars, desc, url));
                });

        return String.join("\n", summaries);
    }

    private String sanitizeQuery(String query) {
        if (query == null) return "java";
        String clean = query.toLowerCase()
                .replace("popular", "")
                .replace("best", "")
                .replace("top", "")
                .replace("library", "")
                .trim();
        return clean.isEmpty() ? query : clean;
    }

    private boolean isSoftwareLibrary(JsonNode item) {
        var name = item.path("full_name").asText("").toLowerCase();
        var description = item.path("description").asText("").toLowerCase();

        var isSpam = EXCLUDED_KEYWORDS.stream()
                .anyMatch(keyword -> name.contains(keyword) || description.contains(keyword));

        return !isSpam;
    }

    private String truncate(String text) {
        if (text == null || text.length() <= 100) {
            return text;
        }
        return text.substring(0, 100) + "...";
    }
}