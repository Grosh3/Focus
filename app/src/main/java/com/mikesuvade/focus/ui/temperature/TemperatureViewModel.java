package com.mikesuvade.focus.ui.temperature;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mikesuvade.focus.domain.models.TemperatureResult;
import com.mikesuvade.focus.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.List;

public class TemperatureViewModel extends ViewModel {

    private static final String TAG = "TEMP_DEBUG";
    private final IRepository repository;
    private final MutableLiveData<List<TemperatureResult>> results =
            new MutableLiveData<>(new ArrayList<>());

    // ✅ ТЕРМОСОПРОТИВЛЕНИЯ (Ом)
    private final String[] ohmTables = {"tcp50p", "tsm50m", "gr21", "gr23"};
    private final String[] ohmNames  = {"ТСП50П", "ТСМ50М", "Гр21 (46П)", "Гр23 (53М)"};

    // ✅ ТЕРМОПАРЫ (мВ)
    private final String[] mvTables = {"ha", "hk"};
    private final String[] mvNames  = {"ХА", "ХК"};

    // 🔥 ГРАНИЦЫ ТЕМПЕРАТУР (°C) — порядок совпадает с массивами выше
    // [0]=tcp50p, [1]=tsm50m, [2]=gr21, [3]=gr23
    private final double[] ohmMinTemp = {-50, -50, -30, -20};
    private final double[] ohmMaxTemp = {409, 209, 409, 179};

    // [0]=ha(ХА), [1]=hk(ХК)
    private final double[] mvMinTemp = {-270, -200};
    private final double[] mvMaxTemp = {1372,  800};

    public TemperatureViewModel(IRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<TemperatureResult>> getResults() {
        return results;
    }

    public void calculate(double value, String mode, double coldJunctionTemp, double lineResistance) {
        new Thread(() -> {
            try {
                List<TemperatureResult> resultList = new ArrayList<>();

                if ("OHM".equals(mode)) {
                    Log.d(TAG, "=== OHM CALCULATION ===");
                    Log.d(TAG, "userValue: " + value);
                    Log.d(TAG, "lineResistance: " + lineResistance);

                    double correctedValue = value - lineResistance;
                    Log.d(TAG, "correctedValue: " + correctedValue);

                    for (int i = 0; i < ohmTables.length; i++) {
                        double temperature = repository.getTemperatureFromResistance(
                                ohmTables[i], correctedValue);
                        Log.d(TAG, "sensor: " + ohmNames[i] + ", temperature: " + temperature);

                        TemperatureResult result = new TemperatureResult();
                        result.setSensorName(ohmNames[i]);
                        result.setUserValue(value);
                        result.setUnit("Ом");
                        result.setCorrectedValue(correctedValue);
                        result.setTemperature(temperature);
                        result.setTableName(ohmTables[i]);

                        // 🔥 ПРОВЕРКА ДИАПАЗОНА (граница = ошибка)
                        if (temperature <= ohmMinTemp[i] || temperature >= ohmMaxTemp[i]) {
                            result.setOutOfRange(true);
                        }

                        resultList.add(result);
                    }
                } else {
                    Log.d(TAG, "=== MV CALCULATION ===");
                    Log.d(TAG, "userValue: " + value);
                    Log.d(TAG, "coldJunctionTemp: " + coldJunctionTemp);

                    boolean hasColdJunction = (coldJunctionTemp != -999);
                    Log.d(TAG, "hasColdJunction: " + hasColdJunction);

                    for (int i = 0; i < mvTables.length; i++) {
                        double coldJunctionMv;
                        double totalMv;
                        double effectiveTemp;

                        if (hasColdJunction) {
                            coldJunctionMv = repository.getSignalFromTemperature(
                                    mvTables[i], coldJunctionTemp);
                            totalMv = value + coldJunctionMv;
                            effectiveTemp = coldJunctionTemp;
                            Log.d(TAG, "sensor: " + mvNames[i]);
                            Log.d(TAG, "  coldJunctionMv: " + coldJunctionMv);
                        } else {
                            coldJunctionMv = 0;
                            totalMv = value;
                            effectiveTemp = 0;
                            Log.d(TAG, "sensor: " + mvNames[i]);
                            Log.d(TAG, "  no cold junction temperature provided, skipping compensation");
                        }

                        Log.d(TAG, "  totalMv: " + totalMv);

                        double temperature = repository.getTemperatureFromResistance(
                                mvTables[i], totalMv);
                        Log.d(TAG, "  temperature: " + temperature);

                        TemperatureResult result = new TemperatureResult();
                        result.setSensorName(mvNames[i]);
                        result.setUserValue(value);
                        result.setUnit("мВ");
                        result.setColdJunctionTemp(effectiveTemp);
                        result.setColdJunctionMv(coldJunctionMv);
                        result.setTotalMv(totalMv);
                        result.setTemperature(temperature);
                        result.setTableName(mvTables[i]);

                        // 🔥 ПРОВЕРКА ДИАПАЗОНА (граница = ошибка)
                        if (temperature <= mvMinTemp[i] || temperature >= mvMaxTemp[i]) {
                            result.setOutOfRange(true);
                        }

                        resultList.add(result);
                    }
                }

                Log.d(TAG, "results count: " + resultList.size());
                results.postValue(resultList);

            } catch (Exception e) {
                Log.e(TAG, "Error in calculate", e);
                e.printStackTrace();
            }
        }).start();
    }

    public void clearResults() {
        results.setValue(new ArrayList<>());
    }
    /**
     * Обратный расчёт: по введённой температуре находим сигнал (Ом или мВ) для каждой таблицы.
     * Холодный спай и сопротивление линии НЕ учитываются.
     */
    public void calculateInverse(double temperature, String mode) {
        new Thread(() -> {
            try {
                List<TemperatureResult> resultList = new ArrayList<>();

                if ("OHM".equals(mode)) {
                    for (int i = 0; i < ohmTables.length; i++) {
                        double signal = repository.getSignalFromTemperature(ohmTables[i], temperature);

                        TemperatureResult result = new TemperatureResult();
                        result.setSensorName(ohmNames[i]);
                        result.setUnit("Ом");
                        result.setTemperature(temperature);       // введённая T
                        result.setCorrectedValue(signal);          // рассчитанный сигнал
                        result.setTableName(ohmTables[i]);
                        result.setInverse(true);

                        if (temperature <= ohmMinTemp[i] || temperature >= ohmMaxTemp[i]) {
                            result.setOutOfRange(true);
                        }

                        resultList.add(result);
                    }
                } else {
                    for (int i = 0; i < mvTables.length; i++) {
                        double signal = repository.getSignalFromTemperature(mvTables[i], temperature);

                        TemperatureResult result = new TemperatureResult();
                        result.setSensorName(mvNames[i]);
                        result.setUnit("мВ");
                        result.setTemperature(temperature);
                        result.setCorrectedValue(signal);
                        result.setTableName(mvTables[i]);
                        result.setInverse(true);

                        if (temperature <= mvMinTemp[i] || temperature >= mvMaxTemp[i]) {
                            result.setOutOfRange(true);
                        }

                        resultList.add(result);
                    }
                }

                results.postValue(resultList);
            } catch (Exception e) {
                Log.e(TAG, "Error in calculateInverse", e);
            }
        }).start();
    }
}