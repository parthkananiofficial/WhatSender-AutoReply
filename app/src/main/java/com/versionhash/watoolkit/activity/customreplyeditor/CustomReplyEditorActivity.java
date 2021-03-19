package com.versionhash.watoolkit.activity.customreplyeditor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.versionhash.watoolkit.R;
import com.versionhash.watoolkit.activity.integration.WebIntegrationActivity;
import com.versionhash.watoolkit.model.helpers.RuleHelper;
import com.versionhash.watoolkit.model.preferences.PreferencesManager;
import com.versionhash.watoolkit.model.rules.Rule;

import java.util.Map;
import java.util.UUID;

import io.realm.Realm;


public class CustomReplyEditorActivity extends AppCompatActivity {
    TextInputEditText incomingMsgTextInput, replyMessageTextInputEdit;
    MaterialRadioButton exact_match_radio, starts_with_radio, contains_radio, does_not_contains_radio, anything_radio, static_radio, server_radio;
    TextInputLayout incomingMsgInputLayout, replyMsgTextInputLayout;
    RadioGroup condition_type_radio_group, anything_condition_type_radio_group;
    MaterialButton saveAutoReplyTextBtn,deleteCustomReplyBtn;
    String ruleId;

    private AdView mAdView;

    Realm realm;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        setContentView(R.layout.activity_custom_reply_editor);

        realm = Realm.getDefaultInstance();
        preferencesManager = PreferencesManager.getPreferencesInstance(this);

        ruleId = bundle.getString("ruleId");
        Rule rule = realm.where(Rule.class).equalTo("ruleId", ruleId).findFirst();

        incomingMsgInputLayout = findViewById(R.id.incomingMsgInputLayout);
        replyMsgTextInputLayout = findViewById(R.id.replyMsgTextInputLayout);

        incomingMsgTextInput = findViewById(R.id.incomingMsgTextInput);
        replyMessageTextInputEdit = findViewById(R.id.replyMessageTextInputEdit);
        saveAutoReplyTextBtn = findViewById(R.id.saveCustomReplyBtn);
        deleteCustomReplyBtn = findViewById(R.id.deleteCustomReplyBtn);

        anything_condition_type_radio_group = findViewById(R.id.anything_condition_type_radio_group);
        condition_type_radio_group = findViewById(R.id.condition_type_radio_group);

        exact_match_radio = findViewById(R.id.exact_match_radio);
        starts_with_radio = findViewById(R.id.starts_with_radio);
        contains_radio = findViewById(R.id.contains_radio);
        does_not_contains_radio = findViewById(R.id.does_not_contains_radio);
        anything_radio = findViewById(R.id.anything_radio);
        static_radio = findViewById(R.id.static_radio);
        server_radio = findViewById(R.id.server_radio);

