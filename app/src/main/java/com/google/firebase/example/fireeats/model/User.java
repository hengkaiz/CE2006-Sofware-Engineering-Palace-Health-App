package com.google.firebase.example.fireeats.model;

public class User {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_AGE = "age";
    public static final String FIELD_BP = "blood pressure";
    public static final String FIELD_CHOLESTEROL = "cholesterol";
    public static final String FIELD_DIABETIC = "diabetic";
    public static final String FIELD_HEIGHT = "height";
    public static final String FIELD_SEX = "sex";
    public static final String FIELD_TREATEDHBP = "treated for hbp";
    public static final String FIELD_WEIGHT = "weight";
    public static final String FIELD_SMOKE = "smoker";
    public static final String FIELD_ACTIVITYLEVEL = "activity level";
    public static final String FIELD_HISTORYHEARTDISEASE = "history heart disease";

    private String name;
    private int age;
    private int bloodPressure;
    private int cholesterol;
    private String diabetic;
    private int height;
    private String sex;
    private String treatedHBP;
    private int weight;
    private String smoke;
    private int activityLevel;
    private String historyHeartDisease;

    private double bmi;
    private double riskRF;
    private double riskAge;
    private int totalRisk;

    public User() {
    }

    public User(String name, int age, int bloodPressure, int cholesterol, String diabetic, int height, String sex, String treatedHBP, int weight, String smoke, int activityLevel, String historyHeartDisease, double bmi, double riskRF, double riskAge, int totalRisk) {
        this.name = name;
        this.age = age;
        this.bloodPressure = bloodPressure;
        this.cholesterol = cholesterol;
        this.diabetic = diabetic;
        this.height = height;
        this.sex = sex;
        this.treatedHBP = treatedHBP;
        this.weight = weight;
        this.smoke = smoke;
        this.activityLevel = activityLevel;
        this.historyHeartDisease = historyHeartDisease;
        this.bmi = bmi;
        this.riskRF = riskRF;
        this.riskAge = riskAge;
        this.totalRisk = totalRisk;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(int bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public int getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(int cholesterol) {
        this.cholesterol = cholesterol;
    }

    public String getDiabetic() {
        return diabetic;
    }

    public void setDiabetic(String diabetic) {
        this.diabetic = diabetic;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getTreatedHBP() {
        return treatedHBP;
    }

    public void setTreatedHBP(String treatedHBP) {
        this.treatedHBP = treatedHBP;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getSmoke() {
        return smoke;
    }

    public void setSmoke(String smoke) {
        this.smoke = smoke;
    }

    public int getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(int activityLevel) {
        this.activityLevel = activityLevel;
    }

    public String getHistoryHeartDisease() {
        return historyHeartDisease;
    }

    public void setHistoryHeartDisease(String historyHeartDisease) {
        this.historyHeartDisease = historyHeartDisease;
    }

    public double getBmi() {
        return bmi;
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }

    public double getRiskRF() {
        return riskRF;
    }

    public void setRiskRF(double riskRF) {
        this.riskRF = riskRF;
    }

    public double getRiskAge() {
        return riskAge;
    }

    public void setRiskAge(double riskAge) {
        this.riskAge = riskAge;
    }

    public int getTotalRisk() {
        return totalRisk;
    }

    public void setTotalRisk(int totalRisk) {
        this.totalRisk = totalRisk;
    }
}

