package com.path.inventory.controller;

import com.path.inventory.repository.DeviceRepository;
import com.path.inventory.repository.EmployeeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class DashboardController {

    private final DeviceRepository deviceRepository;
    private final EmployeeRepository employeeRepository;

    public DashboardController(
            DeviceRepository deviceRepository,
            EmployeeRepository employeeRepository) {

        this.deviceRepository = deviceRepository;
        this.employeeRepository = employeeRepository;
    }


    // ==========================================
    // MAIN DASHBOARD
    // ==========================================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute(
                "totalDevices",
                deviceRepository.count()
        );

        model.addAttribute(
                "availableDevices",
                deviceRepository.countByAvailableTrue()
        );

        model.addAttribute(
                "assignedDevices",
                deviceRepository.countByAvailableFalse()
        );

        model.addAttribute(
                "totalEmployees",
                employeeRepository.count()
        );

        return "dashboard";
    }


    // ==========================================
    // LOCATION DATA
    // ==========================================

    @GetMapping("/dashboard/location")
    @ResponseBody
    public Map<String, Long> getLocationData(
            @RequestParam String location) {

        Map<String, Long> data = new HashMap<>();


        // --------------------------------------
        // Check whether location is a country
        // --------------------------------------

        boolean isCountry =
                location.equals("India") ||
                        location.equals("Nepal") ||
                        location.equals("Bangladesh");


        if (isCountry) {

            // -------------------------------
            // Country device counts
            // -------------------------------

            long totalDevices = 0;

            long availableDevices = 0;

            long assignedDevices = 0;


            /*
             * Devices are stored using assetLocation.
             *
             * We count the devices whose assetLocation
             * belongs to one of the cities in that country.
             */

            if (location.equals("India")) {

                totalDevices =
                        countDevicesForCities(
                                "Delhi",
                                "Mumbai",
                                "Lucknow"
                        );

                availableDevices =
                        countAvailableDevicesForCities(
                                "Delhi",
                                "Mumbai",
                                "Lucknow"
                        );

                assignedDevices =
                        countAssignedDevicesForCities(
                                "Delhi",
                                "Mumbai",
                                "Lucknow"
                        );
            }


            else if (location.equals("Nepal")) {

                totalDevices =
                        countDevicesForCities(
                                "Kathmandu",
                                "Pokhara"
                        );

                availableDevices =
                        countAvailableDevicesForCities(
                                "Kathmandu",
                                "Pokhara"
                        );

                assignedDevices =
                        countAssignedDevicesForCities(
                                "Kathmandu",
                                "Pokhara"
                        );
            }


            else if (location.equals("Bangladesh")) {

                totalDevices =
                        countDevicesForCities(
                                "Dhaka",
                                "Chattogram"
                        );

                availableDevices =
                        countAvailableDevicesForCities(
                                "Dhaka",
                                "Chattogram"
                        );

                assignedDevices =
                        countAssignedDevicesForCities(
                                "Dhaka",
                                "Chattogram"
                        );
            }


            // -------------------------------
            // Employee count
            // -------------------------------

            long totalEmployees =
                    employeeRepository.countByCountry(location);


            data.put("totalDevices", totalDevices);

            data.put(
                    "availableDevices",
                    availableDevices
            );

            data.put(
                    "assignedDevices",
                    assignedDevices
            );

            data.put(
                    "totalEmployees",
                    totalEmployees
            );

            return data;
        }


        // ======================================
        // CITY
        // ======================================

        long totalDevices =
                deviceRepository
                        .countByAssetLocation(location);


        long availableDevices =
                deviceRepository
                        .countByAssetLocationAndAvailableTrue(
                                location
                        );


        long assignedDevices =
                deviceRepository
                        .countByAssetLocationAndAvailableFalse(
                                location
                        );


        long totalEmployees =
                employeeRepository
                        .countByOfficeLocation(location);


        data.put("totalDevices", totalDevices);

        data.put(
                "availableDevices",
                availableDevices
        );

        data.put(
                "assignedDevices",
                assignedDevices
        );

        data.put(
                "totalEmployees",
                totalEmployees
        );


        return data;
    }


    // ==========================================
    // COUNTRY HELPER METHODS
    // ==========================================

    private long countDevicesForCities(
            String... cities) {

        long count = 0;

        for (String city : cities) {

            count +=
                    deviceRepository
                            .countByAssetLocation(city);
        }

        return count;
    }


    private long countAvailableDevicesForCities(
            String... cities) {

        long count = 0;

        for (String city : cities) {

            count +=
                    deviceRepository
                            .countByAssetLocationAndAvailableTrue(
                                    city
                            );
        }

        return count;
    }


    private long countAssignedDevicesForCities(
            String... cities) {

        long count = 0;

        for (String city : cities) {

            count +=
                    deviceRepository
                            .countByAssetLocationAndAvailableFalse(
                                    city
                            );
        }

        return count;
    }
}