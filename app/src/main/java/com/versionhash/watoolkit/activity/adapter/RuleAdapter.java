package com.versionhash.watoolkit.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.versionhash.watoolkit.R;
import com.versionhash.watoolkit.activity.main.MainActivity;
import com.versionhash.watoolkit.model.helpers.RuleHelper;
import com.versionhash.watoolkit.model.preferences.PreferencesManager;
import com.versionhash.watoolkit.model.rules.Rule;

import java.util.ArrayList;

public class RuleAdapter extends BaseAdapter {

    ArrayList<Rule> rules;
    Context context;
    MainActivity mActivity;
    private PreferencesManager preferencesManager;
    public RuleAdapter(@NonNull Context context, ArrayList<Rule> rules) {
        this.context = context;
        this.rules = rules;
    }

    @Override
    public int getCount() {
        return rules.size();
    }

    @Override
    public Object getItem(int position) {
        return rules.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_rule, parent, false);
        }
        TextView expected_msg = convertView.findViewById(R.id.expected_msg_txt);
        TextView reply_msg = convertView.findViewById(R.id.reply_msg_txt);
        TextView ruleId = convertView.findViewById(R.id.hidden_ruleId);

        preferencesManager = PreferencesManager.getPreferencesInstance(context);

        Rule rule = (Rule) this.getItem(position);

        if(rule.getConditionType().equals(Rule.ANYTHING))
        {
            expected_msg.setText("{ Anything }");
            if(preferencesManager.getAnythingType().equals(PreferencesManager.WEBSERVER))
            {
                reply_msg.setText(rule.getReplyMsg());
            }
        }else{
            expected_msg.setText(rule.getConditionMsg());
        }
        if(rule.getConditionType().equals(Rule.ANYTHING) && preferencesManager.getAnythingType().equals(PreferencesManager.WEBSERVER)){
                reply_msg.setText("{ Response of the Server }");
        }else{
            reply_msg.setText(rule.getReplyMsg());
        }

        String str_ruleId = rule.getRuleId();
        ruleId.setText(rule.getRuleId());

        MaterialButton deleteBtn = convertView.findViewById(R.id.delete_button);
        MaterialButton editBtn = convertView.findViewById(R.id.edit_button);

        deleteBtn.setTag(str_ruleId);
        editBtn.setTag(str_ruleId);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete the entry using position
                RuleHelper.deleteById(v.getTag().toString());
            }
        });
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edit the entry using the position
                if (context instanceof MainActivity) {
                    ((MainActivity) context).openCustomReplyEditorActivity(v.getTag().toString());
                }
            }
        });
        return convertView;
    }

}
