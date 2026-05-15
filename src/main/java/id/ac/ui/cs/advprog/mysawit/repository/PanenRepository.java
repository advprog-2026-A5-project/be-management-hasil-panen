package id.ac.ui.cs.advprog.mysawit.repository;

import id.ac.ui.cs.advprog.mysawit.model.Panen;
import id.ac.ui.cs.advprog.mysawit.model.StatusPanen;
import id.ac.ui.cs.advprog.mysawit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PanenRepository extends JpaRepository<Panen, Long> {
    //Sudah di lapor hari itu belom
    boolean existsByBuruhAndTanggalPanenBetween(
        User buruh,
        LocalDateTime startOfDay,
        LocalDateTime endOfDay
    );

    List<Panen> findByBuruh(User buruh);

    List<Panen> findByBuruhAndTanggalPanenBetweenOrderByTanggalPanenDesc(
        User buruh,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    List<Panen> findByStatusOrderByTanggalPanenDesc(StatusPanen status);
}

