package org.datamate.finance.controller;

import org.datamate.finance.application.JournalService;
import org.datamate.finance.controller.dto.CreateJournalRequest;
import org.datamate.finance.domain.Journal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/journals")
public class JournalController {

    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    /**
     * Standard REST Controller that accepts a simple HTTP Payload DTO.
     * The Controller has NO authorization annotations because security
     * belongs in the Domain/Application layer (Hexagonal Architecture).
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createJournal(@RequestBody CreateJournalRequest payload) {
        
        // 1. Map DTO to Application Command
        Journal command = new Journal();
        command.setAmount(payload.getAmount());
        command.setDepartment(payload.getDepartment());
        command.setCostCenter(payload.getCostCenter());
        command.setStatus(payload.getStatus());
        command.setRequiresAudit(payload.getRequiresAudit());
        command.setTransactionDate(payload.getTransactionDate());

        // 2. Call the Application Service
        // THE PEP WILL INTERCEPT THIS EXACT LINE OF CODE!
        String result = journalService.createJournal(command);

        // 3. Return success
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", result
        ));
    }
}
