package com.mikesuvade.focus.ui.main;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;
    private OnAddToListClickListener addToListListener;
    private OnBlockingClickListener blockingClickListener;

    // ==================== INTERFACES ====================

    public interface OnItemClickListener {
        void onItemClick(GateValve valve, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(GateValve valve);
    }

    public interface OnAddToListClickListener {
        void onAddToListClick(GateValve valve);
    }

    public interface OnBlockingClickListener {
        void onBlockingClick(GateValve valve, String type); // "open", "close", "external"
    }

    // ==================== SETTERS ====================

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnAddToListClickListener(OnAddToListClickListener listener) {
        this.addToListListener = listener;
    }

    public void setOnBlockingClickListener(OnBlockingClickListener listener) {
        this.blockingClickListener = listener;
    }

    public void setValves(List<GateValve> valves) {
        this.valves = valves != null ? valves : new ArrayList<>();
        expandedPositions.clear();
        notifyDataSetChanged();
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
        boolean isExpanded = expandedPositions.contains(position);
        holder.bind(valve, isExpanded);

        // Короткий тап - развернуть/свернуть
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(valve, position);
            }
        });

        // Длинный тап - редактировать
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(valve);
                return true;
            }
            return false;
        });

        // Кнопка + добавить в список
        holder.btnAddToList.setOnClickListener(v -> {
            if (addToListListener != null) {
                addToListListener.onAddToListClick(valve);
            }
        });

        // Блокировки
        holder.btnBlockingOpen.setOnClickListener(v -> {
            if (blockingClickListener != null) {
                blockingClickListener.onBlockingClick(valve, "open");
            }
        });

        holder.btnBlockingClose.setOnClickListener(v -> {
            if (blockingClickListener != null) {
                blockingClickListener.onBlockingClick(valve, "close");
            }
        });

        holder.btnExternalChain.setOnClickListener(v -> {
            if (blockingClickListener != null) {
                blockingClickListener.onBlockingClick(valve, "external");
            }
        });
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
        private final Button btnAddToList;
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
            btnAddToList = itemView.findViewById(R.id.btnAddToList);
            expandedContent = itemView.findViewById(R.id.expandedContent);
            btnBlockingOpen = itemView.findViewById(R.id.btnBlockingOpen);
            btnBlockingClose = itemView.findViewById(R.id.btnBlockingClose);
            btnExternalChain = itemView.findViewById(R.id.btnExternalChain);
        }
        void bind(GateValve valve, boolean isExpanded) {
            // ==========================================
            // ISY и NAME — если ISY пустой, NAME занимает его место
            // ==========================================
            String isy = valve.getIsy();
            String name = valve.getName();

            // Если name пустой — используем kks
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

            // ==========================================
            // СБОРКА + МЕСТОПОЛОЖЕНИЕ (объединённые)
            // ==========================================
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

            // ==========================================
            // ОПИСАНИЕ
            // ==========================================
            String fullName = valve.getFullName();
            if (fullName != null && !fullName.isEmpty()) {
                tvFullName.setVisibility(View.VISIBLE);
                tvFullName.setText("Описание: " + fullName);
            } else {
                tvFullName.setVisibility(View.GONE);
            }

            // ==========================================
            // РАСПОЛОЖЕНИЕ
            // ==========================================
            String onPlace = valve.getOnPlace();
            if (onPlace != null && !onPlace.isEmpty()) {
                tvOnPlace.setVisibility(View.VISIBLE);
                tvOnPlace.setText("Расположение: " + onPlace);
            } else {
                tvOnPlace.setVisibility(View.GONE);
            }

            // ==========================================
            // KKS
            // ==========================================
            String kks = valve.getKks();
            if (kks != null && !kks.isEmpty()) {
                tvKks.setVisibility(View.VISIBLE);
                tvKks.setText("KKS: " + kks);
            } else {
                tvKks.setVisibility(View.GONE);
            }

            // ==========================================
            // РАЗВОРАЧИВАЕМАЯ ЧАСТЬ (блокировки)
            // ==========================================
            expandedContent.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }

    }
}