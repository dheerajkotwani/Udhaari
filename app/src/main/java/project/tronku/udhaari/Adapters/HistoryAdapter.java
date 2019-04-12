package project.tronku.udhaari.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import project.tronku.udhaari.Models.PaymentModel;
import project.tronku.udhaari.R;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Viewholder> {

    private Context context;
    private ArrayList<PaymentModel> paymentModels;

    public HistoryAdapter(Context context, ArrayList<PaymentModel> list) {
        this.context = context;
        paymentModels = list;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.history_item_layout, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        String name = paymentModels.get(position).getName();
        String phone = paymentModels.get(position).getPhone();
        String amount = String.valueOf(paymentModels.get(position).getAmount());
        String description = paymentModels.get(position).getDescription();
        String status = paymentModels.get(position).getStatus();

        holder.name.setText(name);
        holder.phone.setText(phone);
        holder.amount.setText(amount);

        if (status.equals("borrowed")) {
            holder.amount.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            holder.symbol.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }
        else {
            holder.amount.setTextColor(context.getResources().getColor(R.color.green));
            holder.symbol.setTextColor(context.getResources().getColor(R.color.green));
        }

        //getting time and date
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        String currentDate = sdf.format(date);
        String dateStr = paymentModels.get(position).getDate();
        String time = paymentModels.get(position).getTime();
        String hrs = time.substring(0, time.indexOf(":"));
        int hr = Integer.parseInt(hrs);
        String timeStr;
        if (hr <= 12)
            timeStr = time + " AM";
        else
            timeStr = hr-12 + ":" + time.substring(time.indexOf(":") + 1) + " PM";

        if (dateStr.equals(currentDate))
            holder.dateTime.setText(timeStr);
        else
            holder.dateTime.setText(dateStr);

        holder.historyItem.setOnClickListener(v -> {
            showDialog(name, phone, amount, dateStr, timeStr, description, status);
        });

    }

    private void showDialog(String name, String phone, String amount, String dateStr, String timeStr, String description, String status) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.history_summary);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView nameTextView = dialog.findViewById(R.id.summary_name);
        TextView phoneTextView = dialog.findViewById(R.id.summary_phone);
        TextView amountTextView = dialog.findViewById(R.id.summary_amount);
        TextView symbolTextView = dialog.findViewById(R.id.rupee_symbol);
        TextView dateTextView = dialog.findViewById(R.id.summary_date);
        TextView timeTextView = dialog.findViewById(R.id.summary_time);
        TextView descriptionTextView = dialog.findViewById(R.id.summary_description);

        nameTextView.setText(name);
        phoneTextView.setText(phone);
        amountTextView.setText(amount);
        dateTextView.setText(dateStr);
        timeTextView.setText(timeStr);
        descriptionTextView.setText(description);

        if (status.equals("borrowed")) {
            amountTextView.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            symbolTextView.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }
        else {
            amountTextView.setTextColor(context.getResources().getColor(R.color.green));
            symbolTextView.setTextColor(context.getResources().getColor(R.color.green));
        }

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return paymentModels.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {

        @BindView(R.id.history_name)
        TextView name;
        @BindView(R.id.history_phone)
        TextView phone;
        @BindView(R.id.history_dateTime)
        TextView dateTime;
        @BindView(R.id.history_amount)
        TextView amount;
        @BindView(R.id.rupee_symbol)
        TextView symbol;
        @BindView(R.id.history_item)
        LinearLayout historyItem;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void updateList(ArrayList<PaymentModel> list) {
        paymentModels = list;
        notifyDataSetChanged();
    }
}
