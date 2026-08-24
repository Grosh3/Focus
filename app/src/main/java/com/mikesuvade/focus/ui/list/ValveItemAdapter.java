package com.mikesuvade.focus.ui.list;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.ValveItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ValveItemAdapter extends RecyclerView.Adapter<ValveItemAdapter.ViewHolder> {

    private List<ValveItem> items = new ArrayList<>();
    private OnItemClickListener listener;
    private final Context context;

    public interface OnItemClickListener {
        void onMoveClick(ValveItem item);
        void onMotorClick(ValveItem item);
        void onBoxClick(ValveItem item);
        void onCheckedClick(ValveItem item, boolean isChecked);
        void onItemLongClick(ValveItem item);
        void onItemClick(ValveItem item);  // ← ДОБАВЛЕНО
    }

    public ValveItemAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<ValveItem> items) {
        Log.d("LIST_DEBUG", "=== ValveItemAdapter.setItems ===");
        Log.d("LIST_DEBUG", "New items size: " + (items != null ? items.size() : 0));
        if (items != null) {
            for (ValveItem item : items) {
                Log.d("LIST_DEBUG", "  item: gateValveId=" + item.getGateValveId() +
                        ", assembled=" + item.getIsAssembled() +
                        ", checked=" + item.getIsChecked());
            }
        }
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
        Log.d("LIST_DEBUG", "=== ValveItemAdapter.setItems END ===");
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_valve_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ValveItem item = items.get(position);
        GateValve valve = ((MyApp) context.getApplicationContext())
                .getRepository()
                .getGateValveById(item.getGateValveId());
        holder.bind(item, valve, listener, context, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox cbChecked;
        private final TextView tvIsy;
        private final TextView tvName;
        private final TextView tvAssembleStatus;
        private final TextView tvMotorStatus;
        private final TextView tvBoxStatus;
        private final TextView tvCheckedAt;
        private final ImageButton btnMove;
        private final ImageButton btnMotor;
        private final ImageButton btnBox;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbChecked = itemView.findViewById(R.id.cbChecked);
            tvIsy = itemView.findViewById(R.id.tvIsy);
            tvName = itemView.findViewById(R.id.tvName);
            tvAssembleStatus = itemView.findViewById(R.id.tvAssembleStatus);
            tvMotorStatus = itemView.findViewById(R.id.tvMotorStatus);
            tvBoxStatus = itemView.findViewById(R.id.tvBoxStatus);
            tvCheckedAt = itemView.findViewById(R.id.tvCheckedAt);
            btnMove = itemView.findViewById(R.id.btnMove);
            btnMotor = itemView.findViewById(R.id.btnMotor);
            btnBox = itemView.findViewById(R.id.btnBox);
        }

        void bind(ValveItem item, GateValve valve, OnItemClickListener listener, Context context, int position) {
            Log.d("LIST_DEBUG", "=== bind ===");
            Log.d("LIST_DEBUG", "position=" + position + ", gateValveId=" + item.getGateValveId() +
                    ", isChecked=" + item.getIsChecked());

            // ==========================================
            // ISY и NAME из GateValve
            // ==========================================
            if (valve != null) {
                String isy = valve.getIsy();
                if (isy != null && !isy.isEmpty()) {
                    tvIsy.setVisibility(View.VISIBLE);
                    tvIsy.setText(isy);
                } else {
                    tvIsy.setVisibility(View.GONE);
                }

                String name = valve.getName();
                if (name == null || name.isEmpty()) {
                    name = context.getString(R.string.no_name);
                }
                tvName.setText(name);
            } else {
                tvIsy.setVisibility(View.GONE);
                tvName.setText(context.getString(R.string.no_name));
            }

            // ==========================================
            // СТАТУСЫ (из ValveItem)
            // ==========================================

            // 1. Статус собрано/разобрано
            tvAssembleStatus.setVisibility(View.VISIBLE);
            if (item.getIsChecked() == 0) {
                if (item.getIsAssembled() == 1) {
                    tvAssembleStatus.setText(context.getString(R.string.status_assemble));
                    tvAssembleStatus.setTextColor(0xFF4CAF50);
                } else {
                    tvAssembleStatus.setText(context.getString(R.string.status_disassemble));
                    tvAssembleStatus.setTextColor(0xFFFF0000);
                }
            } else {
                if (item.getIsAssembled() == 1) {
                    tvAssembleStatus.setText(context.getString(R.string.status_assembled));
                    tvAssembleStatus.setTextColor(0xFF4CAF50);
                } else {
                    tvAssembleStatus.setText(context.getString(R.string.status_disassembled));
                    tvAssembleStatus.setTextColor(0xFFFF0000);
                }
            }

            // 2. Статус двигателя
            if (item.getIsChecked() == 1 && item.getMotorDisabled() == 1) {
                tvMotorStatus.setVisibility(View.VISIBLE);
                if (item.getIsAssembled() == 1) {
                    tvMotorStatus.setText(context.getString(R.string.status_motor_connected));
                    tvMotorStatus.setTextColor(0xFF4CAF50);
                } else {
                    tvMotorStatus.setText(context.getString(R.string.status_motor_disconnected));
                    tvMotorStatus.setTextColor(0xFFFF0000);
                }
            } else {
                tvMotorStatus.setVisibility(View.GONE);
            }

            // 3. Статус коробки КВ
            if (item.getIsChecked() == 1 && item.getBoxRemoved() == 1) {
                tvBoxStatus.setVisibility(View.VISIBLE);
                if (item.getIsAssembled() == 1) {
                    tvBoxStatus.setText(context.getString(R.string.status_box_installed));
                    tvBoxStatus.setTextColor(0xFF4CAF50);
                } else {
                    tvBoxStatus.setText(context.getString(R.string.status_box_removed));
                    tvBoxStatus.setTextColor(0xFFFF8800);
                }
            } else {
                tvBoxStatus.setVisibility(View.GONE);
            }

            // ==========================================
            // ЗАЧЁРКИВАНИЕ
            // ==========================================
            if (item.getIsChecked() == 1) {
                tvIsy.setPaintFlags(tvIsy.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvIsy.setAlpha(0.5f);
                tvName.setPaintFlags(tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvName.setAlpha(0.5f);
                tvAssembleStatus.setPaintFlags(tvAssembleStatus.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvAssembleStatus.setAlpha(0.5f);
                if (tvMotorStatus.getVisibility() == View.VISIBLE) {
                    tvMotorStatus.setPaintFlags(tvMotorStatus.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    tvMotorStatus.setAlpha(0.5f);
                }
                if (tvBoxStatus.getVisibility() == View.VISIBLE) {
                    tvBoxStatus.setPaintFlags(tvBoxStatus.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    tvBoxStatus.setAlpha(0.5f);
                }
            } else {
                tvIsy.setPaintFlags(tvIsy.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvIsy.setAlpha(1.0f);
                tvName.setPaintFlags(tvName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvName.setAlpha(1.0f);
                tvAssembleStatus.setPaintFlags(tvAssembleStatus.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvAssembleStatus.setAlpha(1.0f);
            }

            // Дата выполнения - только время (ЧЧ:ММ)
            if (item.getCheckedAt() != null && !item.getCheckedAt().isEmpty()) {
                try {
                    SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date date = fullFormat.parse(item.getCheckedAt());

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String timeOnly = timeFormat.format(date);

                    tvCheckedAt.setVisibility(View.VISIBLE);
                    tvCheckedAt.setText("✅ " + timeOnly);
                } catch (Exception e) {
                    tvCheckedAt.setVisibility(View.VISIBLE);
                    tvCheckedAt.setText("✅ " + item.getCheckedAt());
                }
            } else {
                tvCheckedAt.setVisibility(View.GONE);
            }

            // ==========================================
            // КНОПКИ
            // ==========================================

            btnMove.setOnClickListener(null);
            btnMove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMoveClick(item);
                }
            });

            if (item.getMotorDisabled() == 1) {
                btnMotor.setColorFilter(0xFFFF0000);
                btnMotor.setContentDescription(context.getString(R.string.content_description_motor_off));
            } else {
                btnMotor.setColorFilter(0xFF888888);
                btnMotor.setContentDescription(context.getString(R.string.content_description_motor_on));
            }
            btnMotor.setOnClickListener(null);
            btnMotor.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMotorClick(item);
                }
            });

            if (item.getBoxRemoved() == 1) {
                btnBox.setColorFilter(0xFFFF8800);
                btnBox.setContentDescription(context.getString(R.string.content_description_box_off));
            } else {
                btnBox.setColorFilter(0xFF888888);
                btnBox.setContentDescription(context.getString(R.string.content_description_box_on));
            }
            btnBox.setOnClickListener(null);
            btnBox.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBoxClick(item);
                }
            });

            // ==========================================
            // ✅ CHECKBOX
            // ==========================================
            cbChecked.setOnCheckedChangeListener(null);
            cbChecked.setChecked(item.getIsChecked() == 1);
            Log.d("LIST_DEBUG", "CheckBox setChecked = " + (item.getIsChecked() == 1));

            cbChecked.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Log.d("LIST_DEBUG", "=== CheckBox clicked ===");
                Log.d("LIST_DEBUG", "position=" + position + ", gateValveId=" + item.getGateValveId());
                Log.d("LIST_DEBUG", "isChecked=" + isChecked);

                if (listener != null) {
                    listener.onCheckedClick(item, isChecked);
                }
            });

            // ==========================================
            // ✅ КОРОТКИЙ ТАП ПО КАРТОЧКЕ (НОВОЕ!)
            // ==========================================
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });

            // ==========================================
            // ДЛИННЫЙ ТАП
            // ==========================================
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onItemLongClick(item);
                }
                return true;
            });

            Log.d("LIST_DEBUG", "=== bind END ===");
        }
    }
}