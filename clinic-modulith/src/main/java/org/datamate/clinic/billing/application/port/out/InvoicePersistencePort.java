package org.datamate.clinic.billing.application.port.out;

import org.datamate.clinic.billing.domain.model.Invoice;

/**
 * Outbound port for Invoice persistence operations.
 * Implemented by adapters in the infrastructure layer.
 */
public interface InvoicePersistencePort {
    Invoice findById(String id);
    void save(Invoice invoice);
}
