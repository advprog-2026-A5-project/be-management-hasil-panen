package id.ac.ui.cs.advprog.mysawit.dto.request;

import id.ac.ui.cs.advprog.mysawit.model.StatusPanen;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanenResponse {
    private Long id;
    private Double hasilPanenKg;
    private String beritaPanen;
    private List<String> urlFoto;
    private LocalDateTime tanggalPanen;
    private StatusPanen status;
    private String alasanPenolakan;

    private Long buruhId;
    private String buruhUsername;

    private Long mandorId;
    private String mandorUsername;
}
