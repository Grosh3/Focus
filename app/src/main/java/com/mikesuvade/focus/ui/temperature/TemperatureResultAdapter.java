package com.mikesuvade.focus.ui.temperature;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.TemperatureResult;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TemperatureResultAdapter extends RecyclerView.Adapter<TemperatureResultAdapter.ViewHolder> {

    private List<TemperatureResult> items = new ArrayList<>();
    private OnSaveClickListener saveListener;

    private final DecimalFormat df = new DecimalFormat("#0.00");
    private final DecimalFormat dfTemp = new DecimalFormat("#0.0");

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
        holder.bind(item, saveListener, df, dfTemp);
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
            tvSensorName  = itemView.findViewById(R.id.tvSensorName);
            tvValue       = itemView.findViewById(R.id.tvValue);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            btnSave       = itemView.findViewById(R.id.btnSave);
        }

        void bind(TemperatureResult item, OnSaveClickListener listener,
                  DecimalFormat df, DecimalFormat dfTemp) {
            tvSensorName.setText(item.getSensorName());

            if (item.isOutOfRange()) {
                // 🔥 ВНЕ ДИАПАЗОНА — "за пределом!"
                tvValue.setText("за пределом!");
                tvValue.setTextColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.temp_color_alarm));

                tvTemperature.setText("");
                tvTemperature.setTextColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.temp_color_alarm));

                btnSave.setEnabled(false);
                btnSave.setAlpha(0.4f);
                btnSave.setOnClickListener(null);
            } else {
                // 🔥 НОРМА
                tvValue.setTextColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.on_surface_secondary));
                tvTemperature.setTextColor(getTempColor(itemView.getContext(), item));

                if ("Ом".equals(item.getUnit())) {
                    tvValue.setText(df.format(item.getCorrectedValue()) + " Ом");
                } else {
                    tvValue.setText(
                            df.format(item.getUserValue()) + " мВ + " +
                                    df.format(item.getColdJunctionMv()) + " мВ = " +
                                    df.format(item.getTotalMv()) + " мВ"
                    );
                }

                tvTemperature.setText(dfTemp.format(item.getTemperature()) + " °C");

                btnSave.setEnabled(true);
                btnSave.setAlpha(1f);
                btnSave.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onSaveClick(item);
                    }
                });
            }
        }

        /**
         * Цвет температуры:
         * - зелёный, если в норме
         * - красный, если температура выше порога для своего датчика:
         *     Ом-датчики → > 80
         *     ХА         → > 550
         *     ХК         → > 80
         */
        private int getTempColor(Context context, TemperatureResult item) {
            double t = item.getTemperature();
            String sensor = item.getSensorName() != null ? item.getSensorName() : "";

            boolean alarm;
            if (sensor.equals("ХА")) {
                alarm = t > context.getResources().getInteger(R.integer.temp_threshold_ha);
            } else if (sensor.equals("ХК")) {
                alarm = t > context.getResources().getInteger(R.integer.temp_threshold_hk);
            } else {
                // все Ом-датчики: ТСП50П, ТСМ50М, Гр21 (46П), Гр23 (53М)
                alarm = t > context.getResources().getInteger(R.integer.temp_threshold_ohm);
            }

            return ContextCompat.getColor(context,
                    alarm ? R.color.temp_color_alarm : R.color.temp_color_normal);
        }
    }
}