        //enableDisableRadiogroup(anything_condition_type_radio_group,false);
        condition_type_radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.anything_radio) {
                    //enableDisableRadiogroup(anything_condition_type_radio_group, true);
                    if (server_radio.isChecked()) {
                        enableDisableTextEdit(replyMsgTextInputLayout, false);
                    }
                    enableDisableTextEdit(incomingMsgInputLayout, false);
                } else {
                    //enableDisableRadiogroup(anything_condition_type_radio_group, false);
                    enableDisableTextEdit(incomingMsgInputLayout, true);
                    enableDisableTextEdit(replyMsgTextInputLayout, true);
                }
            }
        });
        anything_condition_type_radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.server_radio) {
                        //check the configurations is saved or not otherwise open the webconfigurationactivity first
                        if (preferencesManager.getWebServer().equals("")) {
                            Intent intent = new Intent(CustomReplyEditorActivity.this, WebIntegrationActivity.class);
                            startActivity(intent);
                        }
                        enableDisableTextEdit(replyMsgTextInputLayout, false);
                }else{
                    enableDisableTextEdit(replyMsgTextInputLayout, true);
                }
            }
        });


        if (rule != null) {
            incomingMsgTextInput.setText(rule.getConditionMsg());
            switch (rule.getIncomingMsgCondition()) {
                case Rule.EXACT:
                    exact_match_radio.setChecked(true);
                    break;
                case Rule.STARTS_WITH:
                    starts_with_radio.setChecked(true);
                    break;
                case Rule.CONTAINS:
                    contains_radio.setChecked(true);
                    break;
                case Rule.DOES_NOT_CONTAINS:
                    does_not_contains_radio.setChecked(true);
                    break;
                case Rule.ANYTHING:
                    anything_radio.setChecked(true);
                    break;
            }
            switch (rule.getResponseMsgSourceCondition()) {
                case Rule.STATIC:
                    static_radio.setChecked(true);
                    replyMessageTextInputEdit.setText(rule.getReplyMsg());
                    break;
                case Rule.WEBSERVER:
                    server_radio.setChecked(true);
                    break;
            }
            deleteCustomReplyBtn.setVisibility(View.VISIBLE);

//            if(!rule.getConditionType().equals(Rule.ANYTHING))
//            {
//                enableDisableRadiogroup(anything_condition_type_radio_group,false);
//                enableDisableTextEdit(incomingMsgInputLayout,true);
//                enableDisableTextEdit(replyMsgTextInputLayout,true);
//            }
        }
        incomingMsgTextInput.requestFocus();

        replyMessageTextInputEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Disable save button if text does not satisfy requirements
                // set the validation check
            }
        });

        saveAutoReplyTextBtn.setOnClickListener(view -> {
            saveData();
            if (incomingMsgTextInput.getText().toString().trim() != null && replyMessageTextInputEdit.getText().toString().trim() != null) {
                this.onNavigateUp();
            }
        });
        deleteCustomReplyBtn.setOnClickListener(view -> {
            deleteData();
            this.onNavigateUp();
        });

        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        loadBanner();
    }

    private void deleteData() {
        RuleHelper.deleteById(ruleId);
    }

    private void saveData() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Rule rule = new Rule();
                if (ruleId.isEmpty() || ruleId == null) {
                    rule.setRuleId(UUID.randomUUID().toString()); //create new rule
                } else {
                    rule.setRuleId(ruleId); //update the record
                }
                rule.setConditionMsg(incomingMsgTextInput.getText().toString().trim());
                rule.setReplyMsg(replyMessageTextInputEdit.getText().toString().trim());
                if (exact_match_radio.isChecked())
                    rule.setIncomingMsgCondition(exact_match_radio.getTag().toString());
                else if (starts_with_radio.isChecked())
                    rule.setIncomingMsgCondition(starts_with_radio.getTag().toString());
                else if (contains_radio.isChecked())
                    rule.setIncomingMsgCondition(contains_radio.getTag().toString());
                else if (does_not_contains_radio.isChecked())
                    rule.setIncomingMsgCondition(does_not_contains_radio.getTag().toString());
                else if (anything_radio.isChecked()) {
                    rule.setIncomingMsgCondition(anything_radio.getTag().toString());
                }

                if (server_radio.isChecked()) {
                    rule.setResponseMsgSourceCondition(Rule.WEBSERVER);
                } else {
                    rule.setResponseMsgSourceCondition(Rule.STATIC);
                }
                realm.copyToRealmOrUpdate(rule);
            }
        });
    }

    private void enableDisableRadiogroup(RadioGroup radioGroup, boolean isEnabled) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(isEnabled);
        }
    }

    private void enableDisableTextEdit(TextInputLayout layout, boolean isEnabled) {
        if (isEnabled) {
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
        }
    }

    private void enableDisableTextEditOld(TextInputEditText textEdit, String txt, boolean isEnabled) {
        if (isEnabled) {
            if (textEdit.getText().toString().equals(txt)) {
                textEdit.setText("");
            }
            textEdit.setEnabled(true);
            textEdit.setVisibility(View.VISIBLE);
        } else {
            textEdit.setText(txt);
            textEdit.setEnabled(false);
            textEdit.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
    private void loadBanner()
    {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                for (String adapterClass : statusMap.keySet()) {
                    AdapterStatus status = statusMap.get(adapterClass);
                    Log.d("MyApp", String.format(
                            "Adapter name: %s, Description: %s, Latency: %d",
                            adapterClass, status.getDescription(), status.getLatency()));
                }

                // Start loading ads here...
                mAdView = findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }
        });
    }
}