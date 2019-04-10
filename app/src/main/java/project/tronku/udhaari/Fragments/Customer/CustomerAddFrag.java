package project.tronku.udhaari.Fragments.Customer;


import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import project.tronku.udhaari.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerAddFrag extends Fragment {

    @BindView(R.id.search_layout)
    FrameLayout searchLayout;
    @BindView(R.id.vendor_list_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.search_edit_text)
    EditText searchVendorEditText;

    @BindView(R.id.add_amount_layout)
    FrameLayout addAmountLayout;
    @BindView(R.id.amount_edit_text)
    EditText amountEditText;
    @BindView(R.id.description_edit_text)
    EditText descriptionEditText;
    @BindView(R.id.borrow_button)
    Button borrowButton;
    @BindView(R.id.search_again_button)
    LinearLayout searchAgainButton;

    private Unbinder unbinder;
    private View view;

    public CustomerAddFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_customer_add, container, false);
        unbinder = ButterKnife.bind(this, view);

        //TODO need adapter with onclicklistener interface

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
