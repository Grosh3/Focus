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

public class SetpointFragment extends Fragment {

    private EditText etSearch;
    private RecyclerView rvSetpoints;
    private TextView tvEmpty;
    private SetpointAdapter adapter;
    private SetpointViewModel viewModel;

    private final List<Integer> expandedPositions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setpoint, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearch = view.findViewById(R.id.etSearch);
        rvSetpoints = view.findViewById(R.id.rvSetpoints);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        setupViewModel();
        setupRecyclerView();
        setupSearch();

        // Автофокус на поиск
      //  etSearch.requestFocus();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SetpointViewModel.class);
        viewModel.getSetpoints().observe(getViewLifecycleOwner(), setpoints -> {
            if (setpoints != null && !setpoints.isEmpty()) {
                adapter.updateData(setpoints);
                rvSetpoints.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                adapter.updateData(new ArrayList<>());
                rvSetpoints.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupRecyclerView() {
        rvSetpoints.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SetpointAdapter();

        adapter.setOnItemClickListener((setpoint, position) -> {
            toggleExpanded(position);
        });

        adapter.setOnItemLongClickListener((setpoint, position) -> {
            // TODO: Показать диалог редактирования/удаления
            return true;
        });

        rvSetpoints.setAdapter(adapter);
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