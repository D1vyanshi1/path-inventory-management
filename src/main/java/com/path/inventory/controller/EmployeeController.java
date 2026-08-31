package com.path.inventory.controller;

import org.springframework.data.domain.Sort;
import com.path.inventory.entity.Employee;
import com.path.inventory.repository.EmployeeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    private final String uploadDir = "uploads/employees/";

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/employees")
    public String viewEmployees(
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        model.addAttribute("employees", employeeRepository.findAll(sort));
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "employees";
    }

    @GetMapping("/employees/new")
    public String showAddEmployeeForm(Model model) {

        model.addAttribute("employee", new Employee());

        return "add-employee";
    }

    @PostMapping("/employees/save")
    public String saveEmployee(
            @ModelAttribute Employee employee,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model) throws IOException {

        if (employeeRepository.existsByEmployeeId(employee.getEmployeeId())) {

            model.addAttribute("error", "Employee ID already exists.");
            model.addAttribute("employee", employee);

            return "add-employee";
        }

        if (employeeRepository.existsByEmail(employee.getEmail())) {

            model.addAttribute("error", "Email already exists.");
            model.addAttribute("employee", employee);

            return "add-employee";
        }

        if (!imageFile.isEmpty()) {

            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = imageFile.getOriginalFilename();

            String extension = "";

            if (originalFileName != null &&
                    originalFileName.contains(".")) {

                extension =
                        originalFileName.substring(
                                originalFileName.lastIndexOf(".")
                        );
            }

            String fileName =
                    UUID.randomUUID() + extension;

            Path filePath =
                    uploadPath.resolve(fileName);

            Files.copy(
                    imageFile.getInputStream(),
                    filePath
            );

            employee.setImage(fileName);
        }

        employeeRepository.save(employee);

        return "redirect:/employees";
    }

    @GetMapping("/employees/delete/{id}")
    public String deleteEmployee(@PathVariable Long id) {

        employeeRepository.deleteById(id);

        return "redirect:/employees";
    }

    @GetMapping("/employees/edit/{id}")
    public String showEditEmployeeForm(@PathVariable Long id, Model model) {

        Employee employee = employeeRepository.findById(id).orElseThrow();

        model.addAttribute("employee", employee);

        return "edit-employee";
    }

    @PostMapping("/employees/update/{id}")
    public String updateEmployee(
            @PathVariable Long id,
            @ModelAttribute Employee employee,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model) throws IOException {

        Employee existingEmployee =
                employeeRepository.findById(id).orElseThrow();

        if (employeeRepository.existsByEmailAndIdNot(
                employee.getEmail(), id)) {

            model.addAttribute(
                    "error",
                    "Email already exists."
            );

            model.addAttribute(
                    "employee",
                    employee
            );

            return "edit-employee";
        }

        existingEmployee.setEmployeeId(
                employee.getEmployeeId()
        );

        existingEmployee.setEmployeeName(
                employee.getEmployeeName()
        );

        existingEmployee.setEmail(
                employee.getEmail()
        );

        existingEmployee.setDepartment(
                employee.getDepartment()
        );

        existingEmployee.setDesignation(
                employee.getDesignation()
        );

        existingEmployee.setCountry(
                employee.getCountry()
        );

        existingEmployee.setOfficeLocation(
                employee.getOfficeLocation()
        );

        if (!imageFile.isEmpty()) {

            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName =
                    imageFile.getOriginalFilename();

            String extension = "";

            if (originalFileName != null &&
                    originalFileName.contains(".")) {

                extension =
                        originalFileName.substring(
                                originalFileName.lastIndexOf(".")
                        );
            }

            String fileName =
                    UUID.randomUUID() + extension;

            Path filePath =
                    uploadPath.resolve(fileName);

            Files.copy(
                    imageFile.getInputStream(),
                    filePath
            );

            existingEmployee.setImage(fileName);
        }

        employeeRepository.save(existingEmployee);

        return "redirect:/employees";
    }

    @GetMapping("/employees/{id}/devices")
    public String viewEmployeeDevices(@PathVariable Long id, Model model) {

        Employee employee = employeeRepository.findById(id).orElseThrow();

        model.addAttribute("employee", employee);
        model.addAttribute("devices", employee.getDevices());

        return "employee-devices";
    }

}