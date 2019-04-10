package project.tronku.udhaari.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import project.tronku.udhaari.Models.PaymentModel;
import project.tronku.udhaari.R;

public class PendingAdapter extends RecyclerView.Adapter<PendingAdapter.ViewHolder> {

    private Context context;
    private ArrayList<PaymentModel> paymentModels;

    public PendingAdapter(Context context, ArrayList<PaymentModel> list) {
        this.context = context;
        paymentModels = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pending_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(paymentModels.get(position).getName());
        holder.phone.setText(paymentModels.get(position).getPhone());
        holder.amount.setText(String.valueOf(paymentModels.get(position).getAmount()));
    }

    @Override
    public int getItemCount() {
        return paymentModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.pending_name)
        TextView name;
        @BindView(R.id.pending_amount)
        TextView amount;
        @BindView(R.id.pending_phone)
        TextView phone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void updateList(ArrayList<PaymentModel> list) {
        paymentModels = list;
        notifyDataSetChanged();
    }
}
