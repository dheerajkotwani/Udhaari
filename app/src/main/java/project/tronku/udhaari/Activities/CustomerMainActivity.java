package project.tronku.udhaari.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;
import project.tronku.udhaari.Fragments.Customer.CustomerAddFrag;
import project.tronku.udhaari.Fragments.Customer.CustomerHistoryFrag;
import project.tronku.udhaari.Fragments.Customer.CustomerPendingFrag;
import project.tronku.udhaari.R;
import project.tronku.udhaari.UdhaariApp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class CustomerMainActivity extends AppCompatActivity {

    @BindView(R.id.customer_bottom_nav_view)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.customer_toolbar)
    Toolbar toolbar;

    private FirebaseAuth auth;

    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

        ButterKnife.bind(this);
        auth = FirebaseAuth.getInstance();

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

                case R.id.customer_add :
                    if (position != 2) {
                        position = 2;
                        loadFragment(new CustomerAddFrag());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.profile:
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.sign_out:
                auth.signOut();
                UdhaariApp.getInstance().clearPrefs();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
        }
        return false;
    }
}
