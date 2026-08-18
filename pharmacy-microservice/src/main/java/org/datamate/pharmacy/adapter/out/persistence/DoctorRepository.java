package org.datamate.pharmacy.adapter.out.persistence;

import org.datamate.pharmacy.domain.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, String> {

    Page<Doctor> findByActiveTrue(Pageable pageable);

    Page<Doctor> findByActiveTrueAndNameContainingIgnoreCase(String search, Pageable pageable);
}
