package com.mikesuvade.focus.ui.temperature;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.TemperatureResult;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TemperatureResultAdapter extends RecyclerView.Adapter<TemperatureResultAdapter.ViewHolder> {

    private List<TemperatureResult> items = new ArrayList<>();
    private final DecimalFormat df = new DecimalFormat("#0.00");
    private OnSaveClickListener saveListener;

    public interface OnSaveClickListener {
        void onSaveClick(TemperatureResult result);
    }

    public void setItems(List<TemperatureResult> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnSaveClickListener(OnSaveClickListener listener) {
        this.saveListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_temperature_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TemperatureResult item = items.get(position);
        holder.bind(item, saveListener, df);  // ← передаём df
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSensorName;
        private final TextView tvValue;
        private final TextView tvTemperature;
        private final Button btnSave;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSensorName = itemView.findViewById(R.id.tvSensorName);
            tvValue = itemView.findViewById(R.id.tvValue);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            btnSave = itemView.findViewById(R.id.btnSave);
        }

        void bind(TemperatureResult item, OnSaveClickListener listener, DecimalFormat df) {
            tvSensorName.setText(item.getSensorName());

            if ("Ом".equals(item.getUnit())) {
                tvValue.setText(df.format(item.getCorrectedValue()) + " Ом");
            } else {
                tvValue.setText(
                        df.format(item.getUserValue()) + " мВ + " +
                                df.format(item.getColdJunctionMv()) + " мВ = " +
                                df.format(item.getTotalMv()) + " мВ"
                );
            }

            tvTemperature.setText(df.format(item.getTemperature()) + " °C");

            btnSave.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSaveClick(item);
                }
            });
        }
    }
}