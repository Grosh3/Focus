package com.mikesuvade.focus.ui.detail.helper;

import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.Sensor;
import com.mikesuvade.focus.domain.models.Setpoint;

public final class EntityCopyUtils {

    private EntityCopyUtils() {}

    public static GateValve copyValve(GateValve source) {
        if (source == null) return null;
        GateValve copy = new GateValve();
        copy.setId(source.getId());
        copy.setName(source.getName());
        copy.setKks(source.getKks());
        copy.setIsy(source.getIsy());
        copy.setPowerCabinet(source.getPowerCabinet());
        copy.setOnPlace(source.getOnPlace());
        copy.setFullName(source.getFullName());
        copy.setNameEng(source.getNameEng());
        copy.setAp50(source.getAp50());
        copy.setMark(source.getMark());
        copy.setCdaCabinet(source.getCdaCabinet());
        copy.setCdaCabinetPosition(source.getCdaCabinetPosition());
        copy.setSlot(source.getSlot());
        copy.setNameSpaceViewOpen(source.getNameSpaceViewOpen());
        copy.setNamespaceViewClose(source.getNamespaceViewClose());
        copy.setNamespaceViewPerifer(source.getNamespaceViewPerifer());
        copy.setDescriptionBlockingOpen(source.getDescriptionBlockingOpen());
        copy.setDescriptionBlockingClose(source.getDescriptionBlockingClose());
        copy.setDescriptionBlockingPerifer(source.getDescriptionBlockingPerifer());
        copy.setLocationDescription(source.getLocationDescription());
        copy.setOriginalId(source.getOriginalId());
        copy.setIsDeleted(source.getIsDeleted());
        copy.setIsEdited(source.getIsEdited());
        copy.setEditedAtValve(source.getEditedAtValve());
        copy.setCreatedAt(source.getCreatedAt());
        return copy;
    }

    public static Sensor copySensor(Sensor source) {
        if (source == null) return null;
        Sensor copy = new Sensor();
        copy.setId(source.getId());
        copy.setKeynum(source.getKeynum());
        copy.setFa(source.getFa());
        copy.setKks(source.getKks());
        copy.setStMarkir(source.getStMarkir());
        copy.setFullName(source.getFullName());
        copy.setName(source.getName());
        copy.setMedia(source.getMedia());
        copy.setUnits(source.getUnits());
        copy.setNominal(source.getNominal());
        copy.setVolMin(source.getVolMin());
        copy.setVolMax(source.getVolMax());
        copy.setSpeed(source.getSpeed());
        copy.setFaultPar(source.getFaultPar());
        copy.setInsteadF(source.getInsteadF());
        copy.setFilter(source.getFilter());
        copy.setModelSensor(source.getModelSensor());
        copy.setModSensor(source.getModSensor());
        copy.setAdditionalInfo(source.getAdditionalInfo());
        copy.setMinVal(source.getMinVal());
        copy.setMaxVal(source.getMaxVal());
        copy.setMeasureUnit(source.getMeasureUnit());
        copy.setLocation(source.getLocation());
        copy.setCva(source.getCva());
        copy.setDampingTime(source.getDampingTime());
        copy.setOriginalKks(source.getOriginalKks());
        copy.setIsDeleted(source.getIsDeleted());
        copy.setIsEdited(source.getIsEdited());
        copy.setEditedAt(source.getEditedAt());
        copy.setCreatedAt(source.getCreatedAt());
        return copy;
    }

    public static Setpoint copySetpoint(Setpoint source) {
        if (source == null) return null;
        Setpoint copy = new Setpoint();
        copy.setId(source.getId());
        copy.setName(source.getName());
        copy.setPositionName(source.getPositionName());
        copy.setLocation(source.getLocation());
        copy.setSetpointValue(source.getSetpointValue());
        copy.setDelayTime(source.getDelayTime());
        copy.setOperation(source.getOperation());
        copy.setNotes(source.getNotes());
        copy.setEquipmentGroup(source.getEquipmentGroup());
        copy.setOriginalId(source.getOriginalId());
        copy.setIsDeleted(source.getIsDeleted());
        copy.setIsEdited(source.getIsEdited());
        copy.setEditedAt(source.getEditedAt());
        copy.setCreatedAt(source.getCreatedAt());
        return copy;
    }
}