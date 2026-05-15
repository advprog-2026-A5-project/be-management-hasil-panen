package id.ac.ui.cs.advprog.mysawit.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "panen",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"buruh_id", "tanggal_panen"}
    ))
public class Panen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Double hasilPanenKg;
    @Column(nullable = false)
    private String beritaPanen;
    @ElementCollection
    @CollectionTable(name = "panen_foto",
        joinColumns = @JoinColumn(name = "panen_id"))
    @Column()
    private List<String> urlFoto = new ArrayList<>();
    @Column(nullable=false)
    private LocalDateTime tanggalPanen;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPanen status;
    @ManyToOne
    @JoinColumn(name = "buruh_id", nullable = false)
    private User buruh;
    @ManyToOne
    @JoinColumn(name = "mandor_id", nullable = false)
    private User mandor;
    @Column()
    private String alasanPenolakan;
}
