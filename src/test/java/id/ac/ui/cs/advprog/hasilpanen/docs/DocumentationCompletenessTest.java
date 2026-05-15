package id.ac.ui.cs.advprog.hasilpanen.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DocumentationCompletenessTest {

    @Test
    void testRequiredDocumentationFilesExistAndAreNotEmpty() throws Exception {
        assertDoc("docs/ARCHITECTURE.md");
        assertDoc("docs/API.md");
        assertDoc("docs/CONCURRENCY.md");
        assertDoc("docs/DEPLOYMENT.md");
        assertDoc("docs/INTEGRATION.md");
    }

    private void assertDoc(String path) throws Exception {
        Path file = Path.of(path);
        assertThat(Files.exists(file)).isTrue();
        assertThat(Files.size(file)).isGreaterThan(0L);
    }
}
