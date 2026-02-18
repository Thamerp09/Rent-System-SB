package com.thamer.Rent_System.model;

import java.time.LocalDateTime;
import jakarta.persistence.*; // استخدام jakarta فقط

@Entity
@Table(name = "uploaded_files")
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // من الجيد جعل الأسماء مطلوبة
    private String fileName;

    private String fileType;

    private LocalDateTime uploadDate;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB") // البيانات يجب ألا تكون فارغة
    private byte[] data;
    
    // --- الحقول الجديدة ---

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // من الجيد جعل التصنيف مطلوبًا
    private FileCategory fileCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // من الجيد جعل الموقع مطلوبًا
    private PropertyLocation location;
    
    // --- Constructor ---
    public UploadedFile() {
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public FileCategory getFileCategory() {
        return fileCategory;
    }

    public void setFileCategory(FileCategory fileCategory) {
        this.fileCategory = fileCategory;
    }

    public PropertyLocation getLocation() {
        return location;
    }

    public void setLocation(PropertyLocation location) {
        this.location = location;
    }
}