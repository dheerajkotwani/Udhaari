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

    public PendingAdapter(Context context, ArrayList<PaymentModel> list) {
        this.context = context;
        paymentModels = list;
        dialog = new Dialog(context);
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

        holder.name.setText(name);
        holder.phone.setText(phone);
        holder.amount.setText(String.valueOf(amount));

        holder.pendingItem.setOnClickListener(v -> showDialog(name, phone, amount));
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
                updateDatabase(name, phone, amount, amountPaying);
            }
        });
    }

    private void updateDatabase(String name, String phone, int amount, int amountPaying) {
        String userName = UdhaariApp.getInstance().getDataFromPref("name");
        String userPhone = UdhaariApp.getInstance().getDataFromPref("phone");

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.runTransaction(transaction -> {

            Date date = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
            String dateStr = sdf.format(date);
            String timeStr = sdf2.format(date);
            long timeStamp = System.currentTimeMillis();

            //updating Customer -> Pending amount
            transaction.update(firestore.collection("Customers")
                    .document(userPhone).collection("Pending").document(phone), "amount", amount-amountPaying);

            //adding Customer -> History
            VendorModel historyVendorModel = new VendorModel(name, phone, amountPaying, dateStr, timeStr, "", timeStamp, "paid");
            transaction.set(firestore.collection("Customers").document(userPhone).collection("History").document(), historyVendorModel);

            //updating Customer -> Pending amount
            transaction.update(firestore.collection("Vendors")
                    .document(phone).collection("Pending").document(userPhone), "amount", amount-amountPaying);

            //adding Vendor -> History
            CustomerModel historyCustomerModel = new CustomerModel(userName, userPhone, amountPaying, dateStr, timeStr, "", timeStamp, "paid");
            transaction.set(firestore.collection("Vendors").document(phone).collection("History").document(), historyCustomerModel);

            return null;
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Timber.e("Added new payment successfully!");
                Toast.makeText(context, "Transaction added! Swipe to refresh.", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        }).addOnFailureListener(e -> {
            Timber.e("Update failed: %s", e.getMessage());
            Toast.makeText(context, "Transaction failed! Try again.", Toast.LENGTH_SHORT).show();
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
