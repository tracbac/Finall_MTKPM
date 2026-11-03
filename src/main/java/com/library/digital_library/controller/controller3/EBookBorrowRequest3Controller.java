package com.library.digital_library.controller.controller3;

import com.library.digital_library.dto.CreateEBookRequest3;
import com.library.digital_library.dto.EBookRequestResponse3;
import com.library.digital_library.service.service3.EBookBorrowRequest3Service;
import jakarta.validation.Valid;
import java.security.Principal;
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
@RequestMapping("/api/v3/ebooks/requests")
@RequiredArgsConstructor
public class EBookBorrowRequest3Controller {

    private final EBookBorrowRequest3Service requestService;

    @PostMapping
    public ResponseEntity<EBookRequestResponse3> createRequest(
            @Valid @RequestBody CreateEBookRequest3 request,
            Principal principal) {
        return ResponseEntity.status(201)
                .body(EBookRequestResponse3.from(requestService.createRequest(principal.getName(), request.bookId())));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<EBookRequestResponse3>> getMyRequests(Principal principal) {
        return ResponseEntity.ok(requestService.getRequestsForUser(principal.getName()).stream()
                .map(EBookRequestResponse3::from)
                .toList());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<EBookRequestResponse3> cancelRequest(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(
                EBookRequestResponse3.from(requestService.cancelRequest(principal.getName(), id)));
    }
}
