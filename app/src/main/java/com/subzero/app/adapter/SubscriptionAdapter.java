package com.subzero.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.subzero.app.R;
import com.subzero.app.db.StorageManager;
import com.subzero.app.model.Subscription;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.VH> {

    private Context context;
    private List<Subscription> subs;
    private OnSubClickListener listener;
    private StorageManager store;

    public interface OnSubClickListener {
        void onClick(Subscription sub);
        void onLongClick(Subscription sub);
    }

    public SubscriptionAdapter(Context context, List<Subscription> subs, OnSubClickListener listener) {
        this.context = context; this.subs = subs; this.listener = listener;
        this.store = StorageManager.getInstance(context);
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(context).inflate(R.layout.item_subscription, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH holder, int position) {
        Subscription s = subs.get(position);
        holder.tvName.setText(s.getName());
        holder.tvIcon.setText(getCategoryIcon(s.getCategory()));

        String symbol = getSymbol();
        holder.tvAmount.setText(symbol + String.format(Locale.getDefault(), "%.2f", s.getAmount()));
        holder.tvCycle.setText("/" + getCycleLabel(s.getCycle()));

        String nextDate = s.getNextPaymentDate();
        if (nextDate != null && !nextDate.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdf.parse(nextDate);
                String display = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE).format(date);
                holder.tvNextPayment.setText("下次付款：" + display);

                // Color dot based on urgency
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                Calendar today = Calendar.getInstance();
                long diff = (cal.getTimeInMillis() - today.getTimeInMillis()) / (1000 * 60 * 60 * 24);
                if (diff < 0) holder.viewDot.setBackgroundColor(Color.parseColor("#F44336"));
                else if (diff <= 3) holder.viewDot.setBackgroundColor(Color.parseColor("#FF9800"));
                else holder.viewDot.setBackgroundColor(Color.parseColor("#4CAF50"));
            } catch (ParseException e) {
                holder.tvNextPayment.setText("下次付款：" + nextDate);
                holder.viewDot.setBackgroundColor(Color.parseColor("#9E9E9E"));
            }
        }

        if (!s.isActive()) {
            holder.cardView.setAlpha(0.5f);
        } else {
            holder.cardView.setAlpha(1.0f);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(s));
        holder.itemView.setOnLongClickListener(v -> { listener.onLongClick(s); return true; });
    }

    @Override public int getItemCount() { return subs.size(); }

    private String getSymbol() {
        String c = store.getSetting("currency");
        return c.equals("USD") ? "$" : c.equals("EUR") ? "€" : c.equals("JPY") ? "¥" : "¥";
    }

    private String getCycleLabel(String cycle) {
        switch (cycle) {
            case "weekly": return "周";
            case "quarterly": return "季";
            case "yearly": return "年";
            default: return "月";
        }
    }

    private String getCategoryIcon(String cat) {
        switch (cat != null ? cat : "") {
            case "entertainment": return "🎬";
            case "productivity": return "💼";
            case "health": return "💪";
            case "shopping": return "🛒";
            case "food": return "🍔";
            case "education": return "📚";
            default: return "📦";
        }
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvIcon, tvName, tvAmount, tvCycle, tvNextPayment;
        MaterialCardView cardView;
        View viewDot;
        public VH(@NonNull View v) {
            super(v);
            tvIcon = v.findViewById(R.id.tv_icon);
            tvName = v.findViewById(R.id.tv_name);
            tvAmount = v.findViewById(R.id.tv_amount);
            tvCycle = v.findViewById(R.id.tv_cycle);
            tvNextPayment = v.findViewById(R.id.tv_next_payment);
            cardView = v.findViewById(R.id.card_view);
            viewDot = v.findViewById(R.id.view_status_dot);
        }
    }
}
