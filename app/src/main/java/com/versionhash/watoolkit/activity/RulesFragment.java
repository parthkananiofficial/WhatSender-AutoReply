package com.versionhash.watoolkit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.versionhash.watoolkit.R;
import com.versionhash.watoolkit.activity.adapter.RuleAdapter;
import com.versionhash.watoolkit.activity.customreplyeditor.CustomReplyEditorActivity;
import com.versionhash.watoolkit.model.helpers.RuleHelper;
import com.versionhash.watoolkit.model.preferences.PreferencesManager;
import com.versionhash.watoolkit.model.utils.Constants;

import io.realm.Realm;
import io.realm.RealmChangeListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RulesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RulesFragment extends Fragment {
    Realm realm;
    RealmChangeListener realmChangeListener;
    RuleAdapter ruleAdapter;
    ListView listView;
    FloatingActionButton addRuleFAB;
    CardView timePickerCard;
    TextView timeSelectedTextPreview, timePickerSubTitleTextPreview;
    private PreferencesManager preferencesManager;
    private ImageView imgMinus, imgPlus;
    private int days = 0;

    public RulesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RulesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RulesFragment newInstance(String param1, String param2) {
        RulesFragment fragment = new RulesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        preferencesManager = PreferencesManager.getPreferencesInstance(getActivity());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rules, container, false);
        listView = view.findViewById(R.id.rule_listview);
        timePickerCard = view.findViewById(R.id.timePickerCardView);
        timePickerSubTitleTextPreview = view.findViewById(R.id.timePickerSubTitle);

        timeSelectedTextPreview = view.findViewById(R.id.timeSelectedText);

        imgMinus = view.findViewById(R.id.imgMinus);
        imgPlus = view.findViewById(R.id.imgPlus);
        imgMinus.setOnClickListener(v -> {
            if (days > Constants.MIN_DAYS) {
                days--;
                saveNumDays();
            }
        });

        imgPlus.setOnClickListener(v -> {
            if (days < Constants.MAX_DAYS) {
                days++;
                saveNumDays();
            }
        });
        setNumDays();
        addRuleFAB = view.findViewById(R.id.addRuleFAB);
        addRuleFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CustomReplyEditorActivity.class);
                intent.putExtra("ruleId", "");
                startActivity(intent);
            }
        });
        realm = Realm.getDefaultInstance();
        RuleHelper ruleHelper = new RuleHelper();
        ruleHelper.getAll();
        ruleAdapter = new RuleAdapter(getActivity(), ruleHelper.justRefresh());
        listView.setAdapter(ruleAdapter);
        realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange(Object o) {
                //refresh
                ruleAdapter = new RuleAdapter(getActivity(), ruleHelper.justRefresh());
                listView.setAdapter(ruleAdapter);
            }
        };
        //Add change listener
        realm.addChangeListener(realmChangeListener);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.removeChangeListener(realmChangeListener);
        realm.close();
    }

    private void saveNumDays() {
        preferencesManager.setAutoReplyDelay(days * 24 * 60 * 60 * 1000);//Save in Milliseconds
        setNumDays();
    }

    private void setNumDays() {
        long timeDelay = (preferencesManager.getAutoReplyDelay() / (60 * 1000));//convert back to minutes
        days = (int) timeDelay / (60 * 24);//convert back to days
        if (days == 0) {
            timeSelectedTextPreview.setText("•");
            timePickerSubTitleTextPreview.setText(R.string.time_picker_sub_title_default);
        } else {
            timeSelectedTextPreview.setText("" + days);
            timePickerSubTitleTextPreview.setText(String.format(getResources().getString(R.string.time_picker_sub_title), days));
        }
    }
}