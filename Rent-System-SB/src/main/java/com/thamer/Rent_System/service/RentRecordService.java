package com.thamer.Rent_System.service;


import com.thamer.Rent_System.model.RentRecord;
import com.thamer.Rent_System.repository.RentRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RentRecordService {

    @Autowired
    private RentRecordRepository rentRecordRepository;

    public void saveRecord(RentRecord record) {
        rentRecordRepository.save(record);
    }

    public List<RentRecord> getAllRecords() {
        return rentRecordRepository.findAll();
    }

    public Optional<RentRecord> getRecordById(Long id) {
        return rentRecordRepository.findById(id);
    }

    public void deleteRecord(Long id) {
        rentRecordRepository.deleteById(id);
    }

    public RentRecord updateRecord(Long id, RentRecord updatedRecord) {
        return rentRecordRepository.findById(id)
                .map(record -> {
                    record.setAmount(updatedRecord.getAmount());
                    record.setDueDate(updatedRecord.getDueDate());
                    record.setPaid(updatedRecord.isPaid());
                    record.setContract(updatedRecord.getContract());
                    return rentRecordRepository.save(record);
                }).orElseThrow(() -> new RuntimeException("Record not found with ID: " + id));
    }
}
