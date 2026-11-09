package com.library.digital_library.controller;

import com.library.digital_library.dto.EBookRequestDecision3;
import com.library.digital_library.dto.EBookRequestResponse3;
import com.library.digital_library.dto.EBookApprovalSettings3;
import jakarta.validation.Valid;
import com.library.digital_library.service.service3.EBookBorrowRequest3Service;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ebooks/requests")
@RequiredArgsConstructor
public class AdminEBookBorrowRequest3Controller {

    private final EBookBorrowRequest3Service requestService;

    @GetMapping
    public ResponseEntity<List<EBookRequestResponse3>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests().stream()
                .map(EBookRequestResponse3::from)
                .toList());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<EBookRequestResponse3> approveRequest(
            @PathVariable Long id,
            @Valid @RequestBody EBookApprovalSettings3 settings) {
        return ResponseEntity.ok(EBookRequestResponse3.from(
                requestService.approveRequest(id, settings.validDays(), settings.maxDownloads())));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<EBookRequestResponse3> rejectRequest(
            @PathVariable Long id,
            @RequestBody(required = false) EBookRequestDecision3 decision) {
        return ResponseEntity.ok(EBookRequestResponse3.from(
                requestService.rejectRequest(id, decision == null ? null : decision.note())));
    }
}
