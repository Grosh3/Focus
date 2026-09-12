package com.mikesuvade.focus.ui.main;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.Setpoint;

import java.util.ArrayList;
import java.util.List;
import androidx.core.content.ContextCompat;

public class SetpointAdapter extends RecyclerView.Adapter<SetpointAdapter.ViewHolder> {

    private List<Setpoint> setpoints = new ArrayList<>();
    private List<Integer> expandedPositions = new ArrayList<>();

    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(Setpoint setpoint, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(Setpoint setpoint, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
        Log.d("SETPOINT_ADAPTER", "setOnItemClickListener: listener=" + (listener != null ? "NOT NULL" : "NULL"));
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void updateData(List<Setpoint> newSetpoints) {
        Log.d("SETPOINT_ADAPTER", "updateData: size=" + (newSetpoints != null ? newSetpoints.size() : 0));
        this.setpoints = newSetpoints != null ? newSetpoints : new ArrayList<>();
        this.expandedPositions.clear();
        notifyDataSetChanged();
        // 🔥 ПРИНУДИТЕЛЬНО ОБНОВЛЯЕМ ВСЕ ВЬЮХИ
        notifyItemRangeChanged(0, this.setpoints.size());
    }

    public void setExpanded(int position, boolean expanded) {
        Log.d("EXPAND_SETPOINT", "setExpanded: position=" + position + ", expanded=" + expanded);
        Log.d("EXPAND_SETPOINT", "expandedPositions before: " + expandedPositions);

        if (expanded) {
            if (!expandedPositions.contains(position)) {
                expandedPositions.add(position);
                Log.d("EXPAND_SETPOINT", "added position " + position);
            }
        } else {
            expandedPositions.remove(Integer.valueOf(position));
            Log.d("EXPAND_SETPOINT", "removed position " + position);
        }

        Log.d("EXPAND_SETPOINT", "expandedPositions after: " + expandedPositions);
        notifyItemChanged(position);
    }

    public void clearExpanded() {
        expandedPositions.clear();
        notifyDataSetChanged();
    }

    public List<Setpoint> getSetpoints() {
        return setpoints;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_setpoint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Setpoint setpoint = setpoints.get(position);
        boolean isExpanded = expandedPositions.contains(position);
        holder.bind(setpoint, isExpanded, listener, longClickListener, position);
    }

    @Override
    public int getItemCount() {
        return setpoints.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPositionName;
        private final TextView tvName;
        private final TextView tvSetpointValue;
        private final TextView tvOperation;
        private final TextView tvDelayTime;
        private final View expandedContent;
        private final TextView tvLocation;
        private final TextView tvEquipmentGroup;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPositionName = itemView.findViewById(R.id.tvPositionName);
            tvName = itemView.findViewById(R.id.tvName);
            tvSetpointValue = itemView.findViewById(R.id.tvSetpointValue);
            tvOperation = itemView.findViewById(R.id.tvOperation);
            tvDelayTime = itemView.findViewById(R.id.tvDelayTime);
            expandedContent = itemView.findViewById(R.id.expandedContent);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvEquipmentGroup = itemView.findViewById(R.id.tvEquipmentGroup);
        }

        void bind(Setpoint setpoint, boolean isExpanded,
                  OnItemClickListener listener,
                  OnItemLongClickListener longClickListener,
                  int position) {

            Log.d("SETPOINT_BIND", "=== bind START ===");
            Log.d("SETPOINT_BIND", "position=" + position);
            Log.d("SETPOINT_BIND", "setpoint=" + setpoint.getPositionName());
            Log.d("SETPOINT_BIND", "isExpanded=" + isExpanded);
            Log.d("SETPOINT_BIND", "listener=" + (listener != null ? "NOT NULL" : "NULL"));

            // Позиция
            String positionName = setpoint.getPositionName();
            if (positionName != null && !positionName.isEmpty()) {
                tvPositionName.setVisibility(View.VISIBLE);
                tvPositionName.setText(positionName);
            } else {
                tvPositionName.setVisibility(View.GONE);
            }

            // Наименование + " до"
            String name = setpoint.getName();
            String setpointValue = setpoint.getSetpointValue();
            String displayName = name;
            if (name != null && !name.isEmpty() && setpointValue != null && !setpointValue.isEmpty()) {
                displayName = name + " до";
            }
            if (displayName != null && !displayName.isEmpty()) {
                tvName.setVisibility(View.VISIBLE);
                tvName.setText(displayName);
            } else {
                tvName.setVisibility(View.GONE);
            }

            // Уставка (жирным)
            if (setpointValue != null && !setpointValue.isEmpty()) {
                tvSetpointValue.setVisibility(View.VISIBLE);
                tvSetpointValue.setText(setpointValue);
            } else {
                tvSetpointValue.setVisibility(View.GONE);
            }

            // Операция + Notes в скобках
            String operation = setpoint.getOperation();
            String notes = setpoint.getNotes();
            if (operation != null && !operation.isEmpty()) {
                tvOperation.setVisibility(View.VISIBLE);
                String operationText = "Операция: " + operation;
                if (notes != null && !notes.isEmpty() && !notes.equals("-") && !notes.equals("—")) {
                    operationText += " (" + notes + ")";
                }
                tvOperation.setText(operationText);

                if (isCriticalOperation(operation)) {
                    tvOperation.setTextColor(ContextCompat.getColor(
                            itemView.getContext(), R.color.critical_operation_red));
                } else {
                    tvOperation.setTextColor(ContextCompat.getColor(
                            itemView.getContext(), R.color.operation_default));
                }
            } else {
                tvOperation.setVisibility(View.GONE);
            }

            // Выдержка (всегда видна, если есть значение)
            String delayTime = setpoint.getDelayTime();
            if (delayTime != null && !delayTime.isEmpty() && !delayTime.equals("-") && !delayTime.equals("—")) {
                tvDelayTime.setVisibility(View.VISIBLE);
                tvDelayTime.setText("Выдержка: " + delayTime);
            } else {
                tvDelayTime.setVisibility(View.GONE);
            }

            // Разворачиваемая часть
            if (isExpanded) {
                expandedContent.setVisibility(View.VISIBLE);

                // Расположение (в баяне)
                String location = setpoint.getLocation();
                if (location != null && !location.isEmpty()) {
                    tvLocation.setVisibility(View.VISIBLE);
                    tvLocation.setText("Расположение: " + location);
                } else {
                    tvLocation.setVisibility(View.GONE);
                }

                // ГРУППА ОБОРУДОВАНИЯ (в баяне)
                String equipmentGroup = setpoint.getEquipmentGroup();
                if (equipmentGroup != null && !equipmentGroup.isEmpty()) {
                    tvEquipmentGroup.setVisibility(View.VISIBLE);
                    tvEquipmentGroup.setText("Группа: " + equipmentGroup);
                } else {
                    tvEquipmentGroup.setVisibility(View.GONE);
                }

            } else {
                expandedContent.setVisibility(View.GONE);
            }

            // Слушатели
            itemView.setOnClickListener(v -> {
                Log.d("SETPOINT_CLICK", "!!! itemView CLICKED !!! position=" + position);
                if (listener != null) {
                    listener.onItemClick(setpoint, position);
                } else {
                    Log.e("SETPOINT_CLICK", "❌ listener is NULL!");
                }
            });

            itemView.setOnLongClickListener(v -> {
                Log.d("SETPOINT_LONG_CLICK", "!!! itemView LONG CLICKED !!! position=" + position);
                if (longClickListener != null) {
                    return longClickListener.onItemLongClick(setpoint, position);
                }
                return false;
            });
        }
        /**
         * Проверяет, является ли операция критической (отключение)
         */
        private boolean isCriticalOperation(String operation) {
            if (operation == null || operation.isEmpty()) return false;

            String[] criticalOps = itemView.getContext().getResources()
                    .getStringArray(R.array.critical_operations);

            for (String critical : criticalOps) {
                if (operation.contains(critical)) {
                    return true;
                }
            }
            return false;
        }
    }
}