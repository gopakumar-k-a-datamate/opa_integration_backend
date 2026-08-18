package org.datamate.clinic.billing.adapter.out;

import org.datamate.clinic.billing.application.port.out.InvoicePersistencePort;
import org.datamate.clinic.billing.domain.model.Invoice;
import org.springframework.stereotype.Component;

/**
 * Infrastructure implementation of the InvoicePersistencePort.
 * A true "Clean Architecture" adapter.
 */
@Component
public class InvoicePersistenceAdapter implements InvoicePersistencePort {

    // Normally you would inject your Spring Data JPA Repository here, e.g.:
    // private final InvoiceJpaRepository repository;

    @Override
    public Invoice findById(String id) {
        // Stub implementation simulating a database fetch
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setTotalAmount(100.0);
        invoice.setIsPaid(false);
        invoice.setAccountType("STANDARD");
        invoice.setInvoiceType("CONSULTATION");
        invoice.setDepartment("ORTHOPEDICS"); // This DB context is crucial for ABAC!
        return invoice;
    }

    @Override
    public void save(Invoice invoice) {
        // Stub implementation simulating a database save
        System.out.println("Successfully saved invoice to database: " + invoice.getId());
    }
}
