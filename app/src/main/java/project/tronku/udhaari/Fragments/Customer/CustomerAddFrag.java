package project.tronku.udhaari.Fragments.Customer;


import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import project.tronku.udhaari.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerAddFrag extends Fragment {

    private View view;
    @BindView(R.id.add_description)
    TextView addDescription;
    @BindView(R.id.description)
    EditText description;
    @BindView(R.id.back_button)
    ImageView backButton;
    @BindView(R.id.search)
    FrameLayout searchLayout;
    @BindView(R.id.add_amount)
    FrameLayout addAmountLayout;

    private Unbinder unbinder;

    public CustomerAddFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_customer_add, container, false);
        unbinder = ButterKnife.bind(this, view);

        addDescription.setOnClickListener(view -> description.setVisibility(View.VISIBLE));
        backButton.setOnClickListener(view -> {
            searchLayout.setVisibility(View.VISIBLE);
            addAmountLayout.setVisibility(View.GONE);
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
