/*
 * Copyright 2017 (C) CodePlay Studio. All rights reserved.
 *
 * All source code within this app is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.mike.loancalculator;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.GradientDrawable;
import android.provider.ContactsContract;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class activity_calculator extends AppCompatActivity {

    private EditText etLoadAmount, etDownPayment, etTerm, etAnnualInterestRate;
    private TextView tvMonthlyRepayment, tvTotalRepayment, tvTotalInterest, tvAverageMonthlyInterest;
    private ToggleButton tgBtnCompound;
    private SharedPreferences sharedPreferences;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        etLoadAmount = (EditText)findViewById(R.id.loan_amount);
        etDownPayment = (EditText)findViewById(R.id.down_payment);
        etTerm = (EditText) findViewById(R.id.term);
        etAnnualInterestRate = (EditText)findViewById(R.id.annual_interest_rate);

        tvMonthlyRepayment = (TextView)findViewById(R.id.monthly_repayment);
        tvTotalRepayment = (TextView)findViewById(R.id.total_repayment);
        tvTotalInterest = (TextView)findViewById(R.id.total_interest);
        tvAverageMonthlyInterest = (TextView)findViewById(R.id.average_month_interest);

        tgBtnCompound = (ToggleButton)findViewById(R.id.togleButtonCompound);

        ActionBar ab =getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setIcon(R.drawable.ic_launcher);
        ab.setTitle("  " + ab.getTitle());

        sharedPreferences = getSharedPreferences("loanCalculator", Context.MODE_PRIVATE);
        LoadValue();
    }

    private void StoreValue(){
        SharedPreferences.Editor et = sharedPreferences.edit();
        et.putString("LM", etLoadAmount.getText().toString());
        et.putString("DP", etDownPayment.getText().toString());
        et.putString("TM", etTerm.getText().toString());
        et.putString("IN", etAnnualInterestRate.getText().toString());
        et.putBoolean("CM", tgBtnCompound.isChecked());
        et.putString("MP", tvMonthlyRepayment.getText().toString());
        et.putString("TP", tvTotalRepayment.getText().toString());
        et.putString("TI", tvTotalInterest.getText().toString());
        et.putString("AMI", tvAverageMonthlyInterest.getText().toString());
        et.apply();
    }

    private void LoadValue(){
        etLoadAmount.setText(sharedPreferences.getString("LM", ""));
        etDownPayment.setText(sharedPreferences.getString("DP", ""));
        etTerm.setText(sharedPreferences.getString("TM", ""));
        etAnnualInterestRate.setText(sharedPreferences.getString("IN", ""));
        tgBtnCompound.setChecked(sharedPreferences.getBoolean("CM", false));
        tvMonthlyRepayment.setText(sharedPreferences.getString("MP", getString(R.string.default_result)));
        tvTotalRepayment.setText(sharedPreferences.getString("TP", getString(R.string.default_result)));
        tvTotalInterest.setText(sharedPreferences.getString("TI", getString(R.string.default_result)));
        tvAverageMonthlyInterest.setText(sharedPreferences.getString("AMI", getString(R.string.default_result)));
    }

    private boolean validate(){
        if(etLoadAmount.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter Loan Amount.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(etDownPayment.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter Down Payment Amount.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(etAnnualInterestRate.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter Interest Rate.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(etTerm.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter Term.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void calculate(){
        if(!validate())
            return;

        String amount = etLoadAmount.getText().toString();
        String downPayment = etDownPayment.getText().toString();
        String interestRate = etAnnualInterestRate.getText().toString();
        String term = etTerm.getText().toString();

        double loanAmount = Double.parseDouble(amount) - Double.parseDouble(downPayment);
        double interest = Double.parseDouble(interestRate) / 12 /100;
        double noOfMonth = (Integer.parseInt(term) * 12);

        if(noOfMonth > 0){
            double monthlyRepayment = loanAmount * (interest+(interest/(java.lang.Math.pow((1+interest), noOfMonth)-1)));
            double totalRepayment = monthlyRepayment * noOfMonth;
            double totalInterest = totalRepayment - loanAmount;
            double monthlyInterest = totalInterest / noOfMonth;

            tvMonthlyRepayment.setText(String.format("%.2f", monthlyRepayment));
            tvTotalRepayment.setText(String.format("%.2f", totalRepayment));
            tvTotalInterest.setText(String.format("%.2f", totalInterest));
            tvAverageMonthlyInterest.setText(String.format("%.2f", monthlyInterest));
        }
    }

    private void calculateFixed(){
        if(!validate())
            return;

        String amount = etLoadAmount.getText().toString();
        String downPayment = etDownPayment.getText().toString();
        String interestRate = etAnnualInterestRate.getText().toString();
        String term = etTerm.getText().toString();

        double loanAmount = Double.parseDouble(amount) - Double.parseDouble(downPayment);
        double interest = Double.parseDouble(interestRate) / 12 /100;
        double noOfMonth = (Integer.parseInt(term) * 12);

        if(noOfMonth > 0){
            double totalRepayment = loanAmount * noOfMonth * interest;
            double monthlyRepayment = totalRepayment / noOfMonth;
            double totalInterest = totalRepayment - loanAmount;
            double monthlyInterest = totalInterest / noOfMonth;

            tvMonthlyRepayment.setText(String.format("%.2f", monthlyRepayment));
            tvTotalRepayment.setText(String.format("%.2f", totalRepayment));
            tvTotalInterest.setText(String.format("%.2f", totalInterest));
            tvAverageMonthlyInterest.setText(String.format("%.2f", monthlyInterest));
        }
    }

    private void reset(){
        etLoadAmount.setText("");
        etDownPayment.setText("");
        etTerm.setText("");
        etAnnualInterestRate.setText("");

        tvMonthlyRepayment.setText(R.string.default_result);
        tvTotalRepayment.setText(R.string.default_result);
        tvTotalInterest.setText(R.string.default_result);
        tvAverageMonthlyInterest.setText(R.string.default_result);
    }

    public void OnClick(View v){
        switch (v.getId()){
            case R.id.button_calculate:
                if(tgBtnCompound.isChecked())
                    calculateFixed();
                else
                    calculate();
                StoreValue();

                ContentValues iv = new ContentValues();
                iv.put(Database.COL_LOAN_AMOUNT, etLoadAmount.getText().toString());
                iv.put(Database.COL_DOWN_PAYMENT, etDownPayment.getText().toString());
                iv.put(Database.COL_TERM, etTerm.getText().toString());
                iv.put(Database.COL_ANNUAL_RATE, etAnnualInterestRate.getText().toString());
                iv.put(Database.COL_COMPOUND, tgBtnCompound.getText().toString());
                iv.put(Database.COL_MONTHLY_REPAYMENT, tvMonthlyRepayment.getText().toString());
                iv.put(Database.COL_TOTAL_REPAYMENT, tvTotalRepayment.getText().toString());
                iv.put(Database.COL_TOTAL_INTEREST, tvTotalInterest.getText().toString());
                iv.put(Database.COL_AVERAGE_MONTHLY_INTEREST, tvAverageMonthlyInterest.getText().toString());
                database.InsertResult(iv);
                break;
            case R.id.button_reset:
                reset();
                break;
            case R.id.button_history:
                startActivity(new Intent(activity_calculator.this, activity_history.class));
                break;
        }
    }

    @Override
    protected void onResume() {
        database = new Database(this);
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (database!=null) {
            database.close();
            database = null;
        }
        super.onStop();
    }
}
