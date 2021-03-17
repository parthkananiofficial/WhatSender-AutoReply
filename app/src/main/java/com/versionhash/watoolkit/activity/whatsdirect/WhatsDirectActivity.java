package com.versionhash.watoolkit.activity.whatsdirect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.hbb20.CountryCodePicker;
import com.versionhash.watoolkit.R;

public class WhatsDirectActivity extends AppCompatActivity {
    CountryCodePicker ccp;
    Button sendBtn;
    TextInputEditText phoneNumber, message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whats_direct);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        phoneNumber = findViewById(R.id.whatsDirectNumber);
        message = findViewById(R.id.whatsDirectMessage);

        sendBtn = findViewById(R.id.sendWhatsDirectBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send the message
                sendMessage(ccp.getSelectedCountryCode() + phoneNumber.getText().toString(), message.getText().toString());
            }
        });
    }

    private void sendMessage(String phoneNo, String msg) {
        phoneNo = phoneNo.replace("+", "").replace(" ", "");
        Intent sendIntent = new Intent("android.intent.action.MAIN");
        sendIntent.setAction(Intent.ACTION_VIEW);
        sendIntent.setPackage("com.whatsapp");
        String url = "https://api.whatsapp.com/send?phone=" + phoneNo + "&text=" + msg;
        sendIntent.setData(Uri.parse(url));
        if (sendIntent.resolveActivity(this.getPackageManager()) != null) {
            startActivity(sendIntent);
        }
    }
}