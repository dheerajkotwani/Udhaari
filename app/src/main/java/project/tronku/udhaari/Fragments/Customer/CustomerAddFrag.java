package project.tronku.udhaari.Fragments.Customer;


import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import project.tronku.udhaari.Adapters.VendorListAdapter;
import project.tronku.udhaari.Models.PaymentModel;
import project.tronku.udhaari.R;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerAddFrag extends Fragment {

    @BindView(R.id.search_edit_text)
    EditText searchEditText;
    @BindView(R.id.vendor_list_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.layer)
    View layer;
    @BindView(R.id.loader)
    ProgressBar loader;
    @BindView(R.id.error_layout)
    LinearLayout errorLayout;

    private Unbinder unbinder;
    private View view;
    private VendorListAdapter adapter;
    private ArrayList<PaymentModel> models = new ArrayList<>();

    private FirebaseFirestore firestore;

    public CustomerAddFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_customer_add, container, false);
        unbinder = ButterKnife.bind(this, view);
        firestore = FirebaseFirestore.getInstance();

        adapter = new VendorListAdapter(getContext(), models);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        fetchVendorList();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(s.toString());
            }
        });

        return view;
    }

    private void fetchVendorList() {

        layer.setVisibility(View.VISIBLE);
        loader.setVisibility(View.VISIBLE);

        firestore.collection("Vendors")
                .get()
                .addOnCompleteListener(task -> {

                    ArrayList<PaymentModel> list = new ArrayList<>();

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            String name = snapshot.get("serviceName").toString();
                            String phoneNo = snapshot.get("phone").toString();
                            PaymentModel model = new PaymentModel(name, phoneNo);
                            list.add(model);
                        }
                        models.clear();
                        models = list;
                        adapter.updateList(models);
                    }
                    else {
                        Timber.e("OnComplete: Fetching pending data failed!");
                        errorLayout.setVisibility(View.VISIBLE);
                    }

                    loader.setVisibility(View.INVISIBLE);
                    layer.setVisibility(View.INVISIBLE);

                })
                .addOnFailureListener(e -> {
                    Timber.e("OnFailure: Fetching pending data failed!");
                    errorLayout.setVisibility(View.VISIBLE);
                    loader.setVisibility(View.INVISIBLE);
                    layer.setVisibility(View.INVISIBLE);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
