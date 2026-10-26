package com.library.digital_library.controller.controller3;

import com.library.digital_library.model.model3.DrmLicense3;
import com.library.digital_library.model.model3.IssueDrmRequest3;
import com.library.digital_library.service.service3.DrmLicenseService3;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller Cấp phép bản quyền số & quản lý DRM (Thành viên 3)
 */
@RestController
@RequestMapping("/api/v3/drm")
@RequiredArgsConstructor
public class DrmLicenseController3 {

    private final DrmLicenseService3 drmLicenseService;

    /**
     * API 1: Cấp phép bản quyền số DRM cho User
     */
    @PostMapping("/issue")
    public ResponseEntity<?> issueLicense(@RequestBody IssueDrmRequest3 request) {
        try {
            DrmLicense3 license = drmLicenseService.issueLicense(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(license);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * API 2: Lấy danh sách giấy phép DRM của User
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DrmLicense3>> getUserLicenses(@PathVariable Long userId) {
        return ResponseEntity.ok(drmLicenseService.getUserLicenses(userId));
    }

    /**
     * API 3: Xem thông tin chi tiết của 1 license
     */
    @GetMapping("/license/{licenseKey}")
    public ResponseEntity<?> getLicenseByKey(@PathVariable String licenseKey) {
        try {
            DrmLicense3 license = drmLicenseService.getLicenseByKey(licenseKey);
            return ResponseEntity.ok(license);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * API 4: Thu hồi giấy phép DRM
     */
    @PostMapping("/revoke")
    public ResponseEntity<?> revokeLicense(@RequestBody Map<String, String> payload) {
        String licenseKey = payload.get("licenseKey");
        String reason = payload.getOrDefault("reason", "Admin thu hồi bản quyền");
        try {
            DrmLicense3 license = drmLicenseService.revokeLicense(licenseKey, reason);
            return ResponseEntity.ok(license);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
