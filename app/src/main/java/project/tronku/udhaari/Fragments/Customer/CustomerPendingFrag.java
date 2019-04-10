package project.tronku.udhaari.Fragments.Customer;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import project.tronku.udhaari.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerPendingFrag extends Fragment {

    @BindView(R.id.customer_pending_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.customer_total_pending)
    TextView totalPendingTextView;
    @BindView(R.id.loader)
    ProgressBar loader;
    @BindView(R.id.layer)
    View layer;

    private Unbinder unbinder;
    private View view;

    public CustomerPendingFrag() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_customer_pending, container, false);
        unbinder = ButterKnife.bind(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
