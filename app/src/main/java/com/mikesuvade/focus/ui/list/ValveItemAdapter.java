package com.mikesuvade.focus.ui.list;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.ValveItem;
import com.mikesuvade.focus.domain.repository.IRepository;

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
        void onItemClick(ValveItem item);
    }

    public ValveItemAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<ValveItem> items) {
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
                .inflate(R.layout.item_valve_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ValveItem item = items.get(position);
        holder.bind(item, listener, context, position);
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

        void bind(ValveItem item, OnItemClickListener listener, Context context, int position) {

            // ==========================================
            // 🔥 ISY и NAME - СНАЧАЛА ИЗ ValveItem
            // ==========================================
            String isy = item.getGateValveIsy();
            String name = item.getGateValveName();

            if ((isy == null || isy.isEmpty()) || (name == null || name.isEmpty())) {
                GateValve valve = getGateValveMerged(item.getGateValveId(), context);
                if (valve != null) {
                    if (isy == null || isy.isEmpty()) isy = valve.getIsy();
                    if (name == null || name.isEmpty()) name = valve.getName();
                }
            }

            if (isy != null && !isy.isEmpty()) {
                tvIsy.setVisibility(View.VISIBLE);
                tvIsy.setText(isy);
            } else {
                tvIsy.setVisibility(View.GONE);
            }

            if (name == null || name.isEmpty()) {
                name = context.getString(R.string.no_name);
            }
            tvName.setText(name);

            // ==========================================
            // СТАТУСЫ
            // ==========================================
            tvAssembleStatus.setVisibility(View.VISIBLE);
            tvCheckedAt.setVisibility(View.GONE);

            if (item.getIsChecked() == 0) {
                if (item.getIsAssembled() == 1) {
                    tvAssembleStatus.setText(context.getString(R.string.status_assemble));
                    tvAssembleStatus.setTextColor(ContextCompat.getColor(context, R.color.status_success));
                } else {
                    tvAssembleStatus.setText(context.getString(R.string.status_disassemble));
                    tvAssembleStatus.setTextColor(ContextCompat.getColor(context, R.color.status_error));
                }
                tvCheckedAt.setVisibility(View.GONE);

                if (item.getMotorDisabled() == 1) {
                    tvMotorStatus.setVisibility(View.VISIBLE);
                    if (item.getIsAssembled() == 1) {
                        tvMotorStatus.setText(context.getString(R.string.motor_connect));
                        tvMotorStatus.setTextColor(ContextCompat.getColor(context, R.color.status_error));
                    } else {
                        tvMotorStatus.setText(context.getString(R.string.motor_disconnect));
                        tvMotorStatus.setTextColor(ContextCompat.getColor(context, R.color.status_error));
                    }
                } else {
                    tvMotorStatus.setVisibility(View.GONE);
                }

                if (item.getBoxRemoved() == 1) {
                    tvBoxStatus.setVisibility(View.VISIBLE);
                    if (item.getIsAssembled() == 1) {
                        tvBoxStatus.setText(context.getString(R.string.box_install));
                        tvBoxStatus.setTextColor(ContextCompat.getColor(context, R.color.status_warning));
                    } else {
                        tvBoxStatus.setText(context.getString(R.string.box_remove));
                        tvBoxStatus.setTextColor(ContextCompat.getColor(context, R.color.status_warning));
                    }
                } else {
                    tvBoxStatus.setVisibility(View.GONE);
                }

            } else {
                if (item.getIsAssembled() == 1) {
                    tvAssembleStatus.setText(context.getString(R.string.status_assembled));
                    tvAssembleStatus.setTextColor(ContextCompat.getColor(context, R.color.status_success));
                } else {
                    tvAssembleStatus.setText(context.getString(R.string.status_disassembled));
                    tvAssembleStatus.setTextColor(ContextCompat.getColor(context, R.color.status_error));
                }

                if (!TextUtils.isEmpty(item.getCheckedAt())) {
                    try {
                        SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        Date date = fullFormat.parse(item.getCheckedAt());
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        String timeOnly = timeFormat.format(date);

                        tvCheckedAt.setVisibility(View.VISIBLE);
                        tvCheckedAt.setText(timeOnly);
                        tvCheckedAt.setTextColor(item.getIsAssembled() == 1 ?
                                ContextCompat.getColor(context, R.color.status_success) :
                                ContextCompat.getColor(context, R.color.status_error));
                    } catch (Exception e) {
                        tvCheckedAt.setVisibility(View.VISIBLE);
                        tvCheckedAt.setText(item.getCheckedAt());
                    }
                }

                if (item.getMotorDisabled() == 1) {
                    tvMotorStatus.setVisibility(View.VISIBLE);
                    if (item.getIsAssembled() == 1) {
                        tvMotorStatus.setText(context.getString(R.string.status_motor_connected_full));
                        tvMotorStatus.setTextColor(ContextCompat.getColor(context, R.color.status_success));
                    } else {
                        tvMotorStatus.setText(context.getString(R.string.status_motor_disconnected_full));
                        tvMotorStatus.setTextColor(ContextCompat.getColor(context, R.color.status_error));
                    }
                } else {
                    tvMotorStatus.setVisibility(View.GONE);
                }

                if (item.getBoxRemoved() == 1) {
                    tvBoxStatus.setVisibility(View.VISIBLE);
                    if (item.getIsAssembled() == 1) {
                        tvBoxStatus.setText(context.getString(R.string.status_box_installed_full));
                        tvBoxStatus.setTextColor(ContextCompat.getColor(context, R.color.status_success));
                    } else {
                        tvBoxStatus.setText(context.getString(R.string.status_box_removed_full));
                        tvBoxStatus.setTextColor(ContextCompat.getColor(context, R.color.status_warning));
                    }
                } else {
                    tvBoxStatus.setVisibility(View.GONE);
                }
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
                tvCheckedAt.setPaintFlags(tvCheckedAt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvCheckedAt.setAlpha(0.5f);
                tvMotorStatus.setPaintFlags(tvMotorStatus.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvMotorStatus.setAlpha(0.5f);
                tvBoxStatus.setPaintFlags(tvBoxStatus.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvBoxStatus.setAlpha(0.5f);
            } else {
                tvIsy.setPaintFlags(tvIsy.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvIsy.setAlpha(1.0f);
                tvName.setPaintFlags(tvName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvName.setAlpha(1.0f);
                tvAssembleStatus.setPaintFlags(tvAssembleStatus.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvAssembleStatus.setAlpha(1.0f);
                tvCheckedAt.setPaintFlags(tvCheckedAt.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvCheckedAt.setAlpha(1.0f);
                tvMotorStatus.setPaintFlags(tvMotorStatus.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvMotorStatus.setAlpha(1.0f);
                tvBoxStatus.setPaintFlags(tvBoxStatus.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvBoxStatus.setAlpha(1.0f);
            }

            // ==========================================
            // 🔥 КНОПКИ С ИКОНКАМИ
            // ==========================================

            LinearLayout buttonContainer = (LinearLayout) btnMotor.getParent();

            // 🔥 Квадратные кнопки фиксированного размера
            int size = (int) (48 * context.getResources().getDisplayMetrics().density);

            buttonContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    size
            ));

            // Сохраняем item в тег
            btnMove.setTag(item);
            btnMotor.setTag(item);
            btnBox.setTag(item);

            // 🔥 ДВИГАТЕЛЬ
            btnMotor.setColorFilter(null);

            if (item.getIsChecked() == 1) {
                btnMotor.setColorFilter(ContextCompat.getColor(context, R.color.btn_default));
                btnMotor.setContentDescription(context.getString(R.string.content_description_motor_off));
            } else {
                if (item.getMotorDisabled() == 1) {
                    btnMotor.setColorFilter(ContextCompat.getColor(context, R.color.btn_motor_off));
                    btnMotor.setContentDescription(context.getString(R.string.content_description_motor_on));
                } else {
                    btnMotor.setColorFilter(ContextCompat.getColor(context, R.color.btn_default));
                    btnMotor.setContentDescription(context.getString(R.string.content_description_motor_off));
                }
            }
            btnMotor.setOnClickListener(v -> {
                if (listener != null) {
                    ValveItem clickedItem = (ValveItem) v.getTag();
                    listener.onMotorClick(clickedItem);
                }
            });

            // 🔥 ККВ
            btnBox.setColorFilter(null);

            if (item.getIsChecked() == 1) {
                btnBox.setColorFilter(ContextCompat.getColor(context, R.color.btn_default));
                btnBox.setContentDescription(context.getString(R.string.content_description_box_off));
            } else {
                if (item.getBoxRemoved() == 1) {
                    btnBox.setColorFilter(ContextCompat.getColor(context, R.color.btn_box_removed));
                    btnBox.setContentDescription(context.getString(R.string.content_description_box_on));
                } else {
                    btnBox.setColorFilter(ContextCompat.getColor(context, R.color.btn_default));
                    btnBox.setContentDescription(context.getString(R.string.content_description_box_off));
                }
            }
            btnBox.setOnClickListener(v -> {
                if (listener != null) {
                    ValveItem clickedItem = (ValveItem) v.getTag();
                    listener.onBoxClick(clickedItem);
                }
            });

            // 🔥 СТРЕЛКА - ВСЕГДА СЕРАЯ
            // 🔥 СТРЕЛКА - ВСЕГДА СИНЯЯ
            btnMove.setColorFilter(ContextCompat.getColor(context, R.color.btn_move_blue));

            if (item.getIsAssembled() == 1) {
                btnMove.setImageResource(R.drawable.ic_move_right);
                btnMove.setContentDescription("Переместить на разбор");
                btnMove.setOnClickListener(v -> {
                    if (listener != null) {
                        ValveItem clickedItem = (ValveItem) v.getTag();
                        listener.onMoveClick(clickedItem);
                    }
                });

                buttonContainer.removeAllViews();
                buttonContainer.addView(btnMotor);
                buttonContainer.addView(btnBox);
                buttonContainer.addView(btnMove);

            } else {
                btnMove.setImageResource(R.drawable.ic_move_left);
                btnMove.setContentDescription("Переместить на сборку");
                btnMove.setOnClickListener(v -> {
                    if (listener != null) {
                        ValveItem clickedItem = (ValveItem) v.getTag();
                        listener.onMoveClick(clickedItem);
                    }
                });

                buttonContainer.removeAllViews();
                buttonContainer.addView(btnMove);
                buttonContainer.addView(btnMotor);
                buttonContainer.addView(btnBox);
            }

            // 🔥 Устанавливаем квадратные размеры ПОСЛЕ добавления во ViewGroup
            for (int i = 0; i < buttonContainer.getChildCount(); i++) {
                View child = buttonContainer.getChildAt(i);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                if (i < buttonContainer.getChildCount() - 1) {
                    params.setMarginEnd((int) (8 * context.getResources().getDisplayMetrics().density));
                }
                child.setLayoutParams(params);
            }

            // ==========================================
            // CHECKBOX
            // ==========================================
            cbChecked.setOnCheckedChangeListener(null);
            cbChecked.setChecked(item.getIsChecked() == 1);
            cbChecked.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onCheckedClick(item, isChecked);
                }
            });

            // Короткий тап
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });

            // Длинный тап
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onItemLongClick(item);
                }
                return true;
            });

            // ==========================================
            // 🔥 ЦВЕТ КАРТОЧКИ
            // ==========================================
            if (item.getIsChecked() == 1) {
                int color = ContextCompat.getColor(context, R.color.card_checked_background);
                itemView.setBackgroundColor(color);
            } else {
                itemView.setBackgroundColor(0x00000000);
            }
        }

        private GateValve getGateValveMerged(int gateValveId, Context context) {
            IRepository repository = ((MyApp) context.getApplicationContext()).getRepository();

            GateValve userValve = repository.getUserGateValveById(gateValveId);
            if (userValve != null && userValve.getIsDeleted() != 1) {
                return userValve;
            }

            List<GateValve> allUserValves = repository.getAllUserGateValves();
            for (GateValve uv : allUserValves) {
                if (uv.getOriginalId() == gateValveId && uv.getIsDeleted() != 1) {
                    return uv;
                }
            }

            return repository.getGateValveById(gateValveId);
        }
    }
}