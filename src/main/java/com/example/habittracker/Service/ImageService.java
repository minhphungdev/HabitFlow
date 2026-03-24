package com.example.habittracker.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

@Service
public class ImageService {
    @Value("${cloudinary.cloud_name}")
    private String cloudName;
    @Value("${cloudinary.api_key}")
    private String apiKey;
    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }
    @Transactional
    public String saveImage(MultipartFile image, String photoFolder) throws IOException {
        if (image == null || image.isEmpty()) {
            return null; // Không có ảnh được tải lên
        }

        try {
            // Chuyển MultipartFile thành File để Cloudinary có thể upload
            // Hoặc có thể upload trực tiếp từ InputStream nếu thư viện hỗ trợ
            File tempFile = Files.createTempFile("upload", image.getOriginalFilename()).toFile();
            image.transferTo(tempFile);

            // Tải ảnh lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(tempFile,
                    ObjectUtils.asMap("folder", "habittracker/" + photoFolder)); // Đặt tên thư mục trên Cloudinary

            // Xóa tệp tạm thời sau khi upload
            tempFile.delete();

            // Trả về URL an toàn của ảnh từ Cloudinary
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu ảnh lên Cloudinary: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String saveImageFromUrl(String imageUrl, String photoFolder) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null; // Không có URL
        }

        try {
            // Tải ảnh từ URL lên Cloudinary trực tiếp
            Map uploadResult = cloudinary.uploader().upload(imageUrl,
                    ObjectUtils.asMap("folder", "habittracker/" + photoFolder));

            // Trả về URL an toàn của ảnh từ Cloudinary
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu avatar từ URL lên Cloudinary: " + e.getMessage(), e);
        }
    }

}
