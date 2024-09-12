package com.smart.home.deviceservice.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "device_types")
public class DeviceTypeDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long deviceTypeId;

    @Column(name = "name", nullable = false, unique = true)
    private String deviceTypeName;

    public DeviceTypeDAO(String deviceTypeName) {
        this.deviceTypeName = deviceTypeName;
    }

}
