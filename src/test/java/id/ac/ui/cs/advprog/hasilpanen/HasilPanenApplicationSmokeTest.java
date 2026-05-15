package id.ac.ui.cs.advprog.hasilpanen;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class HasilPanenApplicationSmokeTest {

    @Test
    void standaloneHasilPanenApplicationClassExists() {
        assertDoesNotThrow(() -> Class.forName("id.ac.ui.cs.advprog.hasilpanen.HasilPanenApplication"));
    }
}
