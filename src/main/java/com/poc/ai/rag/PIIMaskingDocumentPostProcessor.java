package com.poc.ai.rag;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.regex.Pattern;

@Log4j2
@Component
@NoArgsConstructor
public class PIIMaskingDocumentPostProcessor implements DocumentPostProcessor {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "\\b\\(?\\d{2}\\)?[\\s-]?9?\\d{4}[\\s-]?\\d{4}\\b");

    private static final Pattern CPF_PATTERN = Pattern.compile(
            "\\b\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}\\b");

    private static final String EMAIL_REPLACEMENT = "[REDACTED_EMAIL]";
    private static final String PHONE_REPLACEMENT = "[REDACTED_PHONE]";
    private static final String CPF_REPLACEMENT = "[REDACTED_CPF]";

    @Override
    @NonNull
    public List<Document> process(@NonNull Query query, @NonNull List<Document> documents) {
        Assert.notNull(query, "Query cannot be null");
        Assert.notNull(documents, "Documents list cannot be null");

        if (CollectionUtils.isEmpty(documents)) {
            return documents;
        }

        if (log.isDebugEnabled()) {
            log.debug("Executing PII masking on {} documents for query: '{}'",
                    documents.size(), query.text());
        }

        return documents.stream()
                .map(this::maskDocument)
                .toList();
    }

    private Document maskDocument(Document document) {
        String text = document.getText();

        if (text == null || text.isEmpty()) {
            return document;
        }

        String maskedText = maskSensitiveInformation(text);

        if (!maskedText.equals(text)) {
            return document.mutate()
                    .text(maskedText)
                    .metadata("pii_masked", true)
                    .build();
        }

        return document;
    }

    private String maskSensitiveInformation(String text) {
        String result = EMAIL_PATTERN.matcher(text).replaceAll(EMAIL_REPLACEMENT);
        result = PHONE_PATTERN.matcher(result).replaceAll(PHONE_REPLACEMENT);
        result = CPF_PATTERN.matcher(result).replaceAll(CPF_REPLACEMENT);
        return result;
    }
}