package project.tronku.udhaari.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import project.tronku.udhaari.BuildConfig;
import project.tronku.udhaari.R;
import project.tronku.udhaari.UdhaariApp;
import timber.log.Timber;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.phone_number_edit_text)
    EditText phoneNumberEditText;
    @BindView(R.id.otp_edit_text)
    PinView otpEditText;
    @BindView(R.id.get_otp_button)
    Button getOTPButton;
    @BindView(R.id.login_signup_button)
    Button loginSignupButton;

    @BindView(R.id.layer)
    View layer;
    @BindView(R.id.loader)
    ProgressBar loader;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendingToken;

    private String codeSent, phone, type;
    private boolean newUser;

    private AlphaAnimation fade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        fade = new AlphaAnimation(0f, 1f);
        fade.setDuration(400);
        fade.setInterpolator(new AccelerateDecelerateInterpolator());

        getOTPButton.setOnClickListener(v -> {

            phone = phoneNumberEditText.getText().toString();

            if (phone.isEmpty()) {
                phoneNumberEditText.setError("Enter your phone number.");
                phoneNumberEditText.requestFocus();
            }
            else if (phone.length() != 10) {
                phoneNumberEditText.setError("Enter valid number.");
                phoneNumberEditText.requestFocus();
            }
            else {
                otpEditText.requestFocus();
                sendOTP("+91" + phone);
            }

        });

        loginSignupButton.setOnClickListener(v -> {
            String otpCode = otpEditText.getText().toString();
            if (otpCode.isEmpty()) {
                otpEditText.setError("Enter the OTP!");
                otpEditText.requestFocus();
            }
            else {
                layer.setVisibility(View.VISIBLE);
                loader.setVisibility(View.VISIBLE);
                verifyOTP(otpCode);
            }
        });

        //initializing callback for OTP
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                //valid number!
                Timber.d("Verification successful");
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Timber.d("Verification failed");
                getOTPButton.setVisibility(View.VISIBLE);
                loginSignupButton.setVisibility(View.GONE);
                getOTPButton.startAnimation(fade);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                codeSent = s;
                resendingToken = forceResendingToken;
                Timber.e("Code = %s", s);
            }
        };

    }

    private void verifyOTP(String otpCode) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, otpCode);
        signInWithPhoneAuthCredential(credential);
        hideKeyboard();
    }

    private void sendOTP(String phone) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);

        getOTPButton.setVisibility(View.GONE);
        loginSignupButton.setVisibility(View.VISIBLE);
        loginSignupButton.startAnimation(fade);
        hideKeyboard();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Verified!", Toast.LENGTH_SHORT).show();
                        UdhaariApp.getInstance().saveToPref("phone", "+91" + phone);

                        //checking for new user
                        firestore.collection("Users").whereEqualTo("phone", "+91" + phone)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                                        newUser = true;
                                        Timber.e("New user = %s", newUser);

                                        Intent newUser = new Intent(MainActivity.this, NewUserActivity.class);
                                        startActivity(newUser);
                                        finish();
                                    }

                                    else {
                                        newUser = false;
                                        Timber.e("New user = %s", newUser);
                                        Timber.e("Already registered");

                                        type = queryDocumentSnapshots.getDocuments().get(0).get("type").toString();
                                        UdhaariApp.getInstance().saveToPref("type", type);
                                        if (type.equals("customer")) {
                                            Timber.e("CUSTOMER");
                                            startActivity(new Intent(MainActivity.this, CustomerMainActivity.class));
                                            finish();
                                        }
                                        else if (type.equals("vendor")) {
                                            Timber.e("VENDOR");
                                            startActivity(new Intent(MainActivity.this, VendorMainActivity.class));
                                            finish();
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Timber.e("Error in fetching");
                                });

                    } else {
                        // Sign in failed, display a message and update the UI
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(MainActivity.this, "Invalid OTP!", Toast.LENGTH_SHORT).show();
                            otpEditText.setText("");
                        }
                    }

                    layer.setVisibility(View.INVISIBLE);
                    loader.setVisibility(View.INVISIBLE);

                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            if (UdhaariApp.getInstance().getPref().contains("phone")) {
                if (UdhaariApp.getInstance().getPref().contains("name")) {
                    if (UdhaariApp.getInstance().getDataFromPref("type").equals("customer"))
                        startActivity(new Intent(MainActivity.this, CustomerMainActivity.class));
                    else if (UdhaariApp.getInstance().getDataFromPref("type").equals("vendor"))
                        startActivity(new Intent(MainActivity.this, VendorMainActivity.class));
                }
                else
                    startActivity(new Intent(MainActivity.this, NewUserActivity.class));
            }
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
