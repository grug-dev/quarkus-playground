package kkpa.application.ds;

public record Building(
    Long id,
    Long buildingTypeId,
    String buildingType,
    Long cityId,
    String cityName,
    String state) {}
