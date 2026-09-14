package org.datamate.pharmacy.adapter.out.persistence.adapter;

import org.datamate.pharmacy.adapter.out.persistence.entity.DoctorJpaEntity;
import org.datamate.pharmacy.adapter.out.persistence.mapper.DoctorJpaMapper;
import org.datamate.pharmacy.adapter.out.persistence.repository.DoctorJpaRepository;
import org.datamate.pharmacy.application.port.out.DoctorQueryPort;
import org.datamate.pharmacy.domain.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class DoctorPersistenceAdapter implements DoctorQueryPort {

    private final DoctorJpaRepository doctorJpaRepository;

    public DoctorPersistenceAdapter(DoctorJpaRepository doctorJpaRepository) {
        this.doctorJpaRepository = doctorJpaRepository;
    }

    @Override
    public Page<Doctor> findActiveDoctors(Pageable pageable) {
        return doctorJpaRepository.findByActiveTrue(pageable).map(DoctorJpaMapper::toDomain);
    }

    @Override
    public Page<Doctor> searchActiveDoctors(String search, Pageable pageable) {
        return doctorJpaRepository.findByActiveTrueAndNameContainingIgnoreCase(search, pageable).map(DoctorJpaMapper::toDomain);
    }
}
