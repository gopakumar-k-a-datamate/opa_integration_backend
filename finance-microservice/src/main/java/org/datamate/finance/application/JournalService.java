package org.datamate.finance.application;

import org.datamate.finance.domain.Journal;
import org.springframework.stereotype.Service;

@Service
public class JournalService {

    /**
     * This method represents a core Use Case in the Application Layer.
     * Because the 'command' argument is of type Journal (which is annotated
     * with @PolicyResource), the PEP Aspect will intercept this method call
     * and query OPA before allowing execution to proceed.
     */
    public String createJournal(Journal command) {
        // Business logic to save the journal to the database would go here.
        return "Service execution successful. OPA approved the command!";
    }
}
