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
                List<Measurement> result = repository.getAllUserMeasurements();
                measurements.postValue(result);
            } catch (android.database.sqlite.SQLiteException e) {
                measurements.postValue(new ArrayList<>());
            } catch (RuntimeException e) {
                measurements.postValue(new ArrayList<>());
            }
        }).start();
    }

    public void renameMeasurement(int id, String newDescription) {
        new Thread(() -> {
            try {
                repository.updateUserMeasurementDescription(id, newDescription);
            } catch (android.database.sqlite.SQLiteException e) {
                // ignore
            } catch (RuntimeException e) {
                // ignore
            }
            loadMeasurements();
        }).start();
    }
    public void deleteMeasurement(int id) {
        new Thread(() -> {
            try {
                repository.deleteUserMeasurement(id);
            } catch (android.database.sqlite.SQLiteException e) {
                // ignore
            } catch (RuntimeException e) {
                // ignore
            }
            loadMeasurements();
        }).start();
    }
}