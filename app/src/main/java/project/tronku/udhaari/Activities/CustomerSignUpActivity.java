package project.tronku.udhaari.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import project.tronku.udhaari.Models.CustomerModel;
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

import java.util.HashMap;
import java.util.Map;

public class CustomerSignUpActivity extends AppCompatActivity {

    @BindView(R.id.customer_signup_name)
    EditText customerNameEditText;
    @BindView(R.id.customer_signup_phone_no)
    EditText customerPhoneEditText;
    @BindView(R.id.customer_profile)
    ImageView customerProfilePic;
    @BindView(R.id.customer_signup_button)
    Button signupButton;
    @BindView(R.id.layer)
    View layer;
    @BindView(R.id.loader)
    ProgressBar loader;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_sign_up);

        ButterKnife.bind(this);
        firestore = FirebaseFirestore.getInstance();

        String phone = UdhaariApp.getInstance().getDataFromPref("phone");
        customerPhoneEditText.setText(phone);

        //TODO uploading profile pic

        signupButton.setOnClickListener(v -> {
            String name = customerNameEditText.getText().toString();
            if (name.isEmpty()) {
                customerNameEditText.setError("Enter Name.");
                customerNameEditText.requestFocus();
            }
            else {
                loader.setVisibility(View.VISIBLE);
                layer.setVisibility(View.VISIBLE);
                hideKeyboard();
                signupCustomer(name, phone);
            }
        });
    }

    private void signupCustomer(String name, String phone) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("phone", phone);
        userData.put("type", "customer");

        CustomerModel newCustomer = new CustomerModel(name, phone);

        firestore.collection("Users")
                .document(phone)
                .set(userData)
                .addOnSuccessListener(v -> {

                    firestore.collection("Customers")
                            .document(phone)
                            .set(newCustomer)
                            .addOnSuccessListener(s -> {

                                Timber.e("Signed up successfully!");
                                UdhaariApp.getInstance().saveToPref("name", name);
                                startActivity(new Intent(this, CustomerMainActivity.class));
                                finish();

                            })
                            .addOnFailureListener(f -> {
                                Timber.e("Sign-up failed!");
                                Timber.e("Failure!");
                                loader.setVisibility(View.INVISIBLE);
                                layer.setVisibility(View.INVISIBLE);
                                Toast.makeText(this, "Sign-up failed!", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(v -> {
                    Timber.e("User signing failed!");
                    Timber.e("Failure!");
                    loader.setVisibility(View.INVISIBLE);
                    layer.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Sign-up failed!", Toast.LENGTH_SHORT).show();
                });

    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
