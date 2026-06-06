package com.example.savesomebills;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActionTipsPagerAdapter extends RecyclerView.Adapter<ActionTipsPagerAdapter.ViewHolder> {

    public static class ActionTip {
        public final String id, emoji, title, description, detailedInfo, infoUrl;
        public final double savingKwhPerMonth;

        public ActionTip(String id, String emoji, String title, String description,
                         double savingKwhPerMonth, String detailedInfo, String infoUrl) {
            this.id = id;
            this.emoji = emoji;
            this.title = title;
            this.description = description;
            this.savingKwhPerMonth = savingKwhPerMonth;
            this.detailedInfo = detailedInfo;
            this.infoUrl = infoUrl;
        }

        // Backward-compatible constructor
        public ActionTip(String id, String emoji, String title, String description,
                         double savingKwhPerMonth) {
            this(id, emoji, title, description, savingKwhPerMonth, null, null);
        }
    }

    public interface OnConfirmListener {
        void onConfirm(ActionTip tip, boolean isConfirmed);
    }

    public interface OnDismissListener {
        void onDismiss(int position);
    }

    private final List<ActionTip> tips;
    private final Set<String> confirmed;
    private OnConfirmListener confirmListener;
    private OnDismissListener dismissListener;

    public ActionTipsPagerAdapter(List<ActionTip> tips, Set<String> alreadyConfirmed) {
        this.tips = tips;
        this.confirmed = new HashSet<>(alreadyConfirmed);
    }

    public void setOnConfirmListener(OnConfirmListener l) { this.confirmListener = l; }
    public void setOnDismissListener(OnDismissListener l) { this.dismissListener = l; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_action_tip_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActionTip tip = tips.get(position);

        holder.emoji.setText(tip.emoji);
        holder.title.setText(tip.title);
        if (holder.description != null) holder.description.setText(tip.description);

        String savingStr = AppSettings.formatEnergy(holder.itemView.getContext(), tip.savingKwhPerMonth);
        holder.confirmSavingStr = savingStr + " sparen";

        boolean isConfirmed = confirmed.contains(tip.id);
        applyConfirmedState(holder, isConfirmed);

        holder.btnInfo.setOnClickListener(v -> showInfoDialog(v.getContext(), tip));

        holder.btnConfirm.setOnClickListener(v -> {
            if (confirmed.contains(tip.id)) {
                confirmed.remove(tip.id);
                AppSettings.unconfirmTip(v.getContext(), tip.id);
                AppSettings.addSavedKwh(v.getContext(), -tip.savingKwhPerMonth);
                applyConfirmedState(holder, false);
                if (confirmListener != null) confirmListener.onConfirm(tip, false);
            } else {
                confirmed.add(tip.id);
                AppSettings.confirmTip(v.getContext(), tip.id);
                AppSettings.addSavedKwh(v.getContext(), tip.savingKwhPerMonth);
                applyConfirmedState(holder, true);
                if (confirmListener != null) confirmListener.onConfirm(tip, true);
            }
        });

        holder.btnDismiss.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && dismissListener != null) {
                dismissListener.onDismiss(pos);
            }
        });
    }

    private void showInfoDialog(Context context, ActionTip tip) {
        String details = (tip.detailedInfo != null && !tip.detailedInfo.isEmpty())
                ? tip.detailedInfo : tip.description;

        // Build message with saving info on top
        String savingStr = AppSettings.formatEnergy(context, tip.savingKwhPerMonth);
        String fullMessage = "💰 Ersparnis: " + savingStr + " pro Monat\n\n" + details;

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(tip.emoji + "  " + tip.title)
                .setMessage(fullMessage)
                .setPositiveButton("Schließen", null);

        if (tip.infoUrl != null && !tip.infoUrl.isEmpty()) {
            builder.setNeutralButton("🔗 Mehr erfahren", (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tip.infoUrl));
                context.startActivity(intent);
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        // Style the message text for better readability
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setLineSpacing(0, 1.4f);
            messageView.setTextSize(14f);
        }
    }

    private void applyConfirmedState(@NonNull ViewHolder holder, boolean confirmedState) {
        holder.btnConfirm.setText(holder.confirmSavingStr);
        if (confirmedState) {
            int green = holder.itemView.getContext().getColor(R.color.green_40);
            holder.btnConfirm.setEnabled(true);
            holder.btnConfirm.setBackgroundTintList(ColorStateList.valueOf(green));
            holder.card.setStrokeColor(green);
            holder.card.setStrokeWidth(dp(holder.itemView.getContext(), 4));
        } else {
            int teal = holder.itemView.getContext().getColor(R.color.teal_40);
            holder.btnConfirm.setEnabled(true);
            holder.btnConfirm.setBackgroundTintList(ColorStateList.valueOf(teal));
            holder.card.setStrokeColor(teal);
            holder.card.setStrokeWidth(dp(holder.itemView.getContext(), 1));
        }
    }

    private int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() { return tips.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView emoji, title, description, saving;
        MaterialButton btnConfirm, btnDismiss;
        android.widget.ImageButton btnInfo;
        String confirmSavingStr;

        ViewHolder(@NonNull View v) {
            super(v);
            card        = (MaterialCardView) v;
            emoji       = v.findViewById(R.id.tv_action_emoji);
            title       = v.findViewById(R.id.tv_action_title);
            description = v.findViewById(R.id.tv_action_desc);
            saving      = v.findViewById(R.id.tv_action_saving);
            btnConfirm  = v.findViewById(R.id.btn_action_confirm);
            btnDismiss  = v.findViewById(R.id.btn_action_dismiss);
            btnInfo     = v.findViewById(R.id.btn_action_info);
        }
    }
}