package com.library.digital_library.repository.repository3;

import com.library.digital_library.model.model3.DrmLicense3;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface DrmLicense3Repository extends JpaRepository<DrmLicense3, Long> {
    Optional<DrmLicense3> findByLicenseKey(String licenseKey);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select license from DrmLicense3 license where license.licenseKey = :licenseKey")
    Optional<DrmLicense3> findByLicenseKeyForUpdate(@Param("licenseKey") String licenseKey);
    List<DrmLicense3> findByUserId(Long userId);
    List<DrmLicense3> findByUserIdAndBookId(Long userId, Long bookId);
    Optional<DrmLicense3> findFirstByUserIdAndBookIdAndStatus(Long userId, Long bookId, String status);
}
