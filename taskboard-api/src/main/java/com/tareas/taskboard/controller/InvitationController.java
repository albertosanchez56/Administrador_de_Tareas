package com.tareas.taskboard.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tareas.taskboard.dto.InvitationResponse;
import com.tareas.taskboard.service.InvitationService;

import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @GetMapping
    public ResponseEntity<List<InvitationResponse>> listPending() {
        List<InvitationResponse> response = invitationService.listPending(getAuthenticatedUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<InvitationResponse> accept(@PathVariable Long invitationId) {
        InvitationResponse response = invitationService.accept(invitationId, getAuthenticatedUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{invitationId}/reject")
    public ResponseEntity<InvitationResponse> reject(@PathVariable Long invitationId) {
        InvitationResponse response = invitationService.reject(invitationId, getAuthenticatedUserId());
        return ResponseEntity.ok(response);
    }

    private Long getAuthenticatedUserId() {
        return Long.valueOf(
                SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
