package com.mikesuvade.focus.ui.main;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.GateValve;

import java.util.ArrayList;
import java.util.List;

public class GateValveAdapter extends RecyclerView.Adapter<GateValveAdapter.ViewHolder> {

    private List<GateValve> valves = new ArrayList<>();
    private List<Integer> expandedPositions = new ArrayList<>();
    private List<Integer> selectedPositions = new ArrayList<>();

    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;
    private OnCheckBoxClickListener checkBoxListener;
    private OnBlockingClickListener blockingClickListener;

    private boolean isUpdating = false;

    // ==================== INTERFACES ====================

    public interface OnItemClickListener {
        void onItemClick(GateValve valve, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(GateValve valve);
    }

    public interface OnCheckBoxClickListener {
        void onCheckBoxClick(GateValve valve, int position, boolean isChecked);
    }

    public interface OnBlockingClickListener {
        void onBlockingClick(GateValve valve, String type);
    }

    // ==================== SETTERS ====================

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnCheckBoxClickListener(OnCheckBoxClickListener listener) {
        this.checkBoxListener = listener;
    }

    public void setOnBlockingClickListener(OnBlockingClickListener listener) {
        this.blockingClickListener = listener;
    }

    // ✅ ГЛАВНЫЙ МЕТОД - обновляет данные и выделение за один раз
    public void updateData(List<GateValve> newValves, List<Integer> newSelectedPositions) {
        this.valves = newValves != null ? newValves : new ArrayList<>();
        this.expandedPositions.clear();
        this.selectedPositions.clear();
        if (newSelectedPositions != null) {
            this.selectedPositions.addAll(newSelectedPositions);
        }
        notifyDataSetChanged();
    }

    public void setValves(List<GateValve> valves) {
        updateData(valves, this.selectedPositions);
    }

    public void setSelectedPositions(List<Integer> positions) {
        updateData(this.valves, positions);
    }

    public List<GateValve> getValves() {
        return valves;
    }

    public void setExpanded(int position, boolean expanded) {
        Log.d("EXPAND", "SetpointAdapter.setExpanded: position=" + position + ", expanded=" + expanded);
        Log.d("EXPAND", "expandedPositions before: " + expandedPositions);

        if (expanded) {
            if (!expandedPositions.contains(position)) {
                expandedPositions.add(position);
                Log.d("EXPAND", "added position " + position + " to expandedPositions");
            }
        } else {
            expandedPositions.remove(Integer.valueOf(position));
            Log.d("EXPAND", "removed position " + position + " from expandedPositions");
        }

        Log.d("EXPAND", "expandedPositions after: " + expandedPositions);
        notifyItemChanged(position);
    }

    public void clearExpanded() {
        expandedPositions.clear();
        notifyDataSetChanged();
    }

    public boolean isSelected(int position) {
        return selectedPositions.contains(position);
    }

    // ==================== ADAPTER METHODS ====================

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gate_valve, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GateValve valve = valves.get(position);
        Log.d("GATE_ADAPTER", "onBindViewHolder: position=" + position + ", id=" + valve.getId());
        boolean isExpanded = expandedPositions.contains(position);
        boolean isSelected = selectedPositions.contains(position);

        holder.bind(valve, isExpanded, isSelected,
                listener, longClickListener, checkBoxListener, blockingClickListener, position);
    }

    @Override
    public int getItemCount() {
        return valves.size();
    }

    // ==================== VIEW HOLDER ====================

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvIsy;
        private final TextView tvName;
        private final TextView tvLocation;
        private final TextView tvOnPlace;
        private final TextView tvFullName;
        private final TextView tvKks;
        private final CheckBox cbAddToList;
        private final LinearLayout expandedContent;
        private final Button btnBlockingOpen;
        private final Button btnBlockingClose;
        private final Button btnExternalChain;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIsy = itemView.findViewById(R.id.tvIsy);
            tvName = itemView.findViewById(R.id.tvName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvOnPlace = itemView.findViewById(R.id.tvOnPlace);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvKks = itemView.findViewById(R.id.tvKks);
            cbAddToList = itemView.findViewById(R.id.cbAddToList);
            expandedContent = itemView.findViewById(R.id.expandedContent);
            btnBlockingOpen = itemView.findViewById(R.id.btnBlockingOpen);
            btnBlockingClose = itemView.findViewById(R.id.btnBlockingClose);
            btnExternalChain = itemView.findViewById(R.id.btnExternalChain);
        }

