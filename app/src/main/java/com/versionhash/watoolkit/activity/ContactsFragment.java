package com.versionhash.watoolkit.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.versionhash.watoolkit.R;
import com.versionhash.watoolkit.model.preferences.PreferencesManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    SwitchMaterial groupReplySwitch;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private PreferencesManager preferencesManager;

    public ContactsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactsFragment newInstance(String param1, String param2) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        preferencesManager = PreferencesManager.getPreferencesInstance(getActivity());
        groupReplySwitch = view.findViewById(R.id.groupReplySwitch);
        // Enable group chat switch only if main switch id ON
        groupReplySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Ignore if this is not triggered by user action but just UI update in onResume() #62
            if (preferencesManager.isGroupReplyEnabled() == isChecked) {
                return;
            }

            if (isChecked) {
                Toast.makeText(getActivity(), R.string.group_reply_on_info_message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), R.string.group_reply_off_info_message, Toast.LENGTH_SHORT).show();
            }
            preferencesManager.setGroupReplyPref(isChecked);
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //If user directly goes to Settings and removes notifications permission
        //when app is launched check for permission and set appropriate app state
        // set group chat switch state
        groupReplySwitch.setChecked(preferencesManager.isGroupReplyEnabled());
    }
}