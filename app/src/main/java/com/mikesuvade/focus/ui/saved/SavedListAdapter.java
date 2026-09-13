package com.mikesuvade.focus.ui.saved;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.ValveWorkSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavedListAdapter extends RecyclerView.Adapter<SavedListAdapter.ViewHolder> {

    private List<ValveWorkSession> sessions = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ValveWorkSession session);
        void onItemLongClick(ValveWorkSession session);
    }

    public void setSessions(List<ValveWorkSession> sessions) {
        Log.d("SESSY", "SavedListAdapter.setSessions: " + (sessions != null ? sessions.size() : 0));
        this.sessions = sessions != null ? sessions : new ArrayList<>();
        Log.d("SESSY", "SavedListAdapter: calling notifyDataSetChanged()");
        notifyDataSetChanged();
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ValveWorkSession session = sessions.get(position);
        holder.bind(session, listener);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvListName;
        private final TextView tvListDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvListName = itemView.findViewById(R.id.tvListName);
            tvListDate = itemView.findViewById(R.id.tvListDate);
        }

        void bind(ValveWorkSession session, OnItemClickListener listener) {
            tvListName.setText(session.getEquipmentDescription());

            String saveDate = session.getSaveDate();
            if (saveDate != null && !saveDate.isEmpty()) {
                tvListDate.setVisibility(View.VISIBLE);
                tvListDate.setText("📅 " + formatDate(saveDate));
            } else {
                tvListDate.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(session);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onItemLongClick(session);
                }
                return true;
            });

        }
        private static String formatDate(String raw) {
            if (raw == null || raw.isEmpty()) return "";
            try {
                SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat out = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                Date d = in.parse(raw);
                return d != null ? out.format(d) : raw;
            } catch (Exception e) {
                return raw;
            }
        }
    }
}