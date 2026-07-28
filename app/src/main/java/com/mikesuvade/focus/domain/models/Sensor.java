package com.mikesuvade.focus.domain.models;

public class Sensor {
    private int id;
    private int keynum;
    private int fa;
    private String kks;
    private String stMarkir;
    private String fullName;
    private String name;
    private String media;
    private String units;
    private double nominal;
    private double volMin;
    private double volMax;
    private String speed;
    private String faultPar;
    private String insteadF;
    private String filter;
    private String modelSensor;
    private String modSensor;
    private String additionalInfo;
    private double minVal;
    private double maxVal;
    private String measureUnit;
    private String location;
    private String cva;
    private String dampingTime;

    // Для пользовательских таблиц
    private String originalKks;
    private String editedAt;
    private boolean isCustom;

    // Геттеры и сеттеры (все поля)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getKeynum() { return keynum; }
    public void setKeynum(int keynum) { this.keynum = keynum; }

    public int getFa() { return fa; }
    public void setFa(int fa) { this.fa = fa; }

    public String getKks() { return kks; }
    public void setKks(String kks) { this.kks = kks; }

    public String getStMarkir() { return stMarkir; }
    public void setStMarkir(String stMarkir) { this.stMarkir = stMarkir; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMedia() { return media; }
    public void setMedia(String media) { this.media = media; }

    public String getUnits() { return units; }
    public void setUnits(String units) { this.units = units; }

    public double getNominal() { return nominal; }
    public void setNominal(double nominal) { this.nominal = nominal; }

    public double getVolMin() { return volMin; }
    public void setVolMin(double volMin) { this.volMin = volMin; }

    public double getVolMax() { return volMax; }
    public void setVolMax(double volMax) { this.volMax = volMax; }

    public String getSpeed() { return speed; }
    public void setSpeed(String speed) { this.speed = speed; }

    public String getFaultPar() { return faultPar; }
    public void setFaultPar(String faultPar) { this.faultPar = faultPar; }

    public String getInsteadF() { return insteadF; }
    public void setInsteadF(String insteadF) { this.insteadF = insteadF; }

    public String getFilter() { return filter; }
    public void setFilter(String filter) { this.filter = filter; }

    public String getModelSensor() { return modelSensor; }
    public void setModelSensor(String modelSensor) { this.modelSensor = modelSensor; }

    public String getModSensor() { return modSensor; }
    public void setModSensor(String modSensor) { this.modSensor = modSensor; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public double getMinVal() { return minVal; }
    public void setMinVal(double minVal) { this.minVal = minVal; }

    public double getMaxVal() { return maxVal; }
    public void setMaxVal(double maxVal) { this.maxVal = maxVal; }

    public String getMeasureUnit() { return measureUnit; }
    public void setMeasureUnit(String measureUnit) { this.measureUnit = measureUnit; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCva() { return cva; }
    public void setCva(String cva) { this.cva = cva; }

    public String getDampingTime() { return dampingTime; }
    public void setDampingTime(String dampingTime) { this.dampingTime = dampingTime; }

    public String getOriginalKks() { return originalKks; }
    public void setOriginalKks(String originalKks) { this.originalKks = originalKks; }

    public String getEditedAt() { return editedAt; }
    public void setEditedAt(String editedAt) { this.editedAt = editedAt; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    @Override
    public String toString() {
        return name + " (" + kks + ")";
    }
}