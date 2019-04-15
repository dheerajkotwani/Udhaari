package project.tronku.udhaari.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import project.tronku.udhaari.Models.CustomerModel;
import project.tronku.udhaari.Models.PaymentModel;
import project.tronku.udhaari.Models.VendorModel;
import project.tronku.udhaari.R;
import project.tronku.udhaari.UdhaariApp;
import timber.log.Timber;

public class PendingAdapter extends RecyclerView.Adapter<PendingAdapter.ViewHolder> {

    private Context context;
    private ArrayList<PaymentModel> paymentModels;
    private Dialog dialog;
    private String userName, userPhone;

    public PendingAdapter(Context context, ArrayList<PaymentModel> list) {
        this.context = context;
        paymentModels = list;
        dialog = new Dialog(context);
        userName = UdhaariApp.getInstance().getDataFromPref("name");
        userPhone = UdhaariApp.getInstance().getDataFromPref("phone");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pending_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = paymentModels.get(position).getName();
        String phone = paymentModels.get(position).getPhone();
        int amount = paymentModels.get(position).getAmount();
        String type = UdhaariApp.getInstance().getDataFromPref("type");

        holder.name.setText(name);
        holder.phone.setText(phone);
        holder.amount.setText(String.valueOf(amount));

        if (type.equals("vendor")) {
            holder.symbol.setTextColor(context.getResources().getColor(R.color.green));
            holder.amount.setTextColor(context.getResources().getColor(R.color.green));
        }

        holder.pendingItem.setOnClickListener(v -> {
            if (type.equals("customer"))
                showDialog(name, phone, amount);
        });
    }

    private void showDialog(String name, String phone, int amount) {
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.pay_pending_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        TextView shopName = dialog.findViewById(R.id.settle_shop_name);
        EditText amountEditText = dialog.findViewById(R.id.settle_amount);
        Button settleButton = dialog.findViewById(R.id.settle_button);
        View layer = dialog.findViewById(R.id.layer);
        ProgressBar loader = dialog.findViewById(R.id.loader);

        shopName.setText(name);
        amountEditText.setText(String.valueOf(amount));
        amountEditText.requestFocus();

        settleButton.setOnClickListener(v -> {
            String amountEntered = amountEditText.getText().toString();
            int amountPaying = Integer.parseInt(amountEntered);
            if (amountEntered.isEmpty() || amountPaying > amount) {
                amountEditText.setError("Enter valid amount.");
                amountEditText.requestFocus();
            }
            else {
                layer.setVisibility(View.VISIBLE);
                loader.setVisibility(View.VISIBLE);
                sendForVerification(phone, amount, amountPaying);
            }
        });
    }

    private void sendForVerification(String phone, int amount, int amountPaying) {
        long timeStamp = System.currentTimeMillis();
        Map<String, Object> notifMap = new HashMap<>();
        notifMap.put("name", userName);
        notifMap.put("phone", userPhone);
        notifMap.put("type", "payment_request");
        notifMap.put("totalAmount", amount);
        notifMap.put("amountPaying", amountPaying);
        notifMap.put("timeStamp", timeStamp);
        notifMap.put("read", false);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("Vendors").document(phone).collection("Notifs").document(String.valueOf(timeStamp)).set(notifMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Timber.e("Verification notif send");
                        Toast.makeText(context, "Vendor's verification pending", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Timber.e("Verification send failed: %s", task.getException().toString());
                        Toast.makeText(context, "Something went wrong! Try again!", Toast.LENGTH_LONG).show();
                    }
                    dialog.cancel();
                });
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
        @BindView(R.id.symbol)
        TextView symbol;
        @BindView(R.id.pending_phone)
        TextView phone;
        @BindView(R.id.pending_item)
        LinearLayout pendingItem;

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
