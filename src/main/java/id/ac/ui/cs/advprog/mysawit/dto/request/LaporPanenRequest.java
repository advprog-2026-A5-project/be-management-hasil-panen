package id.ac.ui.cs.advprog.mysawit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaporPanenRequest {
    private Double hasilPanenKg;
    private String beritaPanen;
    private List<String> fotoUrls;
}
