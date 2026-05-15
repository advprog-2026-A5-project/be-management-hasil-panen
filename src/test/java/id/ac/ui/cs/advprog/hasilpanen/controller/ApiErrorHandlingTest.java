package id.ac.ui.cs.advprog.hasilpanen.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import id.ac.ui.cs.advprog.hasilpanen.service.DuplicateHarvestSubmissionException;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestNotFoundException;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestTerminalStatusException;
import id.ac.ui.cs.advprog.hasilpanen.service.MandorUnauthorizedAccessException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ApiErrorHandlingTest.TestErrorController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApiErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testAPIReturns400ForInvalidBody() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    void testAPIReturns403ForUnauthorizedMandor() throws Exception {
        mockMvc.perform(get("/test/unauthorized-mandor"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void testAPIReturns409ForDuplicateSubmission() throws Exception {
        mockMvc.perform(get("/test/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void testAPIReturns409ForTerminalStatusConflict() throws Exception {
        mockMvc.perform(get("/test/terminal"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void testAPIReturns404ForNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @RestController
    static class TestErrorController {

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
    }

    record TestBody(@NotBlank String name) {
    }
}
