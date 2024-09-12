package com.smart.home.scenarioservice.repository.model;

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
@Table(name = "scenarios")
public class ScenarioDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long scenarioId;

    @Column(name = "device_type_id", nullable = false)
    private Long  deviceTypeId;

    @Column(name = "weather_condition", nullable = false)
    private String weatherCondition;

    @Column(name = "condition_value")
    private String conditionValue;

    @Column(name = "operator", nullable = false)
    private String operator;

    @Column(name = "new_status", nullable = false)
    private String newStatus;

}
