package project.tronku.udhaari.Fragments.Vendor;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import project.tronku.udhaari.Adapters.PendingAdapter;
import project.tronku.udhaari.Models.PaymentModel;
import project.tronku.udhaari.R;
import project.tronku.udhaari.UdhaariApp;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class VendorPendingFrag extends Fragment {

    @BindView(R.id.vendor_pending_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.vendor_pending_swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.vendor_total_pending)
    TextView totalPendingTextView;
    @BindView(R.id.owe_layout)
    RelativeLayout oweLayout;
    @BindView(R.id.no_payment_layout)
    LinearLayout noPaymentLayout;
    @BindView(R.id.error_layout)
    LinearLayout errorLayout;
    @BindView(R.id.loader)
    ProgressBar loader;
    @BindView(R.id.layer)
    View layer;

    private Unbinder unbinder;
    private View view;
    private ArrayList<PaymentModel> paymentsList = new ArrayList<>();
    private PendingAdapter adapter;
    private int totalPending = 0;

    private FirebaseFirestore firestore;

    public VendorPendingFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_vendor_pending, container, false);
        unbinder = ButterKnife.bind(this, view);
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();

        firestore.setFirestoreSettings(settings);

        adapter = new PendingAdapter(getContext(), paymentsList);
        String phone = UdhaariApp.getInstance().getDataFromPref("phone");
        fillRecyclerView(phone);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefreshLayout.setOnRefreshListener(() -> fillRecyclerView(phone));

        return view;
    }

    private void fillRecyclerView(String phone) {
        if (totalPending == 0)
            oweLayout.setVisibility(View.INVISIBLE);

        loader.setVisibility(View.VISIBLE);
        layer.setVisibility(View.VISIBLE);
        totalPending = 0;

        firestore.collection("Vendors").document(phone)
                .collection("Pending")
                .get()
                .addOnCompleteListener(task -> {

                    ArrayList<PaymentModel> list = new ArrayList<>();

                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            String name = snapshot.get("name").toString();
                            String phoneNo = snapshot.get("phone").toString();
                            int amount = Integer.parseInt(snapshot.get("amount").toString());
                            totalPending += amount;

                            PaymentModel paymentModel = new PaymentModel(name, phoneNo, amount);
                            list.add(paymentModel);
                        }
                        //update adapter
                        UdhaariApp.getInstance().saveToPref("totalPending", String.valueOf(totalPending));
                        paymentsList.clear();
                        paymentsList = list;

                        if (paymentsList.size() != 0 && totalPending != 0) {
                            totalPendingTextView.setText(String.valueOf(totalPending));
                            noPaymentLayout.setVisibility(View.INVISIBLE);
                            oweLayout.setVisibility(View.VISIBLE);
                        }
                        else {
                            noPaymentLayout.setVisibility(View.VISIBLE);
                            oweLayout.setVisibility(View.INVISIBLE);
                        }
                        adapter.updateList(paymentsList);

                    }
                    else {
                        Timber.e("OnComplete: Fetching pending data failed! %s", task.getException().toString());
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
