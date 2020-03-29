/* Calculating Health Risks for PALACE Health
 *   -by Noah Chong 25/3/2020
 *
 * Algorithm for determining health risk based on a modified version of the one found in:
 * "Estimation of ten-year risk of fatal cardiovascular disease in Europe: the SCORE project"
 *		by R.M. Conroy
 *
 * Values for Health Risk
 * ----------------------
 * Low Risk 		[0, 5)
 * At Risk 			[5, 7)
 * High Risk 		[7, 9)
 * Very High Risk   9+
 * -----------------------
 *
 * As it currently stands, health risks are overstated -- i.e. false positives for health risks are present.
 * Additionally, in general, complete user profiles have a higher likelihood of indicating health risk than guest profiles, due to the additional parameters.
 * With limited information on quantitative health analysis, the algorithm provided is an aggregate of publicly sourced research.
 */

package com.google.firebase.example.fireeats.util;

import com.google.firebase.example.fireeats.model.User;

public class HealthRiskUtil {
    private double weight;
    private double height;
    private double BMI;

    private int chol;
    private int sys_bp;
    private int smoke;
    private double riskRF;

    private int age;
    private double riskAge;

    private double alpha;
    private double p;

    private int activityLevel;
    private boolean histHeartDisease;

    private int riskValue = 0;

    public HealthRiskUtil(){}

    public HealthRiskUtil(User user) {
        if (user.getSex().equals("Male")) {
            alpha = -22.1;
            p = 4.71;
        } else {
            alpha = -29.8;
            p = 6.36;
        }

        chol = user.getCholesterol() == -1 ? 6 : user.getCholesterol();
        sys_bp = user.getBloodPressure() == -1 ? 120 : user.getBloodPressure();
        smoke = user.getSmoke().equals("Yes") ? 1 : 0;

        weight = user.getWeight();
        height = user.getHeight();
        age = user.getAge();

        activityLevel = user.getActivityLevel();
        histHeartDisease = user.getHistoryHeartDisease().equals("Yes") ? true : false;
    }

    // Calculates BMI
    public double calBMI(){
        return weight / Math.pow(height/100, 2);
    }

    // Calculating Risk from Risk Factors
    // Cholesterol (mmol/L), Systolic Blood Pressure (mmHg), and Current Smoker (Y/N)
    // Default values return exp(0) = 1;
    public double calRiskRF(){
        return Math.exp((0.02*(chol-6)) + (0.022*(sys_bp-120)) + (0.63*smoke));
    }

    // Calculating risk from age
    public double calRiskAge(){
        double s10 = Math.exp(-Math.exp(alpha) * Math.pow(age - 10, p));
        double s0 = Math.exp(-Math.exp(alpha) * Math.pow(age - 20, p));

        return 1- (s10/s0);
    }

    // Calculate Total Risk Level
    public int calTotalRisk(){
        BMI = calBMI();
        riskRF = calRiskRF();
        riskAge = calRiskAge();
        riskValue = 0;

        // BMI
        if (BMI < 18.5)
            riskValue += 2;
        else if (BMI >= 18.5 && BMI < 25)
            riskValue += 0;
        else if (BMI > 25 && BMI < 30)
            riskValue += 3;
        else
            riskValue += 5;

        // Age
        if (age >= 20 && age <= 80)
            riskValue += riskAge * 0.1;

        // Risk Factors
        if (riskRF < 1.5)
            riskValue += 0;
        else if (riskRF >= 1.5 && riskRF < 3)
            riskValue += 1;
        else if (riskRF >= 3 && riskRF < 4.5)
            riskValue += 2;
        else
            riskValue += 3;

        // Activity Level
        switch(activityLevel){
            case 3:
                riskValue -= 3;
                break;
            case 2:
                riskValue -= 2;
                break;
            case 1:
                riskValue -= 1;
                break;
            default:
                riskValue += 1;
                break;
        }

        // History of heart disease/ heart attacks
        if (histHeartDisease)
            riskValue += 5;

        return riskValue;
    }
}
