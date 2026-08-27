package com.mikesuvade.focus.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.domain.models.Sensor;
import com.mikesuvade.focus.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.List;

public class SensorViewModel extends ViewModel {

    private final IRepository repository;
    private final MutableLiveData<List<Sensor>> sensors = new MutableLiveData<>(new ArrayList<>());

    public SensorViewModel() {
        this.repository = MyApp.getInstance().getRepository();
        loadAll();
    }

    public LiveData<List<Sensor>> getSensors() {
        return sensors;
    }

    public void loadAll() {
        new Thread(() -> {
            try {
                List<Sensor> all = repository.getAllSensors();
                sensors.postValue(all);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            sensors.postValue(new ArrayList<>());
            return;
        }

        new Thread(() -> {
            try {
                List<Sensor> results = repository.searchSensors(query.trim());
                sensors.postValue(results);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void clear() {
        sensors.postValue(new ArrayList<>());
    }
}