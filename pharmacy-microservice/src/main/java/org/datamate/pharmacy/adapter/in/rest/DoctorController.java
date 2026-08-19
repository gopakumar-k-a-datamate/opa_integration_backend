package org.datamate.pharmacy.adapter.in.rest;


import org.datamate.authz.rest.dto.AllowedValuePageResponse;
import org.datamate.pharmacy.application.port.in.GetDoctorsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pharmacy/doctors")
public class DoctorController {

    private final GetDoctorsUseCase getDoctorsUseCase;

    public DoctorController(GetDoctorsUseCase getDoctorsUseCase) {
        this.getDoctorsUseCase = getDoctorsUseCase;
    }

    @GetMapping
    public AllowedValuePageResponse getDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }

        return getDoctorsUseCase.execute(page, size, search);
    }
}
