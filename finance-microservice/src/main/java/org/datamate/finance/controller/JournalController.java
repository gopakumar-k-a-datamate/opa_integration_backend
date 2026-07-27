package org.datamate.finance.controller;

import org.datamate.finance.application.JournalService;
import org.datamate.finance.controller.dto.CreateJournalRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/journals")
public class JournalController {

    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createJournal(@RequestBody CreateJournalRequest payload) {
        return journalService.createJournal(payload);
    }
}
