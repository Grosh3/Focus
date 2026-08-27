package com.mikesuvade.focus.ui.saved;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.Measurement;

import java.util.ArrayList;
import java.util.List;

public class SavedMeasurementsAdapter extends RecyclerView.Adapter<SavedMeasurementsAdapter.ViewHolder> {

    private List<Measurement> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Measurement measurement);
        void onItemLongClick(Measurement measurement);
    }

    public void setItems(List<Measurement> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_measurement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Measurement item = items.get(position);
        holder.bind(item, listener);  // ← ПЕРЕДАЁМ listener В КАЧЕСТВЕ ПАРАМЕТРА
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDescription;
        private final TextView tvInfo;
        private final TextView tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        void bind(Measurement measurement, OnItemClickListener listener) {  // ← ПРИНИМАЕМ listener
            // Описание
            String description = measurement.getDescription();
            if (description != null && !description.isEmpty()) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(description);
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // Информация о замере
            String info;
            if ("мВ".equals(measurement.getUnit())) {
                info = "(" + measurement.getInputValue() + " + " +
                        String.format("%.2f", measurement.getColdJunctionMv()) + ") мВ → " +
                        String.format("%.2f", measurement.getTemperature()) + " °C";
            } else {
                if (measurement.getLineResistance() > 0.001) {
                    info = "(" + measurement.getInputValue() + " + " +
                            String.format("%.2f", measurement.getLineResistance()) + ") Ом → " +
                            String.format("%.2f", measurement.getTemperature()) + " °C";
                } else {
                    info = measurement.getInputValue() + " Ом → " +
                            String.format("%.2f", measurement.getTemperature()) + " °C";
                }
            }
            tvInfo.setText(info);

            // Дата
            String date = measurement.getCreatedAt();
            if (date != null && !date.isEmpty()) {
                tvDate.setVisibility(View.VISIBLE);
                tvDate.setText("📅 " + date);
            } else {
                tvDate.setVisibility(View.GONE);
            }

            // ✅ КЛИКИ — используем переданный listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(measurement);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onItemLongClick(measurement);
                }
                return true;
            });
        }
    }
}