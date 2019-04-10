package project.tronku.udhaari.Fragments.Customer;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import project.tronku.udhaari.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerAddFrag extends Fragment {


    public CustomerAddFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customer_add, container, false);
    }

}
