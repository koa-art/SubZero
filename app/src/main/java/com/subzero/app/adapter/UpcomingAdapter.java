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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpcomingAdapter extends RecyclerView.Adapter<UpcomingAdapter.VH> {

    private Context context;
    private List<Subscription> subs;
    private StorageManager store;

    public UpcomingAdapter(Context context, List<Subscription> subs) {
        this.context = context; this.subs = subs;
        this.store = StorageManager.getInstance(context);
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(context).inflate(R.layout.item_upcoming, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH holder, int position) {
        Subscription s = subs.get(position);
        holder.tvName.setText(s.getName());
        String symbol = getSymbol();
        holder.tvAmount.setText(symbol + String.format(Locale.getDefault(), "%.2f/月", s.getMonthlyAmount()));

        String cat = s.getCategory() != null ? s.getCategory() : "other";
        int catColor = getCategoryColor(cat);
        holder.viewAccent.setBackgroundColor(catColor);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(s.getNextPaymentDate());
            Calendar cal = Calendar.getInstance(); cal.setTime(date);
            Calendar today = Calendar.getInstance();
            long diffDays = (cal.getTimeInMillis() - today.getTimeInMillis()) / (1000 * 60 * 60 * 24);

            if (diffDays == 0) {
                holder.tvDays.setText(context.getString(R.string.today));
            } else if (diffDays < 0) {
                holder.tvDays.setText(context.getString(R.string.overdue));
            } else {
                holder.tvDays.setText(String.format(context.getString(R.string.days_left), diffDays));
            }
        } catch (ParseException e) {
            holder.tvDays.setText("--");
        }
    }

    @Override public int getItemCount() { return subs.size(); }

    private String getSymbol() {
        String c = store.getSetting("currency");
        if (c.equals("USD")) return "$";
        if (c.equals("EUR")) return "€";
        if (c.equals("JPY")) return "¥";
        return "¥";
    }

    private int getCategoryColor(String cat) {
        switch (cat) {
            case "entertainment": return Color.parseColor("#FF5722");
            case "productivity": return Color.parseColor("#2196F3");
            case "health": return Color.parseColor("#4CAF50");
            case "shopping": return Color.parseColor("#FF9800");
            case "food": return Color.parseColor("#E91E63");
            case "education": return Color.parseColor("#9C27B0");
            default: return Color.parseColor("#607D8B");
        }
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount, tvDays;
        View viewAccent;
        public VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_upcoming_name);
            tvAmount = v.findViewById(R.id.tv_upcoming_amount);
            tvDays = v.findViewById(R.id.tv_days);
            viewAccent = v.findViewById(R.id.view_accent);
        }
    }
}
