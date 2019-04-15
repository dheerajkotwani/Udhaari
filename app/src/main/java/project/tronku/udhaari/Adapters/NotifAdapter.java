package project.tronku.udhaari.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import project.tronku.udhaari.Models.NotifModel;
import project.tronku.udhaari.Models.VendorModel;
import project.tronku.udhaari.R;
import project.tronku.udhaari.UdhaariApp;
import timber.log.Timber;

public class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.Viewholder> {

    private Context context;
    private ArrayList<NotifModel> notifModels;
    private FirebaseFirestore firestore;
    private String userName, userPhone;
    private Dialog dialog;
    private long orgTimeStamp;
    private int pos;

    public NotifAdapter(Context context, ArrayList<NotifModel> notifModels) {
        this.context = context;
        this.notifModels = notifModels;
        firestore = FirebaseFirestore.getInstance();
        dialog = new Dialog(context);
        userName = UdhaariApp.getInstance().getDataFromPref("serviceName");
        userPhone = UdhaariApp.getInstance().getDataFromPref("phone");
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notif_item_layout, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        String type = notifModels.get(position).getType();
        String name = notifModels.get(position).getName();
        String phone = notifModels.get(position).getPhone();

        if (type.equals("payment_request")) {
            holder.paymentReq.setVisibility(View.VISIBLE);
            holder.addFriendReq.setVisibility(View.INVISIBLE);

            String totalAmount = String.valueOf(notifModels.get(position).getTotalAmount());
            String amountPaying = String.valueOf(notifModels.get(position).getAmountPaying());

            SpannableStringBuilder str = new SpannableStringBuilder("You have been paid " + "₹" + amountPaying + " by " + name);
            str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 18, 20 + amountPaying.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 23 + amountPaying.length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.notifMessage.setText(str);
            holder.notifItem.setOnClickListener(v -> {
                boolean read = notifModels.get(position).getRead();
                pos = position;
                orgTimeStamp = notifModels.get(position).getTimeStamp();
                if (!read)
                    acceptPayment(name, phone, totalAmount, amountPaying);
            });
        }
        else {
            holder.paymentReq.setVisibility(View.INVISIBLE);
            holder.addFriendReq.setVisibility(View.VISIBLE);

            SpannableStringBuilder str = new SpannableStringBuilder("You have a new friend request from " + name);
            str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), str.length() - name.length() - 1, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.notifMessage.setText(str);
            holder.notifItem.setOnClickListener(v -> {
                boolean read = notifModels.get(position).getRead();
                pos = position;
                orgTimeStamp = notifModels.get(position).getTimeStamp();
                if (!read)
                    acceptRequest(name, phone);
            });
        }
    }

    private void acceptPayment(String name, String phone, String totalAmount, String amountPaying) {
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.amount_dialog_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView message = dialog.findViewById(R.id.amount_title);
        TextView shopName = dialog.findViewById(R.id.name_text_view);
        EditText amountEditText = dialog.findViewById(R.id.amount_edit_text);
        EditText descriptionEditText = dialog.findViewById(R.id.description_edit_text);
        Button acceptButton = dialog.findViewById(R.id.payment_button);
        View layer = dialog.findViewById(R.id.layer);
        ProgressBar loader = dialog.findViewById(R.id.loader);

        shopName.setText(name);
        message.setText("Accepting payment from:");
        acceptButton.setText("ACCEPT");
        amountEditText.setText(amountPaying);
        amountEditText.setEnabled(false);
        descriptionEditText.setVisibility(View.GONE);

        acceptButton.setOnClickListener(v -> {
            layer.setVisibility(View.VISIBLE);
            loader.setVisibility(View.VISIBLE);
            updateDatabase(name, phone, Integer.parseInt(totalAmount), Integer.parseInt(amountPaying));
        });

        dialog.show();
    }

    private void updateDatabase(String name, String phone, int amount, int amountPaying) {
        firestore.runTransaction(transaction -> {

            Date date = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
            String dateStr = sdf.format(date);
            String timeStr = sdf2.format(date);
            long timeStamp = System.currentTimeMillis();

            //updating Customer -> Pending amount
            transaction.update(firestore.collection("Customers")
                    .document(phone).collection("Pending").document(userPhone), "amount", amount-amountPaying);

            //adding Customer -> History
            VendorModel historyVendorModel = new VendorModel(userName, userPhone, amountPaying, dateStr, timeStr, "", timeStamp, "settled");
            transaction.set(firestore.collection("Customers").document(phone).collection("History").document(), historyVendorModel);

            //updating Customer -> Pending amount
            transaction.update(firestore.collection("Vendors")
                    .document(userPhone).collection("Pending").document(phone), "amount", amount-amountPaying);

            //adding Vendor -> History
            CustomerModel historyCustomerModel = new CustomerModel(name, phone, amountPaying, dateStr, timeStr, "", timeStamp, "settled");
            transaction.set(firestore.collection("Vendors").document(userPhone).collection("History").document(), historyCustomerModel);

            transaction.update(firestore.collection("Vendors").document(userPhone).collection("Notifs").document(String.valueOf(orgTimeStamp)), "read", true);
            return null;
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Timber.e("Added new payment successfully!");
                Toast.makeText(context, "Transaction added! Swipe to refresh.", Toast.LENGTH_SHORT).show();
                notifModels.get(pos).setRead(true);
                notifyItemChanged(pos);
            }
            else {
                Timber.e("Update failed: %s", task.getException().toString());
                Toast.makeText(context, "Transaction failed! Try again.", Toast.LENGTH_SHORT).show();
            }
            dialog.cancel();
        });
    }

    private void acceptRequest(String name, String phone) {
        Dialog requestDialog = new Dialog(context);
        requestDialog.setCancelable(true);
        requestDialog.setContentView(R.layout.friend_request_dialog);
        requestDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView message = requestDialog.findViewById(R.id.request_message);
        TextView nameTextView = requestDialog.findViewById(R.id.request_name);
        Button requestButton = requestDialog.findViewById(R.id.request_button);
        View layer = requestDialog.findViewById(R.id.layer);
        ProgressBar loader = requestDialog.findViewById(R.id.loader);

        nameTextView.setText(name);
        message.setText("You have a friend request from:");
        requestButton.setText("ACCEPT REQUEST");

        requestButton.setOnClickListener(v -> {
            layer.setVisibility(View.VISIBLE);
            loader.setVisibility(View.VISIBLE);

            WriteBatch batch = firestore.batch();

            batch.update(firestore.collection("Customers").document(phone).collection("Friends").document(userPhone), "status", "accepted");
            batch.update(firestore.collection("Vendors").document(userPhone).collection("Friends").document(phone), "status", "accepted");
            batch.update(firestore.collection("Vendors").document(userPhone).collection("Notifs").document(String.valueOf(orgTimeStamp)), "read", true);

            batch.commit()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Timber.e("Request accepted successfully");
                            Toast.makeText(context, "Request accepted!", Toast.LENGTH_SHORT).show();
                            notifModels.get(pos).setRead(true);
                            notifyItemChanged(pos);
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

    @Override
    public int getItemCount() {
        return notifModels.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {

        @BindView(R.id.notif_item)
        LinearLayout notifItem;
        @BindView(R.id.notif_message)
        TextView notifMessage;
        @BindView(R.id.add_friend_request)
        ImageView addFriendReq;
        @BindView(R.id.payment_request)
        ImageView paymentReq;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void updateList(ArrayList<NotifModel> list) {
        notifModels = list;
        notifyDataSetChanged();
    }
}
