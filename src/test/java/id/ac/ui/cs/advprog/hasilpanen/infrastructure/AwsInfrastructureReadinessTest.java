package id.ac.ui.cs.advprog.hasilpanen.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AwsInfrastructureReadinessTest {

    @Test
    void testInfrastructureReadinessArtifactsExist() {
        assertThat(Files.exists(Path.of("infrastructure"))).isTrue();
        assertThat(Files.exists(Path.of("infrastructure/terraform"))).isTrue();
        assertThat(Files.exists(Path.of("infrastructure/aws"))).isTrue();
        assertThat(Files.exists(Path.of("infrastructure/README.md"))).isTrue();
        assertThat(Files.exists(Path.of("infrastructure/terraform/main.tf"))).isTrue();
        assertThat(Files.exists(Path.of("infrastructure/terraform/variables.tf"))).isTrue();
        assertThat(Files.exists(Path.of("infrastructure/terraform/outputs.tf"))).isTrue();
    }
}
