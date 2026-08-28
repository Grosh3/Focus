package com.mikesuvade.focus.ui.saved;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mikesuvade.focus.domain.models.Measurement;
import com.mikesuvade.focus.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.List;

public class SavedMeasurementsViewModel extends ViewModel {

    private final IRepository repository;
    private final MutableLiveData<List<Measurement>> measurements = new MutableLiveData<>(new ArrayList<>());

    public SavedMeasurementsViewModel(IRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Measurement>> getMeasurements() {
        return measurements;
    }

    public void loadMeasurements() {
        new Thread(() -> {
            try {
                // 🔥 ИСПРАВЛЕНО: используем ПОЛЬЗОВАТЕЛЬСКУЮ БД
                List<Measurement> result = repository.getAllUserMeasurements();
                measurements.postValue(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void renameMeasurement(int id, String newDescription) {
        new Thread(() -> {
            try {
                // 🔥 ИСПРАВЛЕНО: используем ПОЛЬЗОВАТЕЛЬСКУЮ БД
                repository.updateUserMeasurementDescription(id, newDescription);
                loadMeasurements();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void deleteMeasurement(int id) {
        new Thread(() -> {
            try {
                // 🔥 ИСПРАВЛЕНО: используем ПОЛЬЗОВАТЕЛЬСКУЮ БД
                repository.deleteUserMeasurement(id);
                loadMeasurements();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}