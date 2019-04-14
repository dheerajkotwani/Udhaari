package project.tronku.udhaari.Fragments.Customer;


import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import javax.annotation.Nullable;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import project.tronku.udhaari.Adapters.HistoryAdapter;
import project.tronku.udhaari.Models.PaymentModel;
import project.tronku.udhaari.R;
import project.tronku.udhaari.UdhaariApp;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerHistoryFrag extends Fragment {

    @BindView(R.id.customer_history_swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.customer_history_recycler_view)
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
    private HistoryAdapter adapter;
    private ArrayList<PaymentModel> paymentModels = new ArrayList<>();

    private FirebaseFirestore firestore;

    public CustomerHistoryFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_customer_history, container, false);
        unbinder = ButterKnife.bind(this, view);
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();

        firestore.setFirestoreSettings(settings);

        adapter = new HistoryAdapter(getContext(), paymentModels);
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

        firestore.collection("Customers").document(phone)
                .collection("History")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {

                    ArrayList<PaymentModel> list = new ArrayList<>();

                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            String name = snapshot.get("serviceName").toString();
                            String phoneNo = snapshot.get("phone").toString();
                            int amount = Integer.parseInt(snapshot.get("amount").toString());
                            String date = snapshot.get("date").toString();
                            String time = snapshot.get("time").toString();
                            long timestamp = snapshot.getLong("timeStamp");
                            String description = snapshot.get("description").toString();
                            String status = snapshot.get("status").toString();

                            PaymentModel paymentModel = new PaymentModel(name, phoneNo, amount, date, time, description, timestamp, status);
                            list.add(paymentModel);
                        }
                        //update adapter
                        paymentModels.clear();
                        paymentModels = list;

                        if (paymentModels.size() == 0)
                            noDataLayout.setVisibility(View.VISIBLE);

                        adapter.updateList(paymentModels);

                    }
                    else {
                        Timber.e("OnComplete: Fetching history data failed! %s", task.getException().toString());
                        errorLayout.setVisibility(View.VISIBLE);
                    }

                    swipeRefreshLayout.setRefreshing(false);
                    loader.setVisibility(View.INVISIBLE);
                    layer.setVisibility(View.INVISIBLE);

                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
