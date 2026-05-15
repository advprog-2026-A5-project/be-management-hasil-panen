package id.ac.ui.cs.advprog.hasilpanen.ci;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CiWorkflowContractTest {

    @Test
    void testCiWorkflowContainsRequiredPipelineSteps() throws IOException {
        String workflow = Files.readString(Path.of(".github/workflows/ci-cd.yml"));

        assertThat(workflow).contains("pull_request:");
        assertThat(workflow).contains("buildAndTest:");
        assertThat(workflow).contains("backend-analysis:");
        assertThat(workflow).contains("test --tests \"id.ac.ui.cs.advprog.hasilpanen.integration.*\"");
        assertThat(workflow).contains("test jacocoTestReport");
        assertThat(workflow).contains("jacocoTestReport");
        assertThat(workflow).contains("bootJar");
        assertThat(workflow).contains("min-coverage-overall: 80");
    }
}
