package com.smartrental.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryService {

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${cloudinary.folder:smart-rental}")
    private String folder;

    private Cloudinary cloudinary;
    private boolean configured;

    @PostConstruct
    public void init() {
        if (cloudName == null || cloudName.isBlank()
                || apiKey == null || apiKey.isBlank()
                || apiSecret == null || apiSecret.isBlank()) {
            log.warn("Cloudinary not configured — file upload to cloud will be skipped");
            configured = false;
            return;
        }
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
        configured = true;
        log.info("Cloudinary initialized for cloud: {}", cloudName);
    }

    public Map upload(MultipartFile file, String subfolder) throws IOException {
        if (!configured) {
            log.warn("Cloudinary not configured — returning empty upload result");
            return ObjectUtils.asMap("url", "", "public_id", "");
        }
        String path = (subfolder != null) ? folder + "/" + subfolder : folder;
        Map result = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", path));
        log.debug("Uploaded {} to Cloudinary, public_id: {}", file.getOriginalFilename(), result.get("public_id"));
        return result;
    }

    public void delete(String publicId) throws IOException {
        if (!configured || publicId == null || publicId.isBlank()) {
            log.warn("Cloudinary not configured or no publicId — skipping delete");
            return;
        }
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        log.debug("Deleted Cloudinary file: {}", publicId);
    }

}
