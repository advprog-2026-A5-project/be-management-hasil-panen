package id.ac.ui.cs.advprog.hasilpanen.controller;

import id.ac.ui.cs.advprog.hasilpanen.service.DuplicateHarvestSubmissionException;
import id.ac.ui.cs.advprog.hasilpanen.service.AuthenticationRequiredException;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestNotFoundException;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestTerminalStatusException;
import id.ac.ui.cs.advprog.hasilpanen.service.MandorUnauthorizedAccessException;
import id.ac.ui.cs.advprog.hasilpanen.service.RoleForbiddenException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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

    @GetMapping("/test/role-forbidden")
    String roleForbidden() {
        throw new RoleForbiddenException("wrong role");
    }

    @GetMapping("/test/auth-required")
    String authRequired() {
        throw new AuthenticationRequiredException("missing auth");
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

    @GetMapping("/test/illegal-arg")
    String illegalArgument() {
        throw new IllegalArgumentException("bad input");
    }

    @GetMapping("/test/data-conflict")
    String dataConflict() {
        throw new DataIntegrityViolationException("duplicate key");
    }

    @GetMapping("/test/upload-too-large")
    String uploadTooLarge() {
        throw new MaxUploadSizeExceededException(1024);
    }

    @GetMapping("/test/internal")
    String internal() {
        throw new RuntimeException("boom");
    }

    record TestBody(@NotBlank String name) {
    }
}
