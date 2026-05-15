package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.repository.HarvestReportRepository;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Supplier;

public class CreateHarvestService {

    private final HarvestReportRepository repository;
    private final Supplier<LocalDate> dateSupplier;

    public CreateHarvestService(HarvestReportRepository repository, Supplier<LocalDate> dateSupplier) {
        this.repository = repository;
        this.dateSupplier = dateSupplier;
    }

    public HarvestReport createHarvest(CreateHarvestCommand command) {
        LocalDate today = dateSupplier.get();
        synchronized ((command.buruhId().toString() + today).intern()) {
            if (repository.findByBuruhIdAndHarvestDate(command.buruhId(), today).isPresent()) {
                throw new DuplicateHarvestSubmissionException("harvest already submitted for today");
            }

            HarvestReport report = HarvestReport.submit(
                    UUID.randomUUID(),
                    command.buruhId(),
                    today,
                    command.kilogram(),
                    command.reportText(),
                    command.photos());

            return repository.save(report);
        }
    }
}
