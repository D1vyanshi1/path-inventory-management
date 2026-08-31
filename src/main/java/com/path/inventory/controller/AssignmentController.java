package com.path.inventory.controller;

import com.path.inventory.entity.Device;
import com.path.inventory.entity.DeviceHistory;
import com.path.inventory.entity.Employee;
import com.path.inventory.repository.DeviceHistoryRepository;
import com.path.inventory.repository.DeviceRepository;
import com.path.inventory.repository.EmployeeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class AssignmentController {

    private final DeviceRepository deviceRepository;
    private final EmployeeRepository employeeRepository;
    private final DeviceHistoryRepository deviceHistoryRepository;

    public AssignmentController(DeviceRepository deviceRepository,
                                EmployeeRepository employeeRepository,
                                DeviceHistoryRepository deviceHistoryRepository) {
        this.deviceRepository = deviceRepository;
        this.employeeRepository = employeeRepository;
        this.deviceHistoryRepository = deviceHistoryRepository;
    }

    @GetMapping("/assign-device")
    public String showAssignPage(Model model) {

        model.addAttribute("devices", deviceRepository.findByAvailableTrue());
        model.addAttribute("employees", employeeRepository.findAll());

        return "assign-device";
    }

    @PostMapping("/assign-device")
    public String assignDevice(@RequestParam Long deviceId,
                               @RequestParam Long employeeId,
                               @RequestParam LocalDate issueDate) {

        Device device = deviceRepository.findById(deviceId).orElseThrow();
        Employee employee = employeeRepository.findById(employeeId).orElseThrow();

        // Update current device allocation
        device.setEmployee(employee);
        device.setAssetLocation(employee.getOfficeLocation());
        device.setIssueDate(issueDate);
        device.setAvailable(false);

        deviceRepository.save(device);

        // Save allocation history
        DeviceHistory history = new DeviceHistory();
        history.setDevice(device);
        history.setEmployee(employee);
        history.setIssueDate(issueDate);

        deviceHistoryRepository.save(history);

        return "redirect:/devices";
    }

    @GetMapping("/return-device/{id}")
    public String returnDevice(@PathVariable Long id) {

        Device device = deviceRepository.findById(id).orElseThrow();

        // Find current active history record
        deviceHistoryRepository
                .findFirstByDeviceAndReturnDateIsNull(device)
                .ifPresent(history -> {
                    history.setReturnDate(LocalDate.now());
                    deviceHistoryRepository.save(history);
                });

        // Clear current allocation
        device.setEmployee(null);
        device.setIssueDate(null);
        device.setAvailable(true);

        deviceRepository.save(device);

        return "redirect:/devices";
    }

}