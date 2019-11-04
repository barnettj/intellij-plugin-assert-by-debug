package dev.fervento.assertbydebug.sample.entity;

import java.util.HashMap;
import java.util.Map;

public class Car {
    private String plateNumber;
    private Map<String, Object> additionalData;

    public Car(String plateNumber) {
        this.plateNumber = plateNumber;
        this.additionalData = new HashMap<>();
    }

    public Car put(String key, Object obj) {
        this.additionalData.put(key, obj);
        return this;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }
}
