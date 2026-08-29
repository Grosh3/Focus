package com.mikesuvade.focus.ui.main;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.Sensor;

import java.util.ArrayList;
import java.util.List;

public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.ViewHolder> {

    private List<Sensor> sensors = new ArrayList<>();
    private List<Integer> expandedPositions = new ArrayList<>();

    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(Sensor sensor, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(Sensor sensor, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void updateData(List<Sensor> newSensors) {
        Log.d("SENSOR_ADAPTER", "=== updateData ===");
        Log.d("SENSOR_ADAPTER", "newSensors size = " + (newSensors != null ? newSensors.size() : 0));

        this.sensors = newSensors != null ? newSensors : new ArrayList<>();
        this.expandedPositions.clear();
        notifyDataSetChanged();

        Log.d("SENSOR_ADAPTER", "adapter now has " + this.sensors.size() + " items");
    }
    public void setExpanded(int position, boolean expanded) {
        if (expanded) {
            if (!expandedPositions.contains(position)) {
                expandedPositions.add(position);
            }
        } else {
            expandedPositions.remove(Integer.valueOf(position));
        }
        notifyItemChanged(position);
    }

    public void clearExpanded() {
        expandedPositions.clear();
        notifyDataSetChanged();
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sensor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Sensor sensor = sensors.get(position);
        boolean isExpanded = expandedPositions.contains(position);
        holder.bind(sensor, isExpanded, listener, longClickListener, position);
    }

    @Override
    public int getItemCount() {
        return sensors.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStMarking;
        private final TextView tvFullName;
        private final TextView tvLocation;
        private final TextView tvKks;
        private final View expandedContent;
        private final TextView tvModelSensor;
        private final TextView tvModSensor;
        private final TextView tvAdditionalInfo;
        private final TextView tvMin;
        private final TextView tvMax;
        private final TextView tvUnitMeasure;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStMarking = itemView.findViewById(R.id.tvStMarking);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvKks = itemView.findViewById(R.id.tvKks);
            expandedContent = itemView.findViewById(R.id.expandedContent);
            tvModelSensor = itemView.findViewById(R.id.tvModelSensor);
            tvModSensor = itemView.findViewById(R.id.tvModSensor);
            tvAdditionalInfo = itemView.findViewById(R.id.tvAdditionalInfo);
            tvMin = itemView.findViewById(R.id.tvMin);
            tvMax = itemView.findViewById(R.id.tvMax);
            tvUnitMeasure = itemView.findViewById(R.id.tvUnitMeasure);
        }

        void bind(Sensor sensor, boolean isExpanded,
                  OnItemClickListener listener,
                  OnItemLongClickListener longClickListener,
                  int position) {

            String stMarking = sensor.getStMarkir();
            if (stMarking != null && !stMarking.isEmpty()) {
                tvStMarking.setVisibility(View.VISIBLE);
                tvStMarking.setText(stMarking);
            } else {
                tvStMarking.setVisibility(View.GONE);
            }

            String fullName = sensor.getFullName();
            if (fullName != null && !fullName.isEmpty()) {
                tvFullName.setVisibility(View.VISIBLE);
                tvFullName.setText(fullName);
            } else {
                tvFullName.setVisibility(View.GONE);
            }

            String location = sensor.getLocation();
            if (location != null && !location.isEmpty()) {
                tvLocation.setVisibility(View.VISIBLE);
                tvLocation.setText(location);
            } else {
                tvLocation.setVisibility(View.GONE);
            }

            String kks = sensor.getKks();
            if (kks != null && !kks.isEmpty()) {
                tvKks.setVisibility(View.VISIBLE);
                tvKks.setText("KKS: " + kks);
            } else {
                tvKks.setVisibility(View.GONE);
            }

            if (isExpanded) {
                expandedContent.setVisibility(View.VISIBLE);

                String modelSensor = sensor.getModelSensor();
                if (modelSensor != null && !modelSensor.isEmpty()) {
                    tvModelSensor.setVisibility(View.VISIBLE);
                    tvModelSensor.setText("Модель: " + modelSensor);
                } else {
                    tvModelSensor.setVisibility(View.GONE);
                }

                String modSensor = sensor.getModSensor();
                if (modSensor != null && !modSensor.isEmpty()) {
                    tvModSensor.setVisibility(View.VISIBLE);
                    tvModSensor.setText("Мод: " + modSensor);
                } else {
                    tvModSensor.setVisibility(View.GONE);
                }

                String additionalInfo = sensor.getAdditionalInfo();
                if (additionalInfo != null && !additionalInfo.isEmpty()) {
                    tvAdditionalInfo.setVisibility(View.VISIBLE);
                    tvAdditionalInfo.setText("Доп: " + additionalInfo);
                } else {
                    tvAdditionalInfo.setVisibility(View.GONE);
                }

                double min = sensor.getMinVal();
                double max = sensor.getMaxVal();
                String unit = sensor.getMeasureUnit();
                if (min != 0 || max != 0 || (unit != null && !unit.isEmpty())) {
                    tvMin.setVisibility(View.VISIBLE);
                    tvMax.setVisibility(View.VISIBLE);
                    tvUnitMeasure.setVisibility(View.VISIBLE);
                    tvMin.setText("Мин: " + min);
                    tvMax.setText("Макс: " + max);
                    tvUnitMeasure.setText("Ед.изм: " + (unit != null ? unit : ""));
                } else {
                    tvMin.setVisibility(View.GONE);
                    tvMax.setVisibility(View.GONE);
                    tvUnitMeasure.setVisibility(View.GONE);
                }

            } else {
                expandedContent.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(sensor, position);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    return longClickListener.onItemLongClick(sensor, position);
                }
                return false;
            });
        }
    }
}