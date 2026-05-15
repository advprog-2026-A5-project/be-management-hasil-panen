package id.ac.ui.cs.advprog.mysawit.dto.response;

import id.ac.ui.cs.advprog.mysawit.dto.request.PanenResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanenListResponse {
    private List<PanenResponse> panenList;
    private long totalItems;
    private int totalPages;
    private int currentPage;
}
