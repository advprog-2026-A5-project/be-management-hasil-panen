package id.ac.ui.cs.advprog.hasilpanen.controller;

import id.ac.ui.cs.advprog.hasilpanen.service.DuplicateHarvestSubmissionException;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestNotFoundException;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestTerminalStatusException;
import id.ac.ui.cs.advprog.hasilpanen.service.MandorUnauthorizedAccessException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestErrorController {

    @PostMapping("/test/validation")
    String validation(@Valid @RequestBody TestBody body) {
        return "ok";
    }

    @GetMapping("/test/unauthorized-mandor")
    String unauthorizedMandor() {
        throw new MandorUnauthorizedAccessException("no access");
    }

    @GetMapping("/test/duplicate")
    String duplicate() {
        throw new DuplicateHarvestSubmissionException("duplicate");
    }

    @GetMapping("/test/terminal")
    String terminal() {
        throw new HarvestTerminalStatusException("terminal");
    }

    @GetMapping("/test/not-found")
    String notFound() {
        throw new HarvestNotFoundException("missing");
    }

    record TestBody(@NotBlank String name) {
    }
}
