package org.datamate.pharmacy.application.port.out;

import org.datamate.pharmacy.domain.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Outbound port for querying doctor data.
 * Implemented by the persistence adapter in the adapter layer.
 */
public interface DoctorQueryPort {
    Page<Doctor> findActiveDoctors(Pageable pageable);
    Page<Doctor> searchActiveDoctors(String search, Pageable pageable);
}
