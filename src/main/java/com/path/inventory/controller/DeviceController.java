package com.path.inventory.controller;

import com.path.inventory.entity.Device;
import com.path.inventory.entity.DeviceHistory;
import com.path.inventory.repository.DeviceHistoryRepository;
import com.path.inventory.repository.DeviceRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.path.inventory.service.ExcelExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import com.path.inventory.entity.User;
import com.path.inventory.repository.UserRepository;
import org.springframework.security.core.Authentication;


@Controller
public class DeviceController {

    private final DeviceRepository deviceRepository;
    private final DeviceHistoryRepository deviceHistoryRepository;
    private final ExcelExportService excelExportService;
    private final UserRepository userRepository;

    public DeviceController(DeviceRepository deviceRepository,
                            DeviceHistoryRepository deviceHistoryRepository,
                            ExcelExportService excelExportService,
                            UserRepository userRepository) {

        this.deviceRepository = deviceRepository;
        this.deviceHistoryRepository = deviceHistoryRepository;
        this.excelExportService = excelExportService;
        this.userRepository = userRepository;
    }

    private User getLoggedInUser(Authentication authentication) {

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Logged-in user not found"));
    }

    @GetMapping("/devices")
    public String viewDevices(
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String officeLocation,
            @RequestParam(required = false) String purchaseSource,
            Model model,
            Authentication authentication) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        List<Device> devices;

        // Country + Office Location
        if (country != null && !country.isEmpty()
                && officeLocation != null && !officeLocation.isEmpty()) {

            devices = deviceRepository.findByEmployeeCountryAndEmployeeOfficeLocation(
                    country,
                    officeLocation);

        }

        // Country only
        else if (country != null && !country.isEmpty()) {

            devices = deviceRepository.findByEmployeeCountry(country);

        }

        // Office Location only
        else if (officeLocation != null && !officeLocation.isEmpty()) {

            devices = deviceRepository.findByEmployeeOfficeLocation(officeLocation);

        }

        // Status
        else if ("available".equalsIgnoreCase(status)) {

            devices = deviceRepository.findByAvailableTrue(sort);

        }

        else if ("assigned".equalsIgnoreCase(status)) {

            devices = deviceRepository.findByAvailableFalse(sort);

        }

        else {

            devices = deviceRepository.findAll(sort);

        }


        // Purchase Source filter
        if (purchaseSource != null && !purchaseSource.isEmpty()) {

            devices = devices.stream()
                    .filter(device ->
                            purchaseSource.equalsIgnoreCase(
                                    device.getPurchaseSource()))
                    .toList();
        }


        // Category User restriction
        User loggedInUser =
                getLoggedInUser(authentication);

        if ("ROLE_CATEGORY_USER".equals(loggedInUser.getRole())) {

            String category =
                    loggedInUser.getAssetCategory();

            devices = devices.stream()
                    .filter(device ->
                            category != null &&
                                    category.equals(device.getCategory()))
                    .toList();
        }


        model.addAttribute("devices", devices);

        model.addAttribute("country", country);

        model.addAttribute("officeLocation",
                officeLocation);

        model.addAttribute("purchaseSource",
                purchaseSource);

        model.addAttribute("sortBy",
                sortBy);

        model.addAttribute("direction",
                direction);

