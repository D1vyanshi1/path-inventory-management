package com.path.inventory.service;

import com.path.inventory.entity.Device;
import com.path.inventory.repository.DeviceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LicenseReminderService {

    private final DeviceRepository deviceRepository;
    private final EmailService emailService;

    public LicenseReminderService(
            DeviceRepository deviceRepository,
            EmailService emailService) {

        this.deviceRepository = deviceRepository;
        this.emailService = emailService;
    }

    @Scheduled(
            fixedRate = 60000
    )

    public void checkLicenseRenewals() {

        LocalDate today = LocalDate.now();

        LocalDate reminderDate = today.plusMonths(1);

        List<Device> devices =
                deviceRepository.findByLicenseRenewalDate(reminderDate);
        for (Device device : devices) {

            if (!device.isLicenseReminderSent()) {

                System.out.println(
                        "License renewal reminder for: "
                                + device.getTagNumber()
                );

                emailService.sendEmail(
                        "divyanshii2704@gmail.com",
                        "License Renewal Reminder - "
                                + device.getTagNumber(),
                        "The license for device "
                                + device.getTagNumber()
                                + " is due for renewal on "
                                + device.getLicenseRenewalDate()
                                + "."
                );

                device.setLicenseReminderSent(true);

                deviceRepository.save(device);
            }
        }
    }
}