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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import project.tronku.udhaari.Models.PaymentModel;
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
                sendForVerification(phone, amountStr, descriptionStr);
            }
        });

        dialog.show();
    }

    private void sendForVerification(String phone, String amount, String description) {
        long timeStamp = System.currentTimeMillis();
        Map<String, Object> notifMap = new HashMap<>();
        notifMap.put("name", userName);
        notifMap.put("phone", userPhone);
        notifMap.put("type", "payment_request");
        notifMap.put("amount", Integer.parseInt(amount));
        notifMap.put("description", description);
        notifMap.put("timeStamp", timeStamp);
        notifMap.put("read", false);

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
