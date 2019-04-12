package project.tronku.udhaari.Adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

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

public class VendorListAdapter extends RecyclerView.Adapter<VendorListAdapter.Viewholder> implements Filterable {

    private Context context;
    private ArrayList<PaymentModel> paymentModels;
    private ArrayList<PaymentModel> paymentModelsFilteredList;
    private Dialog dialog;


    public VendorListAdapter(Context context, ArrayList<PaymentModel> paymentModels) {
        this.context = context;
        this.paymentModels = paymentModels;
        paymentModelsFilteredList = paymentModels;
        dialog = new Dialog(context);
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.shops_item_layout, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        String name = paymentModelsFilteredList.get(position).getName();
        String phone = paymentModelsFilteredList.get(position).getPhone();

        holder.name.setText(name);
        holder.phone.setText(phone);
        holder.shopItem.setOnClickListener(v -> {
            showDialog(name, phone);
        });
    }

    private void showDialog(String name, String phone) {
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.add_amount_dialog_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView shopName = dialog.findViewById(R.id.shop_name_text_view);
        EditText amount = dialog.findViewById(R.id.amount_edit_text);
        EditText description = dialog.findViewById(R.id.description_edit_text);
        Button borrowButton = dialog.findViewById(R.id.borrow_button);
        View layer = dialog.findViewById(R.id.layer);
        ProgressBar loader = dialog.findViewById(R.id.loader);

        shopName.setText(name);
        borrowButton.setOnClickListener(v -> {
            String amountStr = amount.getText().toString();
            String descriptionStr = description.getText().toString();

            if (amountStr.isEmpty()) {
                amount.setError("Enter amount.");
                amount.requestFocus();
            }
            else if (descriptionStr.isEmpty()) {
                description.setError("Enter description.");
                description.requestFocus();
            }
            else {
                layer.setVisibility(View.VISIBLE);
                loader.setVisibility(View.VISIBLE);
                uploadData(name, phone, amountStr, descriptionStr);
            }
        });

        dialog.show();
    }

    private void uploadData(String name, String phone, String amount, String description) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        String userName = UdhaariApp.getInstance().getDataFromPref("name");
        String userPhone = UdhaariApp.getInstance().getDataFromPref("phone");
        final int[] totalPending = {0};

        firestore.collection("Customers").document(userPhone).collection("Pending").whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots == null || snapshots.isEmpty()) {
                        Timber.e("New payment to the vendor");
                    }
                    else {
                        totalPending[0] = Integer.parseInt(snapshots.getDocuments().get(0).get("amount").toString());
                    }
                })
                .addOnFailureListener(exception -> {
                    Timber.e("Error: %s", exception.getMessage());
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        //adding data
                        firestore.runTransaction((Transaction.Function<Void>) transaction -> {

                            Date date = Calendar.getInstance().getTime();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
                            String dateStr = sdf.format(date);
                            String timeStr = sdf2.format(date);
                            long timeStamp = System.currentTimeMillis();
                            int latestAmount = Integer.parseInt(amount);
                            totalPending[0] += latestAmount;

                            //adding data to Customers -> Pending
                            Map<String, Object> pendingVendorMap = new HashMap<>();
                            pendingVendorMap.put("serviceName", name);
                            pendingVendorMap.put("phone", phone);
                            pendingVendorMap.put("amount", totalPending[0]);
                            transaction.set(firestore.collection("Customers").document(userPhone).collection("Pending").document(phone), pendingVendorMap);

                            //adding data to Customers -> History
                            VendorModel historyVendorModel = new VendorModel(name, phone, latestAmount, dateStr, timeStr, description, timeStamp);
                            transaction.set(firestore.collection("Customers").document(userPhone).collection("History").document(), historyVendorModel);

                            //adding data to Vendors -> Pending
                            Map<String, Object> pendingCustomerMap = new HashMap<>();
                            pendingCustomerMap.put("name", userName);
                            pendingCustomerMap.put("phone", userPhone);
                            pendingCustomerMap.put("amount", totalPending[0]);
                            transaction.set(firestore.collection("Vendors").document(phone).collection("Pending").document(userPhone), pendingCustomerMap);

                            //adding data to Vendors -> History
                            CustomerModel historyCustomerModel = new CustomerModel(userName, userPhone, latestAmount, dateStr, timeStr, description, timeStamp);
                            transaction.set(firestore.collection("Vendors").document(phone).collection("History").document(), historyCustomerModel);

                            return null;
                        }).addOnCompleteListener(dataTask -> {
                            if (dataTask.isSuccessful()) {
                                Timber.e("Added new payment successfully!");
                                Toast.makeText(context, "Transaction added!", Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                            }
                        }).addOnFailureListener(e -> {
                            Timber.e("Update failed: %s", e.getMessage());
                            Toast.makeText(context, "Transaction failed! Try again.", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        });


                    }
                    else {
                        Timber.e("Error while querying!");
                        Toast.makeText(context, "Something went wrong! Try again.", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public int getItemCount() {
        return paymentModelsFilteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase();
                if (query.isEmpty())
                    paymentModelsFilteredList = paymentModels;
                else {
                    ArrayList<PaymentModel> list = new ArrayList<>();
                    for (PaymentModel model : paymentModels) {
                        if (model.getName().toLowerCase().contains(query) || model.getPhone().toLowerCase().contains(query))
                            list.add(model);
                    }
                    paymentModelsFilteredList = list;
                }
                FilterResults results = new FilterResults();
                results.values = paymentModelsFilteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                paymentModelsFilteredList = (ArrayList<PaymentModel>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class Viewholder extends RecyclerView.ViewHolder {

        @BindView(R.id.vendor_name)
        TextView name;
        @BindView(R.id.vendor_phone)
        TextView phone;
        @BindView(R.id.shop_item)
        LinearLayout shopItem;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void updateList(ArrayList<PaymentModel> list) {
        paymentModels = list;
        paymentModelsFilteredList = list;
        notifyDataSetChanged();
    }

}
