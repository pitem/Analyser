package com.yhh.analyser.model;

/**
 * Created by yuanhh1 on 2015/8/25.
 */
public class AppLaunchBean {
    public String getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(String completeTime) {
        this.completeTime = completeTime;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getSpendTime() {
        return spendTime;
    }

    public void setSpendTime(String spendTime) {
        this.spendTime = spendTime;
    }

    private String completeTime;
    private String activityName;
    private String spendTime;


    public boolean isEqual(AppLaunchBean bean){
        return getCompleteTime().equals(bean.getCompleteTime());
    }

    @Override
    public String toString() {
        return completeTime + " "+ activityName + " " + spendTime;
    }
}
