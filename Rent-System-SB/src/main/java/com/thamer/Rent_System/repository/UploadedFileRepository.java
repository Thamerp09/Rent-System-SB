package com.thamer.Rent_System.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thamer.Rent_System.model.*;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

}
