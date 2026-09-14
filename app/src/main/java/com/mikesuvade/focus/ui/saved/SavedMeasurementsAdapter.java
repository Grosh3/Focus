package com.mikesuvade.focus.ui.saved;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.Measurement;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavedMeasurementsAdapter extends RecyclerView.Adapter<SavedMeasurementsAdapter.ViewHolder> {

    private List<Measurement> items = new ArrayList<>();
    private OnItemClickListener listener;
    private final DecimalFormat df = new DecimalFormat("#0.00");
    private final DecimalFormat dfTemp = new DecimalFormat("#0.0");

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
        holder.bind(item, listener, df, dfTemp);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDescription;
        private final TextView tvSensorType;
        private final TextView tvInfo;
        private final TextView tvTemperature;
        private final TextView tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvSensorType  = itemView.findViewById(R.id.tvSensorType);
            tvInfo        = itemView.findViewById(R.id.tvInfo);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            tvDate        = itemView.findViewById(R.id.tvDate);
        }

        void bind(Measurement measurement, OnItemClickListener listener,
                  DecimalFormat df, DecimalFormat dfTemp) {

            // ===== ОПИСАНИЕ =====
            String description = measurement.getDescription();
            if (description != null && !description.isEmpty()) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(description);
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // ===== ТИП ДАТЧИКА =====
            String sensorType = measurement.getSensorType();
            if (sensorType != null && !sensorType.isEmpty()) {
                tvSensorType.setVisibility(View.VISIBLE);
                tvSensorType.setText(sensorType);
            } else {
                tvSensorType.setVisibility(View.GONE);
            }

            // ===== ЗНАЧЕНИЕ (мелкое) =====
            String valueInfo;
            if ("мВ".equals(measurement.getUnit())) {
                valueInfo = "(" + df.format(measurement.getInputValue()) + " + " +
                        df.format(measurement.getColdJunctionMv()) + ") мВ";
            } else {
                if (measurement.getLineResistance() > 0.001) {
                    valueInfo = "(" + df.format(measurement.getInputValue()) + " + " +
                            df.format(measurement.getLineResistance()) + ") Ом";
                } else {
                    valueInfo = df.format(measurement.getInputValue()) + " Ом";
                }
            }
            tvInfo.setText(valueInfo);

            // ===== ТЕМПЕРАТУРА (крупно, всегда зелёная) =====
            tvTemperature.setTextColor(0xFF2E7D32);
            tvTemperature.setText(dfTemp.format(measurement.getTemperature()) + " °C");

            // ===== ДАТА =====
            String date = measurement.getCreatedAt();
            if (date != null && !date.isEmpty()) {
                tvDate.setVisibility(View.VISIBLE);
                tvDate.setText("📅 " + formatDate(date));
            } else {
                tvDate.setVisibility(View.GONE);
            }

            // ===== КЛИКИ =====
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

        /**
         * Цвет температуры для сохранённого замера:
         * - зелёный, если в норме
         * - красный, если температура выше порога для своего датчика
         *   (Ом: >80, ХА: >550, ХК: >80)
         */



        private String formatDate(String rawDate) {
            // "2026-09-12 21:03:45" → "12.09.2026 21:03"
            try {
                SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                Date d = inFormat.parse(rawDate);
                if (d != null) return outFormat.format(d);
            } catch (ParseException e) {
                // формат не совпал — вернём как есть
            }
            return rawDate;
        }
    }
}