/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.staff;

import com.azeem.avisos.controller.exceptions.ResourceNotFoundException;
import com.azeem.avisos.controller.model.staff.StaffRecord;
import com.azeem.avisos.controller.repository.StaffRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StaffService {
  private final StaffRepository staffRepository;

  public StaffService(StaffRepository staffRepository) {
    this.staffRepository = staffRepository;
  }

  public List<StaffRecord> getAllStaff() {
    return staffRepository.findAll();
  }

  public StaffRecord getStaff(String staffId) {
    return staffRepository
        .findByStaffId(staffId)
        .orElseThrow(() -> new ResourceNotFoundException("Staff record not found for: " + staffId));
  }

  public List<StaffRecord> searchByJurisdiction(String query) {
    return staffRepository.searchByJurisdiction(query);
  }
}
