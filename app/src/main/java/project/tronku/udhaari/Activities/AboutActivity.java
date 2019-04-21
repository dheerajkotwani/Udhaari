package project.tronku.udhaari.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import project.tronku.udhaari.BuildConfig;
import project.tronku.udhaari.R;

public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.build_version)
    TextView buildVersion;
    @BindView(R.id.facebook)
    LinearLayout facebookLayout;
    @BindView(R.id.web)
    LinearLayout webLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ButterKnife.bind(this);

        facebookLayout.setOnClickListener(v -> {
            Intent fb = new Intent();
            fb.setAction(Intent.ACTION_VIEW);
            fb.setData(Uri.parse("http://www.facebook.com/dscjssnoida"));
            startActivity(fb);
        });

        webLayout.setOnClickListener(v -> {
            Intent web = new Intent();
            web.setAction(Intent.ACTION_VIEW);
            web.setData(Uri.parse("http://dscjss.in"));
            startActivity(web);
        });

        String buildNo = "v." + BuildConfig.VERSION_NAME;
        buildVersion.setText(buildNo);
    }
}
