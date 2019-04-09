package project.tronku.udhaari.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;
import project.tronku.udhaari.Fragments.Customer.CustomerHistoryFrag;
import project.tronku.udhaari.Fragments.Customer.CustomerNotifFrag;
import project.tronku.udhaari.Fragments.Customer.CustomerPendingFrag;
import project.tronku.udhaari.R;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerMainActivity extends AppCompatActivity {

    @BindView(R.id.customer_bottom_nav_view)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.customer_toolbar)
    Toolbar toolbar;

    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        loadFragment(new CustomerPendingFrag());

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.customer_pending :
                    if (position != 0) {
                        position = 0;
                        loadFragment(new CustomerPendingFrag());
                    }
                    return true;

                case R.id.customer_history :
                    if (position != 1) {
                        position = 1;
                        loadFragment(new CustomerHistoryFrag());
                    }
                    return true;

                case R.id.customer_notif :
                    if (position != 2) {
                        position = 2;
                        loadFragment(new CustomerNotifFrag());
                    }
                    return true;
            }
            return false;
        });
    }

    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.customer_fragments_framelayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
