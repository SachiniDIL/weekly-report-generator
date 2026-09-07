package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.dto.ReportListFilters;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Turns a manager's question (or a fixed summary request) into an AI answer: build the report
 * context for the active dashboard filter, wrap it in a prompt, hand it to {@link GeminiService}.
 * Manager-scoped, since the context can span the whole team.
 */
@Service
@PreAuthorize("hasRole('MANAGER')")
public class AiService {

    private final ReportContextBuilder contextBuilder;
    private final GeminiService geminiService;

    public AiService(ReportContextBuilder contextBuilder, GeminiService geminiService) {
        this.contextBuilder = contextBuilder;
        this.geminiService = geminiService;
    }

    public String chat(User manager, String question, ReportListFilters filter) {
        String context = contextBuilder.buildDigest(manager, filter);
        return geminiService.generate(chatPrompt(question, context));
    }

    public String summary(User manager, ReportListFilters filter) {
        String context = contextBuilder.buildDigest(manager, filter);
        return geminiService.generate(summaryPrompt(context));
    }

    private static String chatPrompt(String question, String context) {
        return """
                You are an assistant helping an engineering manager review their team's weekly reports.
                Answer the question using only the information in the CONTEXT below. If the answer is \
                not in the context, say so explicitly — do not guess or use outside knowledge.

                CONTEXT:
                %s

                QUESTION:
                %s
                """
                .formatted(context, question);
    }

    private static String summaryPrompt(String context) {
        return """
                You are an assistant helping an engineering manager review their team's weekly reports.
                Using only the CONTEXT below, write a concise summary under exactly these three headings:

                Completed work
                Recurring blockers
                Workload imbalances

                Base every statement only on the context. If a heading has nothing to report, say that \
                under the heading rather than inventing detail.

                CONTEXT:
                %s
                """
                .formatted(context);
    }
}
