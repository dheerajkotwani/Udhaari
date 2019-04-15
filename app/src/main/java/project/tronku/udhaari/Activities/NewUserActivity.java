package project.tronku.udhaari.Activities;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import project.tronku.udhaari.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

public class NewUserActivity extends AppCompatActivity {

    @BindView(R.id.customer_option)
    LinearLayout customerOption;
    @BindView(R.id.vendor_option)
    LinearLayout vendorOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        ButterKnife.bind(this);

        customerOption.setOnClickListener(v -> startActivity(new Intent(NewUserActivity.this, CustomerSignUpActivity.class)));

        vendorOption.setOnClickListener(v -> startActivity(new Intent(NewUserActivity.this, VendorSignUpActivity.class)));
    }
}
