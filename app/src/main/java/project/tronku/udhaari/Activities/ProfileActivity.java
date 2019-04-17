package project.tronku.udhaari.Activities;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import project.tronku.udhaari.R;
import project.tronku.udhaari.UdhaariApp;
import timber.log.Timber;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.sql.Time;

public class ProfileActivity extends AppCompatActivity {

    @BindView(R.id.user_profile)
    ImageView proPic;
    @BindView(R.id.user_name)
    EditText userNameEditText;
    @BindView(R.id.user_phone_no)
    EditText userPhoneEditText;
    @BindView(R.id.user_service_name)
    EditText userServiceEditText;
    @BindView(R.id.edit_button)
    Button editProfileButton;
    @BindView(R.id.save_button)
    Button saveProfileButton;
    @BindView(R.id.layer)
    View layer;
    @BindView(R.id.loader)
    ProgressBar loader;

    private FirebaseFirestore firestore;
    private AlphaAnimation fade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firestore = FirebaseFirestore.getInstance();
        ButterKnife.bind(this);

        fade = new AlphaAnimation(0f, 1f);
        fade.setDuration(400);
        fade.setInterpolator(new AccelerateDecelerateInterpolator());


        String type = UdhaariApp.getInstance().getDataFromPref("type");
        String name = UdhaariApp.getInstance().getDataFromPref("name");
        String phone = UdhaariApp.getInstance().getDataFromPref("phone");

        userNameEditText.setText(name);
        userPhoneEditText.setText(phone);

        if (type.equals("vendor")) {
            String service = UdhaariApp.getInstance().getDataFromPref("serviceName");
            userServiceEditText.setVisibility(View.VISIBLE);
            userServiceEditText.setText(service);
        }

        editProfileButton.setOnClickListener(v -> {
            userNameEditText.setEnabled(true);
            userServiceEditText.setEnabled(true);
            userNameEditText.requestFocus();
            editProfileButton.setVisibility(View.GONE);
            saveProfileButton.setVisibility(View.VISIBLE);
            saveProfileButton.startAnimation(fade);
        });

        saveProfileButton.setOnClickListener(v -> {
            hideKeyboard();
            String newName = userNameEditText.getText().toString();
            if (newName.isEmpty()) {
                userNameEditText.setError("Enter name.");
                userNameEditText.requestFocus();
            }
            if (type.equals("vendor")) {
                String newService = userServiceEditText.getText().toString();
                if (newService.isEmpty()) {
                    userServiceEditText.setError("Enter service name.");
                    userServiceEditText.requestFocus();
                }
                else {
                    loader.setVisibility(View.VISIBLE);
                    layer.setVisibility(View.VISIBLE);
                    updateVendor(newName, newService, phone);
                }
            }
            else {
                loader.setVisibility(View.VISIBLE);
                layer.setVisibility(View.VISIBLE);
                userNameEditText.setEnabled(false);
                userServiceEditText.setEnabled(false);
                updateCustomer(newName, phone);
            }
        });
    }

    private void updateCustomer(String newName, String phone) {
        WriteBatch batch = firestore.batch();
        batch.update(firestore.collection("Customers").document(phone), "name", newName);
        batch.update(firestore.collection("Users").document(phone), "name", newName);

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Timber.e("Profile Updated!");
                Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
            }
            else {
                Timber.e("Profile update failed: %s", task.getException().toString());
                Toast.makeText(this, "Something went wrong! Try again!", Toast.LENGTH_SHORT).show();
            }
            layer.setVisibility(View.INVISIBLE);
            loader.setVisibility(View.INVISIBLE);
            saveProfileButton.setVisibility(View.GONE);
            editProfileButton.setVisibility(View.VISIBLE);
            editProfileButton.startAnimation(fade);
        });
    }

    private void updateVendor(String newName, String newService, String phone) {
        WriteBatch batch = firestore.batch();
        batch.update(firestore.collection("Vendors").document(phone), "name", newName);
        batch.update(firestore.collection("Vendors").document(phone), "serviceName", newService);
        batch.update(firestore.collection("Users").document(phone), "name", newName);

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Timber.e("Profile Updated!");
                Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
            }
            else {
                Timber.e("Profile update failed: %s", task.getException().toString());
                Toast.makeText(this, "Something went wrong! Try again!", Toast.LENGTH_SHORT).show();
            }
            layer.setVisibility(View.INVISIBLE);
            loader.setVisibility(View.INVISIBLE);
            saveProfileButton.setVisibility(View.GONE);
            editProfileButton.setVisibility(View.VISIBLE);
            editProfileButton.startAnimation(fade);
        });
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