        void bind(GateValve valve, boolean isExpanded, boolean isSelected,
                  OnItemClickListener listener,
                  OnItemLongClickListener longClickListener,
                  OnCheckBoxClickListener checkBoxListener,
                  OnBlockingClickListener blockingClickListener,
                  int position) {
            Log.d("GATE_ADAPTER", "bind: valve.getId() = " + valve.getId());
            Log.d("GATE_ADAPTER", "bind: valve.getName() = " + valve.getName());
            Log.d("GATE_ADAPTER", "bind: valve.getOriginalId() = " + valve.getOriginalId());

            // ISY и NAME
            String isy = valve.getIsy();
            String name = valve.getName();

            if (name == null || name.isEmpty()) {
                name = valve.getKks();
            }
            if (name == null || name.isEmpty()) {
                name = "Без названия";
            }

            if (isy != null && !isy.isEmpty()) {
                tvIsy.setVisibility(View.VISIBLE);
                tvIsy.setText(isy);
                tvName.setText(name);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                params.setMarginStart(12);
                tvName.setLayoutParams(params);
                tvName.setGravity(Gravity.START);
            } else {
                tvIsy.setVisibility(View.GONE);
                tvName.setText(name);
                tvName.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                tvName.setGravity(Gravity.START);
            }

            // СБОРКА + МЕСТОПОЛОЖЕНИЕ
            String powerCabinet = valve.getPowerCabinet();
            String locationDescription = valve.getLocationDescription();

            String locationText = "";
            if (powerCabinet != null && !powerCabinet.isEmpty()) {
                locationText = powerCabinet;
                if (locationDescription != null && !locationDescription.isEmpty()) {
                    locationText += ", сборка " + locationDescription;
                }
            } else if (locationDescription != null && !locationDescription.isEmpty()) {
                locationText = locationDescription;
            }

            if (!locationText.isEmpty()) {
                tvLocation.setVisibility(View.VISIBLE);
                tvLocation.setText(locationText);
            } else {
                tvLocation.setVisibility(View.GONE);
            }

            // ОПИСАНИЕ
            String fullName = valve.getFullName();
            if (fullName != null && !fullName.isEmpty()) {
                tvFullName.setVisibility(View.VISIBLE);
                tvFullName.setText("Описание: " + fullName);
            } else {
                tvFullName.setVisibility(View.GONE);
            }

            // РАСПОЛОЖЕНИЕ
            String onPlace = valve.getOnPlace();
            if (onPlace != null && !onPlace.isEmpty()) {
                tvOnPlace.setVisibility(View.VISIBLE);
                tvOnPlace.setText("Расположение: " + onPlace);
            } else {
                tvOnPlace.setVisibility(View.GONE);
            }

            // KKS
            String kks = valve.getKks();
            if (kks != null && !kks.isEmpty()) {
                tvKks.setVisibility(View.VISIBLE);
                tvKks.setText("KKS: " + kks);
            } else {
                tvKks.setVisibility(View.GONE);
            }

            // ✅ CHECKBOX - отключаем слушатель, устанавливаем состояние, включаем слушатель
            cbAddToList.setOnCheckedChangeListener(null);
            cbAddToList.setChecked(isSelected);
            cbAddToList.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (checkBoxListener != null) {
                    checkBoxListener.onCheckBoxClick(valve, position, isChecked);
                }
            });

            // БЛОКИРОВКИ
            byte[] nsOpen = valve.getNameSpaceViewOpen();
            if (nsOpen != null && nsOpen.length > 0) {
                btnBlockingOpen.setVisibility(View.VISIBLE);
                btnBlockingOpen.setText("БЛОКИРОВКИ \"ОТКРЫТИЕ\"");
                btnBlockingOpen.setOnClickListener(v -> {
                    if (blockingClickListener != null) {
                        blockingClickListener.onBlockingClick(valve, "open");
                    }
                });
            } else {
                btnBlockingOpen.setVisibility(View.GONE);
            }

            byte[] nsClose = valve.getNamespaceViewClose();
            if (nsClose != null && nsClose.length > 0) {
                btnBlockingClose.setVisibility(View.VISIBLE);
                btnBlockingClose.setText("БЛОКИРОВКИ \"ЗАКРЫТИЕ\"");
                btnBlockingClose.setOnClickListener(v -> {
                    if (blockingClickListener != null) {
                        blockingClickListener.onBlockingClick(valve, "close");
                    }
                });
            } else {
                btnBlockingClose.setVisibility(View.GONE);
            }

            byte[] nsPerifer = valve.getNamespaceViewPerifer();
            if (nsPerifer != null && nsPerifer.length > 0) {
                btnExternalChain.setVisibility(View.VISIBLE);
                btnExternalChain.setText("ВНЕШНИЕ ЦЕПИ");
                btnExternalChain.setOnClickListener(v -> {
                    if (blockingClickListener != null) {
                        blockingClickListener.onBlockingClick(valve, "external");
                    }
                });
            } else {
                btnExternalChain.setVisibility(View.GONE);
            }

            // РАЗВОРАЧИВАЕМАЯ ЧАСТЬ
            boolean hasAnyBlocking = (nsOpen != null && nsOpen.length > 0) ||
                    (nsClose != null && nsClose.length > 0) ||
                    (nsPerifer != null && nsPerifer.length > 0);

            if (hasAnyBlocking && isExpanded) {
                expandedContent.setVisibility(View.VISIBLE);
            } else {
                expandedContent.setVisibility(View.GONE);
            }

            // СЛУШАТЕЛИ
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(valve, position);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    return longClickListener.onItemLongClick(valve);
                }
                return false;
            });
        }
    }
}