package project.tronku.udhaari.Adapters;

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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
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
    private String userName, userPhone;
    private FirebaseFirestore firestore;



    public VendorListAdapter(Context context, ArrayList<PaymentModel> paymentModels) {
        this.context = context;
        this.paymentModels = paymentModels;
        paymentModelsFilteredList = paymentModels;
        dialog = new Dialog(context);
        userName = UdhaariApp.getInstance().getDataFromPref("name");
        userPhone = UdhaariApp.getInstance().getDataFromPref("phone");
        firestore = FirebaseFirestore.getInstance();
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
        holder.shopItem.setOnClickListener(v -> checkFriendship(name, phone));
    }

    private void checkFriendship(String name, String phone) {
        firestore.collection("Customers").document(userPhone).collection("Friends")
                .whereEqualTo("phone", phone)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        if (snapshots == null || snapshots.isEmpty()) {
                            sendRequest(name, phone);
                        }
                        else {
                            String status = snapshots.getDocuments().get(0).get("status").toString();
                            Timber.e("STATUS: %s", status);
                            if (status.equals("accepted"))
                                showDialog(name, phone);
                            else
                                Toast.makeText(context, "Request is pending!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendRequest(String name, String phone) {
        Dialog requestDialog = new Dialog(context);
        requestDialog.setCancelable(true);
        requestDialog.setContentView(R.layout.friend_request_dialog);
        requestDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView message = requestDialog.findViewById(R.id.request_message);
        TextView shopName = requestDialog.findViewById(R.id.request_name);
        Button requestButton = requestDialog.findViewById(R.id.request_button);
        View layer = requestDialog.findViewById(R.id.layer);
        ProgressBar loader = requestDialog.findViewById(R.id.loader);

        shopName.setText(name);
        message.setText("You aren't a friend of:");
        requestButton.setText("SEND REQUEST");

        requestButton.setOnClickListener(v -> {
            layer.setVisibility(View.VISIBLE);
            loader.setVisibility(View.VISIBLE);

            firestore.runTransaction(transaction -> {

                //adding request to Customers -> Friends
                Map<String, Object> customerFriendMap = new HashMap<>();
                customerFriendMap.put("serviceName", name);
                customerFriendMap.put("phone", phone);
                customerFriendMap.put("status", "pending");
                transaction.set(firestore.collection("Customers")
                        .document(userPhone).collection("Friends").document(phone), customerFriendMap);

                //adding request to Vendors -> Friends
                Map<String, Object> vendorFriendMap = new HashMap<>();
                vendorFriendMap.put("name", userName);
                vendorFriendMap.put("phone", userPhone);
                vendorFriendMap.put("status", "pending");
                transaction.set(firestore.collection("Vendors")
                        .document(phone).collection("Friends").document(userPhone), vendorFriendMap);

                //adding notif to Vendors -> Notifs
                long timeStamp = System.currentTimeMillis();
                Map<String, Object> vendorNotifMap = new HashMap<>();
                vendorNotifMap.put("name", userName);
                vendorNotifMap.put("phone", userPhone);
                vendorNotifMap.put("type", "friend_request");
                vendorNotifMap.put("timeStamp", timeStamp);
                vendorNotifMap.put("read", false);
                transaction.set(firestore.collection("Vendors")
                        .document(phone).collection("Notifs").document(String.valueOf(timeStamp)), vendorNotifMap);

                return null;
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Timber.e("Request sent successfully");
                    Toast.makeText(context, "Request sent successfully!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Timber.e("Request failed: %s", task.getException().toString());
                    Toast.makeText(context, "Something went wrong! Try again.", Toast.LENGTH_LONG).show();
                }

                requestDialog.cancel();
            });
        });

        requestDialog.show();
    }

    private void showDialog(String name, String phone) {
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.amount_dialog_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView message = dialog.findViewById(R.id.amount_title);
        TextView shopName = dialog.findViewById(R.id.name_text_view);
        EditText amount = dialog.findViewById(R.id.amount_edit_text);
        EditText description = dialog.findViewById(R.id.description_edit_text);
        Button borrowButton = dialog.findViewById(R.id.payment_button);
        View layer = dialog.findViewById(R.id.layer);
        ProgressBar loader = dialog.findViewById(R.id.loader);

        shopName.setText(name);
        message.setText("Borrowing from:");
        borrowButton.setText("BORROW");

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
                updateBorrowData(name, phone, Integer.parseInt(amountStr), descriptionStr);
            }
        });

        dialog.show();
    }

    private void updateBorrowData(String name, String phone, int amount, String description) {
        final int[] totalPending = {0};

        firestore.collection("Customers").document(userPhone).collection("Pending").whereEqualTo("phone", phone)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot == null || snapshot.isEmpty())
                            Timber.e("New borrowing to the vendor");
                        else
                            totalPending[0] = Integer.parseInt(snapshot.getDocuments().get(0).get("amount").toString());

                        //adding data
                        WriteBatch batch = firestore.batch();

                        Date date = Calendar.getInstance().getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
                        String dateStr = sdf.format(date);
                        String timeStr = sdf2.format(date);
                        long timeStamp = System.currentTimeMillis();
                        totalPending[0] += amount;

                        Map<String, Object> pendingVendorMap = new HashMap<>();
                        pendingVendorMap.put("serviceName", name);
                        pendingVendorMap.put("phone", phone);
                        pendingVendorMap.put("amount", totalPending[0]);
                        batch.set(firestore.collection("Customers").document(userPhone).collection("Pending").document(phone), pendingVendorMap);

                        //adding data to Customers -> History
                        VendorModel historyVendorModel = new VendorModel(name, phone, amount, dateStr, timeStr, description, timeStamp, "borrowed");
                        batch.set(firestore.collection("Customers").document(userPhone).collection("History").document(), historyVendorModel);

                        //adding data to Vendors -> Pending
                        Map<String, Object> pendingCustomerMap = new HashMap<>();
                        pendingCustomerMap.put("name", userName);
                        pendingCustomerMap.put("phone", userPhone);
                        pendingCustomerMap.put("amount", totalPending[0]);
                        batch.set(firestore.collection("Vendors").document(phone).collection("Pending").document(userPhone), pendingCustomerMap);

                        //adding data to Vendors -> History
                        CustomerModel historyCustomerModel = new CustomerModel(userName, userPhone, amount, dateStr, timeStr, description, timeStamp, "borrowed");
                        batch.set(firestore.collection("Vendors").document(phone).collection("History").document(), historyCustomerModel);

                        batch.commit().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Timber.e("Added new payment successfully!");
                                Toast.makeText(context, "Transaction added!", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Timber.e("Update failed: %s", task1.getException().toString());
                                Toast.makeText(context, "Transaction failed! Try again.", Toast.LENGTH_SHORT).show();
                            }
                            dialog.cancel();
                        });

                    }
                    else {
                        Timber.e("Error while querying: %s", task.getException().toString());
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
