package id.ac.ui.cs.advprog.hasilpanen.controller;

import id.ac.ui.cs.advprog.hasilpanen.service.TransportEligibilityResult;
import id.ac.ui.cs.advprog.hasilpanen.service.TransportEligibilityService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/harvests")
public class InternalHarvestController {

    private final TransportEligibilityService transportEligibilityService;

    public InternalHarvestController(TransportEligibilityService transportEligibilityService) {
        this.transportEligibilityService = transportEligibilityService;
    }

    @GetMapping("/{harvestId}/transport-eligibility")
    public TransportEligibilityResult getTransportEligibility(@PathVariable UUID harvestId) {
        return transportEligibilityService.getEligibility(harvestId);
    }
}
