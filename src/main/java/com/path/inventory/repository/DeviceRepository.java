package com.path.inventory.repository;

import com.path.inventory.entity.Device;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import com.path.inventory.entity.Device;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    boolean existsBySerialNumber(String serialNumber);

    Optional<Device> findBySerialNumber(String serialNumber);

    List<Device> findByDeviceNameContainingIgnoreCaseOrSerialNumberContainingIgnoreCaseOrTagNumberContainingIgnoreCase(
            String deviceName,
            String serialNumber,
            String tagNumber
    );

    List<Device> findByCategory(String category);

    List<Device> findAllByOrderByIssueDateDesc();

    List<Device> findByAvailableTrue();

    List<Device> findByAvailableTrue(Sort sort);

    List<Device> findByAvailableFalse(Sort sort);

    long countByAvailableTrue();

    long countByAvailableFalse();

    List<Device> findByEmployeeCountry(String country);

    List<Device> findByEmployeeOfficeLocation(String officeLocation);

    List<Device> findByEmployeeCountryAndEmployeeOfficeLocation(
            String country,
            String officeLocation);

    List<Device> findByLicenseRenewalDate(LocalDate licenseRenewalDate);

    long countByAssetLocation(String assetLocation);

    long countByAssetLocationAndAvailableTrue(String assetLocation);

    long countByAssetLocationAndAvailableFalse(String assetLocation);
}