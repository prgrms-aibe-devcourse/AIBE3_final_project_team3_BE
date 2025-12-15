package triplestar.mixchat.domain.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import triplestar.mixchat.domain.report.entity.Report;

public interface  ReportRepository extends JpaRepository<Report, Long> {
}
