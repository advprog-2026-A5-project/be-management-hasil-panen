package id.ac.ui.cs.advprog.hasilpanen.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class HarvestMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testMigrationCreatesHarvestReportsTable() {
        assertThat(tableExists("HARVEST_REPORTS")).isTrue();
    }

    @Test
    void testMigrationCreatesHarvestPhotosTable() {
        assertThat(tableExists("HARVEST_PHOTOS")).isTrue();
    }

    @Test
    void testMigrationEnforcesOneHarvestPerBuruhPerDate() {
        String sql = "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS "
                + "WHERE TABLE_NAME = 'HARVEST_REPORTS' AND CONSTRAINT_TYPE = 'UNIQUE'";
        List<String> constraints = jdbcTemplate.queryForList(sql, String.class);
        assertThat(constraints).isNotEmpty();
    }

    @Test
    void testMigrationCreatesOutboxEventsTable() {
        assertThat(tableExists("OUTBOX_EVENTS")).isTrue();
    }

    private boolean tableExists(String tableName) {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
        return count != null && count > 0;
    }
}
