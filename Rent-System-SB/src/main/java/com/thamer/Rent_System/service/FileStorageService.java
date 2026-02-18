package com.thamer.Rent_System.service;

import com.thamer.Rent_System.model.UploadedFile;
import com.thamer.Rent_System.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import com.thamer.Rent_System.model.FileCategory;
import com.thamer.Rent_System.model.PropertyLocation;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FileStorageService {

    @Autowired
    private FileRepository fileRepository;

    // --- دالة مساعدة جديدة للحصول على اسم ملف مؤرخ ---
    private String createTimestampedFileName(String originalFilename) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = now.format(formatter);

        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return originalFilename + "_" + timestamp;
        }
        String name = originalFilename.substring(0, lastDotIndex);
        String extension = originalFilename.substring(lastDotIndex);

        return name + "_" + timestamp + extension;
    }

    public UploadedFile storeFile(MultipartFile file, String displayName, FileCategory category,
            PropertyLocation location) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        UploadedFile newFile = new UploadedFile();
        newFile.setFileName(displayName.trim().isEmpty() ? fileName : displayName.trim());
        newFile.setFileType(file.getContentType());
        newFile.setData(file.getBytes());
        newFile.setUploadDate(LocalDateTime.now()); // أو Instant.now() حسب نوع الحقل

        // حفظ المعلومات الجديدة
        newFile.setFileCategory(category);
        newFile.setLocation(location);

        return fileRepository.save(newFile);
    }

    // *** ملاحظة: إذا كنت تريد إتاحة التسمية المخصصة من الفورم (customFileName)
    // *** فيجب تعديل دالة storeFile أعلاه لتأخذ اسمًا مخصصًا كمعامل إضافي
    // *** وتستخدمه بدلاً من createTimestampedFileName إذا كان متاحًا.

    public UploadedFile getFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id : " + fileId));
    }

    public List<UploadedFile> getAllFiles() {
        return fileRepository.findAll();
    }

    public UploadedFile updateFileName(Long fileId, String newFileName) {
        UploadedFile file = getFileById(fileId);
        file.setFileName(newFileName);
        return fileRepository.save(file);
    }

    public void deleteFile(Long fileId) {
        fileRepository.deleteById(fileId);
    }

}