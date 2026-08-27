package com.mikesuvade.focus.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.domain.models.Setpoint;
import com.mikesuvade.focus.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.List;

public class SetpointViewModel extends ViewModel {

    private final IRepository repository;
    private final MutableLiveData<List<Setpoint>> setpoints = new MutableLiveData<>(new ArrayList<>());

    public SetpointViewModel() {
        this.repository = MyApp.getInstance().getRepository();
        loadAll();
    }

    public LiveData<List<Setpoint>> getSetpoints() {
        return setpoints;
    }

    public void loadAll() {
        new Thread(() -> {
            try {
                List<Setpoint> all = repository.getAllSetpoints();
                setpoints.postValue(all);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            setpoints.postValue(new ArrayList<>());
            return;
        }

        new Thread(() -> {
            try {
                List<Setpoint> results = repository.searchSetpoints(query.trim());
                setpoints.postValue(results);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void clear() {
        setpoints.postValue(new ArrayList<>());
    }
}