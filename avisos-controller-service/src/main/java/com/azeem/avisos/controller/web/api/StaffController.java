/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.api;

import com.azeem.avisos.controller.model.staff.StaffRecord;
import com.azeem.avisos.controller.service.staff.StaffService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff")
public class StaffController {
  private final StaffService staffService;

  public StaffController(StaffService staffService) {
    this.staffService = staffService;
  }

  @GetMapping
  public List<StaffRecord> getAllStaff() {
    return staffService.getAllStaff();
  }

  @GetMapping("/{id}")
  public StaffRecord getStaffMember(@PathVariable String id) {
    return staffService.getStaff(id);
  }

  @GetMapping("/search")
  public List<StaffRecord> searchStaff(@RequestParam String q) {
    return staffService.searchByJurisdiction(q);
  }
}
