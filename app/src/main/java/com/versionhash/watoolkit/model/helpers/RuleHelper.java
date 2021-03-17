package com.versionhash.watoolkit.model.helpers;


import com.versionhash.watoolkit.model.rules.Rule;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Manages user entered custom auto reply text data.
 */
public class RuleHelper {
    public Boolean saved = false;
    RealmResults<Rule> rules;

    public RuleHelper() {

    }

    public static void deleteById(String ruleId) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                try {
                    RealmResults<Rule> rows = realm.where(Rule.class).equalTo("ruleId", ruleId).findAll();
                    rows.deleteAllFromRealm();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Boolean save(final Rule rule) {
        if (rule == null) {
            saved = false;
        } else {
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    try {
                        Rule r = realm.copyFromRealm(rule);
                        saved = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        saved = false;
                    }
                }
            });
        }
        return saved;
    }

    public void getAll() {
        Realm realm = Realm.getDefaultInstance();
        rules = realm.where(Rule.class).findAll();
    }

    public ArrayList<Rule> justRefresh() {
        ArrayList<Rule> latest_rules = new ArrayList<>();
        for (Rule r : rules) {
            latest_rules.add(r);
        }
        return latest_rules;
    }

    public Rule identifyRule(String msg) {
        //identify the rules basis on the condition
        Realm realm = Realm.getDefaultInstance();
        //first check integration
        RealmResults<Rule> anything_rules = realm.where(Rule.class).equalTo("conditionType", Rule.ANYTHING).findAll();
        for (Rule rule : anything_rules) {
            return rule;
        }
        //first check the exact match
        RealmResults<Rule> exact_rules = realm.where(Rule.class).equalTo("conditionType", Rule.EXACT).findAll();
        for (Rule rule : exact_rules) {
            if (rule.getConditionMsg().toLowerCase().equals(msg.toLowerCase())) {
                return rule;
            }
        }
        //starts with
        RealmResults<Rule> starts_with_rules = realm.where(Rule.class).equalTo("conditionType", Rule.STARTS_WITH).findAll();
        for (Rule rule : starts_with_rules) {
            if (msg.toLowerCase().startsWith(rule.getConditionMsg().toLowerCase())) {
                return rule;
            }
        }
        //Contains check
        RealmResults<Rule> contains_rules = realm.where(Rule.class).equalTo("conditionType", Rule.CONTAINS).findAll();
        for (Rule rule : contains_rules) {
            if (msg.toLowerCase().contains(rule.getConditionMsg().toLowerCase())) {
                return rule;
            }
        }
        //not contains check
        RealmResults<Rule> not_contains_rules = realm.where(Rule.class).equalTo("conditionType", Rule.DOES_NOT_CONTAINS).findAll();
        for (Rule rule : not_contains_rules) {
            if (!msg.toLowerCase().contains(rule.getConditionMsg().toLowerCase())) {
                return rule;
            }
        }
        return null;
    }
}
