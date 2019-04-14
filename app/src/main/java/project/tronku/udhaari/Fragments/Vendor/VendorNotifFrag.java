package project.tronku.udhaari.Fragments.Vendor;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import project.tronku.udhaari.Adapters.HistoryAdapter;
import project.tronku.udhaari.Adapters.NotifAdapter;
import project.tronku.udhaari.Models.NotifModel;
import project.tronku.udhaari.Models.PaymentModel;
import project.tronku.udhaari.R;
import project.tronku.udhaari.UdhaariApp;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class VendorNotifFrag extends Fragment {

    @BindView(R.id.vendor_notif_swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.vendor_notif_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.error_layout)
    LinearLayout errorLayout;
    @BindView(R.id.no_data_layout)
    LinearLayout noDataLayout;
    @BindView(R.id.loader)
    ProgressBar loader;
    @BindView(R.id.layer)
    View layer;

    private View view;
    private Unbinder unbinder;
    private ArrayList<NotifModel> notifModels = new ArrayList<>();
    private NotifAdapter adapter;

    private FirebaseFirestore firestore;

    public VendorNotifFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_vendor_notif, container, false);
        unbinder = ButterKnife.bind(this, view);
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();

        firestore.setFirestoreSettings(settings);

        adapter = new NotifAdapter(getContext(), notifModels);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        String phone = UdhaariApp.getInstance().getDataFromPref("phone");
        fillRecyclerView(phone);

        swipeRefreshLayout.setOnRefreshListener(() -> fillRecyclerView(phone));

        return view;
    }

    private void fillRecyclerView(String phone) {
        loader.setVisibility(View.VISIBLE);
        layer.setVisibility(View.VISIBLE);

        firestore.collection("Vendors").document(phone)
                .collection("Notifs")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {

                    ArrayList<NotifModel> list = new ArrayList<>();

                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            String name = snapshot.get("name").toString();
                            String phoneNo = snapshot.get("phone").toString();
                            String type = snapshot.get("type").toString();
                            long timestamp = snapshot.getLong("timeStamp");
                            boolean read = snapshot.getBoolean("read");

                            if (type.equals("payment_request")) {
                                int amount = Integer.parseInt(snapshot.get("amount").toString());
                                String description = snapshot.get("description").toString();
                                NotifModel model = new NotifModel(name, phoneNo, description, type, amount, timestamp, read);
                                list.add(model);
                            }
                            else {
                                NotifModel model = new NotifModel(name, phoneNo, type, timestamp, read);
                                list.add(model);
                            }

                        }
                        //update adapter
                        notifModels.clear();
                        notifModels = list;

                        if (notifModels.size() == 0)
                            noDataLayout.setVisibility(View.VISIBLE);

                        adapter.updateList(notifModels);

                    }
                    else {
                        Timber.e("OnComplete: Fetching notif data failed! %s", task.getException().toString());
                        errorLayout.setVisibility(View.VISIBLE);
                    }

                    swipeRefreshLayout.setRefreshing(false);
                    loader.setVisibility(View.INVISIBLE);
                    layer.setVisibility(View.INVISIBLE);

                });
    }


}
