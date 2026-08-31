package com.path.inventory.repository;

import com.path.inventory.entity.Device;
import com.path.inventory.entity.DeviceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceHistoryRepository extends JpaRepository<DeviceHistory, Long> {

    List<DeviceHistory> findByDeviceIdOrderByIssueDateDesc(Long deviceId);
    Optional<DeviceHistory> findFirstByDeviceAndReturnDateIsNull(Device device);

    boolean existsByDeviceId(Long deviceId);

}