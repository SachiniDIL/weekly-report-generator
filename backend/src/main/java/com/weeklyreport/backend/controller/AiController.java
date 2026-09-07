package com.weeklyreport.backend.controller;

import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.dto.AiChatRequest;
import com.weeklyreport.backend.dto.MessageResponse;
import com.weeklyreport.backend.dto.ReportListFilters;
import com.weeklyreport.backend.service.AiService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The AI chat assistant. Both endpoints answer purely from the reports matching the supplied
 * dashboard filter — no chat memory, no persistence.
 */
@RestController
@RequestMapping("/ai")
@PreAuthorize("hasRole('MANAGER')")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public MessageResponse chat(
            @AuthenticationPrincipal User manager, @Valid @RequestBody AiChatRequest request) {
        ReportListFilters filter = new ReportListFilters(
                request.projectId(), null, request.weekStart(), request.weekEnd(), request.userId());
        return new MessageResponse(aiService.chat(manager, request.question(), filter));
    }

    @GetMapping("/summary")
    public MessageResponse summary(
            @AuthenticationPrincipal User manager,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd,
            @RequestParam(required = false) Long projectId) {
        ReportListFilters filter = new ReportListFilters(projectId, null, weekStart, weekEnd, null);
        return new MessageResponse(aiService.summary(manager, filter));
    }
}
