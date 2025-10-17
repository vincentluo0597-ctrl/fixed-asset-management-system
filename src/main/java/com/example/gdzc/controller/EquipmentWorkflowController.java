package com.example.gdzc.controller;

import com.example.gdzc.domain.BorrowRecord;
import com.example.gdzc.domain.MaintenanceRecord;
import com.example.gdzc.domain.PlacementLog;
import com.example.gdzc.domain.Equipment;
import com.example.gdzc.dto.BorrowRequestDTO;
import com.example.gdzc.dto.MaintenanceCompleteDTO;
import com.example.gdzc.dto.MaintenanceStartDTO;
import com.example.gdzc.dto.MoveRequestDTO;
import com.example.gdzc.dto.ScrapRequestDTO;
import com.example.gdzc.service.EquipmentWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipment/{id}/workflow")
@RequiredArgsConstructor
public class EquipmentWorkflowController {
    private final EquipmentWorkflowService workflowService;

    @PostMapping("/borrow")
    public ResponseEntity<Equipment> borrow(@PathVariable("id") Long id, @Valid @RequestBody BorrowRequestDTO dto) {
        return ResponseEntity.ok(workflowService.borrow(id, dto));
    }

    @PostMapping("/return")
    public ResponseEntity<Equipment> returnBack(@PathVariable("id") Long id) {
        return ResponseEntity.ok(workflowService.returnBack(id));
    }

    @PostMapping("/move")
    public ResponseEntity<Equipment> move(@PathVariable("id") Long id, @Valid @RequestBody MoveRequestDTO dto) {
        return ResponseEntity.ok(workflowService.move(id, dto));
    }

    @PostMapping("/maintenance/start")
    public ResponseEntity<Equipment> maintenanceStart(@PathVariable("id") Long id, @Valid @RequestBody MaintenanceStartDTO dto) {
        return ResponseEntity.ok(workflowService.maintenanceStart(id, dto));
    }

    @PostMapping("/maintenance/complete")
    public ResponseEntity<Equipment> maintenanceComplete(@PathVariable("id") Long id, @Valid @RequestBody MaintenanceCompleteDTO dto) {
        return ResponseEntity.ok(workflowService.maintenanceComplete(id, dto));
    }

    @PostMapping("/scrap")
    public ResponseEntity<Equipment> scrap(@PathVariable("id") Long id, @Valid @RequestBody ScrapRequestDTO dto) {
        return ResponseEntity.ok(workflowService.scrap(id, dto));
    }

    // 历史记录：位置迁移
    @GetMapping("/history/placements")
    public java.util.List<PlacementLog> getPlacementHistory(@PathVariable("id") Long id) {
        return workflowService.getPlacementHistory(id);
    }

    // 历史记录：借用记录
    @GetMapping("/history/borrows")
    public java.util.List<BorrowRecord> getBorrowHistory(@PathVariable("id") Long id) {
        return workflowService.getBorrowHistory(id);
    }

    // 历史记录：维修记录
    @GetMapping("/history/maintenances")
    public java.util.List<MaintenanceRecord> getMaintenanceHistory(@PathVariable("id") Long id) {
        return workflowService.getMaintenanceHistory(id);
    }
}