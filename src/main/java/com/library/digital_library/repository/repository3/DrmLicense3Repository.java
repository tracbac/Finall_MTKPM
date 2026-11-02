package com.library.digital_library.repository.repository3;

import com.library.digital_library.model.model3.DrmLicense3;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrmLicense3Repository extends JpaRepository<DrmLicense3, Long> {
    Optional<DrmLicense3> findByLicenseKey(String licenseKey);
    List<DrmLicense3> findByUserId(Long userId);
    List<DrmLicense3> findByUserIdAndBookId(Long userId, Long bookId);
    Optional<DrmLicense3> findFirstByUserIdAndBookIdAndStatus(Long userId, Long bookId, String status);
}
