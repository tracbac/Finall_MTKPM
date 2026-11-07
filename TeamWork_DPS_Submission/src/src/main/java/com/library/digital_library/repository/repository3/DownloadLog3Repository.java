package com.library.digital_library.repository.repository3;

import com.library.digital_library.model.model3.DownloadLog3;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DownloadLog3Repository extends JpaRepository<DownloadLog3, Long> {
    List<DownloadLog3> findByUserIdOrderByRequestedAtDesc(Long userId);
    List<DownloadLog3> findByLicenseKey(String licenseKey);
}
