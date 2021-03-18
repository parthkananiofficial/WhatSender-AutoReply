package com.versionhash.watoolkit.model.rules;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Rule extends RealmObject {
    public static final String EXACT = "exact";
    public static final String STARTS_WITH = "startwith";
    public static final String CONTAINS = "contains";
    public static final String DOES_NOT_CONTAINS = "notcontains";
    public static final String ANYTHING = "anything";

    public static final String STATIC = "static";
    public static final String WEBSERVER = "webserver";
    private String conditionMsg;
    private String replyMsg;
    private String incomingMsgCondition;

    public String getResponseMsgSourceCondition() {
        return responseMsgSourceCondition;
    }

    public void setResponseMsgSourceCondition(String responseMsgSourceCondition) {
        this.responseMsgSourceCondition = responseMsgSourceCondition;
    }

    private String responseMsgSourceCondition;
    @PrimaryKey
    @Required
    private String ruleId;

    public Rule() {
    }

    public Rule(String conditionMsg, String replyMsg, String ruleId) {
        this.conditionMsg = conditionMsg;
        this.replyMsg = replyMsg;
        this.ruleId = ruleId;
    }

    public String getIncomingMsgCondition() {
        return incomingMsgCondition;
    }

    public void setIncomingMsgCondition(String incomingMsgCondition) {
        this.incomingMsgCondition = incomingMsgCondition;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "conditionMsg='" + conditionMsg + '\'' +
                ", replyMsg='" + replyMsg + '\'' +
                ", conditionType='" + incomingMsgCondition + '\'' +
                ", ruleId='" + ruleId + '\'' +
                '}';
    }

    public String getConditionMsg() {
        return conditionMsg;
    }

    public void setConditionMsg(String conditionMsg) {
        this.conditionMsg = conditionMsg;
    }

    public String getReplyMsg() {
        return replyMsg;
    }

    public void setReplyMsg(String replyMsg) {
        this.replyMsg = replyMsg;
    }

}
