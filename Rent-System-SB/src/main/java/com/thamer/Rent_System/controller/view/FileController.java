package com.thamer.Rent_System.controller.view;

import com.thamer.Rent_System.model.FileCategory;
import com.thamer.Rent_System.model.PropertyLocation;
import com.thamer.Rent_System.model.UploadedFile;
import com.thamer.Rent_System.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional; // تأكد من استيراد Optional

@Controller
@RequestMapping("/")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    // 1. عرض قائمة الملفات (صحيح ولا يحتاج تعديل)
    @GetMapping("/files")
    public String listAllFiles(Model model) {
        List<UploadedFile> files = fileStorageService.getAllFiles();
        model.addAttribute("files", files);

        // تمرير قائمة المواقع والتصنيفات للـ HTML
        model.addAttribute("locations", PropertyLocation.values());
        model.addAttribute("categories", FileCategory.values());

        return "file_manager";
    }

    // 2. تحديث دالة الرفع
    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("displayName") String displayName,
            @RequestParam("category") FileCategory category,
            @RequestParam("location") PropertyLocation location,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        if (file.isEmpty() || displayName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Error: يرجى ملء جميع الحقول المطلوبة.");
            return "redirect:/files";
        }

        try {
            fileStorageService.storeFile(file, displayName, category, location);
            redirectAttributes.addFlashAttribute("message",
                    "successfully uploaded file: " + displayName);

        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute("message",
                    "Error: فشل رفع الملف: " + ex.getMessage());
        }

        return "redirect:/files";
    }

    // 3. نقطة نهاية لتنزيل الملف (صحيح ولا يحتاج تعديل)
    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        try {
            UploadedFile file = fileStorageService.getFileById(fileId);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                    .body(file.getData());

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 4. نقطة نهاية لمعاينة الملف (صحيح ولا يحتاج تعديل)
    @GetMapping("/preview/{fileId}")
    public ResponseEntity<byte[]> previewFile(@PathVariable Long fileId) {
        try {
            UploadedFile file = fileStorageService.getFileById(fileId);

            // هذا مناسب لمعاينة PDF، الصور، والنصوص مباشرة في المتصفح
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                    .body(file.getData());

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 5. نقطة نهاية لتعديل الاسم (تم تصحيح المسار واسم الحقل)
    @PostMapping("/editName/{id}") // <-- تم تصحيح المسار هنا
    public String editFileName(@PathVariable("id") Long id,
            @RequestParam("newDisplayName") String newDisplayName, // <-- تم تصحيح اسم الحقل هنا
            RedirectAttributes redirectAttributes) {
        try {
            // قد تحتاج لتعديل هذه الدالة في الخدمة لتأخذ ID والاسم الجديد
            fileStorageService.updateFileName(id, newDisplayName);
            redirectAttributes.addFlashAttribute("message", "successfully updated file name.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("message", "Error: Could not update file name: " + e.getMessage());
        }
        return "redirect:/files";
    }

    // 6. نقطة نهاية لحذف الملف (صحيح ولا يحتاج تعديل)
    @PostMapping("/delete/{id}")
    public String deleteFile(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            fileStorageService.deleteFile(id);
            redirectAttributes.addFlashAttribute("message", "successfully deleted the file.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("message", "Error: Could not delete the file: " + e.getMessage());
        }
        return "redirect:/files";
    }
}