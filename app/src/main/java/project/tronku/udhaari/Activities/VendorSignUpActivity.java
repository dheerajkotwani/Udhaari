package project.tronku.udhaari.Activities;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import project.tronku.udhaari.Models.VendorModel;
import project.tronku.udhaari.R;
import project.tronku.udhaari.UdhaariApp;
import timber.log.Timber;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class VendorSignUpActivity extends AppCompatActivity {

    @BindView(R.id.vendor_signup_name)
    EditText vendorNameEditText;
    @BindView(R.id.vendor_signup_service_name)
    EditText vendorServiceNameEditText;
    @BindView(R.id.vendor_signup_phone_no)
    EditText vendorPhoneEditText;
    @BindView(R.id.vendor_profile)
    ImageView vendorProfilePic;
    @BindView(R.id.vendor_signup_button)
    Button signupButton;
    @BindView(R.id.layer)
    View layer;
    @BindView(R.id.loader)
    ProgressBar loader;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_sign_up);

        ButterKnife.bind(this);
        firestore = FirebaseFirestore.getInstance();

        String phone = UdhaariApp.getInstance().getDataFromPref("phone");
        vendorPhoneEditText.setText(phone);

        //TODO uploading profile pic

        signupButton.setOnClickListener(v -> {
            String name = vendorNameEditText.getText().toString();
            String serviceName = vendorServiceNameEditText.getText().toString();

            if (name.isEmpty() && serviceName.isEmpty()) {
                vendorNameEditText.setError("Enter details.");
                vendorNameEditText.requestFocus();
            }
            else if (name.isEmpty()) {
                vendorNameEditText.setError("Enter name.");
                vendorNameEditText.requestFocus();
            }
            else if (serviceName.isEmpty()) {
                vendorServiceNameEditText.requestFocus();
                vendorServiceNameEditText.setError("Enter service name.");
            }
            else {
                hideKeyboard();
                loader.setVisibility(View.VISIBLE);
                layer.setVisibility(View.VISIBLE);
                signUpVendor(name, serviceName, phone);
            }
        });
    }

    private void signUpVendor(String name, String serviceName, String phone) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("phone", phone);
        userData.put("serviceName", serviceName);
        userData.put("type", "vendor");

        Map<String, Object> vendorData = new HashMap<>();
        vendorData.put("name", name);
        vendorData.put("phone", phone);
        vendorData.put("serviceName", serviceName);

        WriteBatch batch = firestore.batch();

        batch.set(firestore.collection("Vendors").document(phone), vendorData);
        batch.set(firestore.collection("Users").document(phone), userData);

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Timber.e("Signed up successfully!");
                startActivity(new Intent(this, VendorMainActivity.class));
                finish();
            }
            else {
                Timber.e("User signing failed: %s", task.getException().toString());
                loader.setVisibility(View.INVISIBLE);
                layer.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Sign-up failed!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
