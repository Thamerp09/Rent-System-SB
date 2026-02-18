package com.thamer.Rent_System.repository;

import com.thamer.Rent_System.model.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<UploadedFile, Long> {
}