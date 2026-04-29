/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.infrastructure.repository;

import com.azeem.avisos.devices.api.DataAcquisitionDevice;
import com.azeem.avisos.devices.api.HardwareLink;
import com.azeem.avisos.devices.model.DeviceStatus;
import com.azeem.avisos.devices.model.DeviceType;
import com.azeem.avisos.infrastructure.factories.*;
import com.azeem.avisos.infrastructure.logger.LogLevel;
import com.azeem.avisos.infrastructure.logger.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static java.lang.String.valueOf;

public class DeviceRepository {
    private final Connection connection;
    private final Logger logger;
    private final HardwareLink hardwareLink;
    private final Map<DeviceType, DeviceFactory> deviceTypeDeviceFactoryMap = new HashMap<>();

    public DeviceRepository(Logger logger, HardwareLink hardwareLink) {
        this.logger = logger;
        this.hardwareLink = hardwareLink;
        this.connection = DatabaseManager.getInstance().getConnection();

        deviceTypeDeviceFactoryMap.put(DeviceType.MOTION_DEVICE, new MotionDeviceFactory());
        deviceTypeDeviceFactoryMap.put(DeviceType.SMOKE_DEVICE, new SmokeDeviceFactory());
        deviceTypeDeviceFactoryMap.put(DeviceType.THERMAL_DEVICE, new ThermalDeviceFactory());
        deviceTypeDeviceFactoryMap.put(DeviceType.GLASS_BREAK_SENSOR_DEVICE, new GlassBreakDeviceFactory());
    }

    public void save(DataAcquisitionDevice dataAcquisitionDevice) {
        String sql = """
            INSERT INTO dataAcquisitionDevices (id, type, status, battery_level)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                type = excluded.type,
                status = excluded.status,
                battery_level = excluded.battery_level
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, valueOf(dataAcquisitionDevice.getId()));
            ps.setString(2, valueOf(dataAcquisitionDevice.getDeviceType()));
            ps.setString(3, valueOf(dataAcquisitionDevice.getDeviceStatus()));
            ps.setDouble(4, dataAcquisitionDevice.getBatteryLife());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(e.getMessage(), LogLevel.ERROR);
        }
    }

    public void remove(DataAcquisitionDevice dataAcquisitionDevice) {
        String sql = "DELETE FROM dataAcquisitionDevices WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, valueOf(dataAcquisitionDevice.getId()));
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(e.getMessage(), LogLevel.ERROR);
        }
    }

    public void removeAll() {
        String sql = "DELETE FROM dataAcquisitionDevices";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(e.getMessage(), LogLevel.ERROR);
        }
    }

    public DataAcquisitionDevice findById(String id) {
        String sql = "SELECT * FROM dataAcquisitionDevices WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                UUID Id = UUID.fromString(rs.getString("id"));
                DeviceType deviceType = DeviceType.valueOf(rs.getString("type"));
                DeviceStatus deviceStatus = DeviceStatus.valueOf(rs.getString("status"));
                int batteryLife = rs.getInt("battery_level");

                UUID idFromDb = UUID.fromString(rs.getString("id"));
                int batt = rs.getInt("battery_level");

                DataAcquisitionDevice dataAcquisitionDevice = deviceTypeDeviceFactoryMap.get(deviceType).create(Id, logger, hardwareLink);
                dataAcquisitionDevice.setBatteryLife(batt);
                dataAcquisitionDevice.setDeviceStatus(deviceStatus);
                return dataAcquisitionDevice;
            }
        } catch (SQLException e) {
            logger.log(e.getMessage(), LogLevel.ERROR);
        }
        return null;
    }

    public List<DataAcquisitionDevice> loadAll() {
        String sql = "SELECT * FROM dataAcquisitionDevices";
        List<DataAcquisitionDevice> dataAcquisitionDevices = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dataAcquisitionDevices.add(mapRowToDevice(rs));
            }
        } catch (SQLException e) {
            logger.log(e.getMessage(), LogLevel.ERROR);
        }
        return dataAcquisitionDevices;
     }

    private DataAcquisitionDevice mapRowToDevice(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        DeviceType type = DeviceType.valueOf(rs.getString("type"));
        DeviceStatus status = DeviceStatus.valueOf(rs.getString("status"));
        int batt = rs.getInt("battery_level");

        DataAcquisitionDevice dataAcquisitionDevice = deviceTypeDeviceFactoryMap.get(type).create(id, logger, hardwareLink);
        dataAcquisitionDevice.setBatteryLife(batt);
        dataAcquisitionDevice.setDeviceStatus(status);
        return dataAcquisitionDevice;
    }
}
