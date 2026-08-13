package org.datamate.pharmacy.application.service;

import lombok.RequiredArgsConstructor;
import org.datamate.pharmacy.adapter.out.persistence.DoctorRepository;
import org.datamate.authz.rest.dto.AllowedValuePageResponse;
import org.datamate.authz.rest.dto.AllowedValueResponse;
import org.datamate.pharmacy.application.port.in.GetDoctorsUseCase;
import org.datamate.pharmacy.domain.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetDoctorsService implements GetDoctorsUseCase {

    private final DoctorRepository doctorRepository;

    @Override
    @Transactional(readOnly = true)
    public AllowedValuePageResponse execute(int page, int size, String search) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("name").ascending()
        );

        Page<Doctor> doctors;

        if (search == null || search.isBlank()) {
            doctors = doctorRepository.findByActiveTrue(pageable);
        } else {
            doctors = doctorRepository.findByActiveTrueAndNameContainingIgnoreCase(search, pageable);
        }

        List<AllowedValueResponse> content = doctors
                .getContent()
                .stream()
                .map(doctor -> new AllowedValueResponse(
                        doctor.getId(),
                        doctor.getName()
                ))
                .collect(Collectors.toList());

        return new AllowedValuePageResponse(
                content,
                doctors.getNumber(),
                doctors.getSize(),
                doctors.getTotalElements(),
                doctors.isLast()
        );
    }
}
