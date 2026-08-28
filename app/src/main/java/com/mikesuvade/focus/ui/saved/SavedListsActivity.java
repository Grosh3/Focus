package com.mikesuvade.focus.ui.saved;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.ValveWorkSession;
import com.mikesuvade.focus.ui.list.ListDetailActivity;
import com.mikesuvade.focus.utils.AppState;

public class SavedListsActivity extends AppCompatActivity {

    private SavedListsViewModel viewModel;
    private SavedListAdapter adapter;
    private TextView tvEmpty;
    private RecyclerView rvSavedLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_lists);

        viewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new SavedListsViewModel(
                                ((MyApp) getApplication()).getRepository()
                        );
                    }
                }
        ).get(SavedListsViewModel.class);

        initViews();
        setupRecyclerView();
        setupObservers();

        viewModel.loadSessions();
    }

    private void initViews() {
        rvSavedLists = findViewById(R.id.rvSavedLists);
        tvEmpty = findViewById(R.id.tvEmpty);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvSavedLists.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SavedListAdapter();
        adapter.setListener(new SavedListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ValveWorkSession session) {
                AppState.getInstance().setActiveSession(session);
                AppState.getInstance().setHasUnsavedChanges(false);

                Intent intent = new Intent(SavedListsActivity.this, ListDetailActivity.class);
                intent.putExtra("session_id", session.getSessionId());
                intent.putExtra("use_user_db", true);  // 🔥 ДОБАВЛЕНО
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(ValveWorkSession session) {
                showItemOptionsDialog(session);
            }
        });
        rvSavedLists.setAdapter(adapter);
    }

    private void setupObservers() {
        Log.d("SESSY", "=== SavedListsActivity.setupObservers START ===");
        viewModel.getSessions().observe(this, sessions -> {
            Log.d("SESSY", "SavedListsActivity: sessions observer triggered");
            Log.d("SESSY", "sessions size = " + (sessions != null ? sessions.size() : 0));
            if (sessions != null && !sessions.isEmpty()) {
                adapter.setSessions(sessions);
                rvSavedLists.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
                Log.d("SESSY", "Showing " + sessions.size() + " sessions");
            } else {
                rvSavedLists.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                Log.d("SESSY", "No sessions to show");
            }
        });
        Log.d("SESSY", "=== SavedListsActivity.setupObservers END ===");
    }

    private void showItemOptionsDialog(ValveWorkSession session) {
        String[] options = {"Переименовать", "Удалить"};

        new AlertDialog.Builder(this)
                .setTitle(session.getEquipmentDescription())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showRenameDialog(session);
                    } else {
                        showDeleteDialog(session);
                    }
                })
                .show();
    }

    private void showRenameDialog(ValveWorkSession session) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_rename_title);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(session.getEquipmentDescription());
        input.setSelection(input.getText().length());
        builder.setView(input);

        builder.setPositiveButton(R.string.dialog_rename_positive, (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                viewModel.renameSession(session.getSessionId(), newName);
                Toast.makeText(this, R.string.toast_renamed, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.dialog_rename_negative, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showDeleteDialog(ValveWorkSession session) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.saved_lists_delete_title)
                .setMessage(getString(R.string.saved_lists_delete_message, session.getEquipmentDescription()))
                .setPositiveButton("Да", (dialog, which) -> {
                    viewModel.deleteSession(session.getSessionId());
                    Toast.makeText(this, R.string.saved_lists_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Нет", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new android.os.Handler().postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) {
                viewModel.loadSessions();
            }
        }, 300);
    }
}