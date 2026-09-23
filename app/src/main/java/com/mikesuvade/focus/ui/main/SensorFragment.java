package com.mikesuvade.focus.ui.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.R;

import java.util.ArrayList;
import java.util.List;

public class SensorFragment extends Fragment {

    private EditText etSearch;
    private RecyclerView rvSensors;
    private TextView tvEmpty;
    private SensorAdapter adapter;
    private SensorViewModel viewModel;

    private final List<Integer> expandedPositions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sensor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearch = view.findViewById(R.id.etSearch);
        rvSensors = view.findViewById(R.id.rvSensors);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        setupViewModel();
        setupRecyclerView();
        setupSearch();

        // Автофокус на поиск
      //  etSearch.requestFocus();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SensorViewModel.class);
        viewModel.getSensors().observe(getViewLifecycleOwner(), sensors -> {
            if (sensors != null && !sensors.isEmpty()) {
                adapter.updateData(sensors);
                rvSensors.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                adapter.updateData(new ArrayList<>());
                rvSensors.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupRecyclerView() {
        rvSensors.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SensorAdapter();

        adapter.setOnItemClickListener((sensor, position) -> {
            toggleExpanded(position);
        });

        adapter.setOnItemLongClickListener((sensor, position) -> {
            // TODO: Показать диалог редактирования/удаления
            return true;
        });

        rvSensors.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    viewModel.search(query);
                } else {
                    viewModel.clear();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void toggleExpanded(int position) {
        if (expandedPositions.contains(position)) {
            expandedPositions.remove(Integer.valueOf(position));
            adapter.setExpanded(position, false);
            return;
        }

        int previousExpanded = -1;
        if (!expandedPositions.isEmpty()) {
            previousExpanded = expandedPositions.get(0);
        }

        expandedPositions.clear();

        if (previousExpanded != -1 && previousExpanded != position) {
            adapter.setExpanded(previousExpanded, false);
        }

        expandedPositions.add(position);
        adapter.setExpanded(position, true);
    }
}