        return "devices";
    }

    @GetMapping("/devices/export")
    public void exportToExcel(HttpServletResponse response) throws IOException {

        excelExportService.exportDevices(response);

    }

    @GetMapping("/devices/search")
    public String searchDevices(@RequestParam("keyword") String keyword, Model model) {

        List<Device> devices = deviceRepository
                .findByDeviceNameContainingIgnoreCaseOrSerialNumberContainingIgnoreCaseOrTagNumberContainingIgnoreCase(
                        keyword, keyword, keyword);

        model.addAttribute("devices", devices);

        return "devices";
    }

    @GetMapping("/devices/new")
    public String showAddForm(Model model) {
        model.addAttribute("device", new Device());
        return "add-device";
    }

    @PostMapping("/devices/save")
    public String saveDevice(@ModelAttribute Device device,
                             @RequestParam("image") MultipartFile image) throws IOException {

        if (deviceRepository.existsBySerialNumber(device.getSerialNumber())) {
            return "redirect:/devices/new?error=duplicate";
        }

        device.setAvailable(true);

        if (!image.isEmpty()) {

            String fileName = image.getOriginalFilename();

            Path uploadPath = Paths.get("uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Files.copy(
                    image.getInputStream(),
                    uploadPath.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING
            );

            device.setImageName(fileName);
        }

        Device savedDevice = deviceRepository.save(device);

        String tagNumber = String.format("PATH-%04d", savedDevice.getId());

        savedDevice.setTagNumber(tagNumber);


        if (device.getInvoiceDate() != null) {
            int age = java.time.Period
                    .between(device.getInvoiceDate(), java.time.LocalDate.now())
                    .getYears();

            device.setAssetAge(age);
        }

        if (device.getAssetPutUseDate() != null &&
                device.getWarrantyPeriod() != null) {

            device.setWarrantyExpiryDate(
                    device.getAssetPutUseDate()
                            .plusYears(device.getWarrantyPeriod()));
        }

        deviceRepository.save(savedDevice);

        return "redirect:/devices";
    }

    @GetMapping("/devices/category")
    public String filterByCategory(@RequestParam String category, Model model) {

        if (category.equals("All")) {
            model.addAttribute("devices",
                    deviceRepository.findAllByOrderByIssueDateDesc());
        } else {
            model.addAttribute("devices",
                    deviceRepository.findByCategory(category));
        }

        return "devices";
    }

    @GetMapping("/devices/delete/{id}")
    public String deleteDevice(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {

        if (deviceHistoryRepository.existsByDeviceId(id)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Cannot delete this device because it has assignment history."
            );

            return "redirect:/devices";
        }

        deviceRepository.deleteById(id);

        return "redirect:/devices";
    }

    @GetMapping("/devices/edit/{id}")
    public String editDevice(@PathVariable Long id, Model model) {

        Device device = deviceRepository.findById(id).orElseThrow();

        model.addAttribute("device", device);

        return "edit-device";
    }

    @PostMapping("/devices/update")
    public String updateDevice(@ModelAttribute Device device,
                               @RequestParam("image") MultipartFile image) throws IOException {

        Device existingDevice = deviceRepository.findById(device.getId()).orElseThrow();

        Optional<Device> serialDevice =
                deviceRepository.findBySerialNumber(device.getSerialNumber());

        if (serialDevice.isPresent() &&
                !serialDevice.get().getId().equals(device.getId())) {

            return "redirect:/devices/edit/" + device.getId() + "?error=duplicate";
        }

        device.setTagNumber(existingDevice.getTagNumber());
        device.setEmployee(existingDevice.getEmployee());
        device.setIssueDate(existingDevice.getIssueDate());
        device.setAvailable(existingDevice.isAvailable());

        if (!image.isEmpty()) {

            String fileName = image.getOriginalFilename();

            Path uploadPath = Paths.get("uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Files.copy(
                    image.getInputStream(),
                    uploadPath.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING
            );

            device.setImageName(fileName);

        } else {

            device.setImageName(existingDevice.getImageName());
        }

        if (device.getInvoiceDate() != null) {
            int age = java.time.Period
                    .between(device.getInvoiceDate(), java.time.LocalDate.now())
                    .getYears();

            device.setAssetAge(age);
        }

        if (device.getAssetPutUseDate() != null &&
                device.getWarrantyPeriod() != null) {

            device.setWarrantyExpiryDate(
                    device.getAssetPutUseDate()
                            .plusYears(device.getWarrantyPeriod()));
        }


          // Preserve/reset license reminder flag

        if (!Objects.equals(
                existingDevice.getLicenseRenewalDate(),
                device.getLicenseRenewalDate())) {

            // License renewal date has changed
            device.setLicenseReminderSent(false);

        } else {

            // License renewal date has not changed
            device.setLicenseReminderSent(
                    existingDevice.isLicenseReminderSent()
            );
        }


        deviceRepository.save(device);

        return "redirect:/devices";
    }

    @GetMapping("/devices/{id}/history")
    public String deviceHistory(@PathVariable Long id, Model model) {

        Device device = deviceRepository.findById(id).orElseThrow();

        List<DeviceHistory> historyList =
                deviceHistoryRepository.findByDeviceIdOrderByIssueDateDesc(id);
        model.addAttribute("device", device);
        model.addAttribute("historyList", historyList);

        model.addAttribute("totalAllocations", historyList.size());

        model.addAttribute("currentStatus",
                device.isAvailable() ? "Available" : "Currently Assigned");

        model.addAttribute("currentEmployee",
                device.getEmployee() != null
                        ? device.getEmployee().getEmployeeName()
                        : "Not Assigned");

        return "device-history";
    }

    @GetMapping("/devices/physical-verification")
    public String physicalVerification(Model model,
                                       Authentication authentication) {

        List<Device> devices = deviceRepository.findAll();

        User loggedInUser = getLoggedInUser(authentication);

        if ("ROLE_CATEGORY_USER".equals(loggedInUser.getRole())) {

            String category = loggedInUser.getAssetCategory();

            devices = devices.stream()
                    .filter(device ->
                            category != null &&
                                    category.equals(device.getCategory()))
                    .toList();
        }

        model.addAttribute("devices", devices);

        return "physical-verification";
    }

    @PostMapping("/devices/physical-verification/{id}")
    public String savePhysicalVerification(
            @PathVariable Long id,
            @RequestParam("physicalVerificationDate")
            LocalDate physicalVerificationDate,
            @RequestParam("physicalCondition")
            String physicalCondition) {

        Device device =
                deviceRepository.findById(id)
                        .orElseThrow();

        device.setPhysicalVerificationDate(
                physicalVerificationDate);

        device.setPhysicalCondition(
                physicalCondition);

        deviceRepository.save(device);

        return "redirect:/devices/physical-verification";
    }

    @GetMapping("/devices/reporting")
    public String reporting(Model model,
                            Authentication authentication) {

        List<Device> devices = deviceRepository.findAll();

        User loggedInUser = getLoggedInUser(authentication);

        // Category User should only see their assigned category
        if ("ROLE_CATEGORY_USER".equals(loggedInUser.getRole())) {

            String category = loggedInUser.getAssetCategory();

            devices = devices.stream()
                    .filter(device ->
                            category != null &&
                                    category.equals(device.getCategory()))
                    .toList();
        }


        // ==========================================
        // ACTIVE / INACTIVE TOTAL
        // ==========================================

        long activeCount = devices.stream()
                .filter(Device::isAvailable)
                .count();

        long inactiveCount = devices.stream()
                .filter(device -> !device.isAvailable())
                .count();


        // ==========================================
        // ACTIVE / INACTIVE BY LOCATION
        // ==========================================

        java.util.Map<String, long[]> locationData =
                new java.util.LinkedHashMap<>();

        for (Device device : devices) {

            String location = device.getAssetLocation();

            if (location == null || location.isBlank()) {
                location = "Unknown";
            }

            locationData.putIfAbsent(
                    location,
                    new long[]{0, 0}
            );

            if (device.isAvailable()) {

                locationData.get(location)[0]++;

            } else {

                locationData.get(location)[1]++;
            }
        }


        // ==========================================
        // SEND DATA TO THYMELEAF
        // ==========================================

        model.addAttribute(
                "activeCount",
                activeCount
        );

        model.addAttribute(
                "inactiveCount",
                inactiveCount
        );

        model.addAttribute(
                "locationData",
                locationData
        );


        return "reporting";
    }

    @GetMapping("/devices/depreciation")
    public String depreciation(Model model,
                               Authentication authentication) {

        List<Device> devices = deviceRepository.findAll();

        User loggedInUser = getLoggedInUser(authentication);

        if ("ROLE_CATEGORY_USER".equals(loggedInUser.getRole())) {

            String category = loggedInUser.getAssetCategory();

            devices = devices.stream()
                    .filter(device ->
                            category != null &&
                                    category.equals(device.getCategory()))
                    .toList();
        }

        model.addAttribute("devices", devices);

        return "depreciation";
    }

}