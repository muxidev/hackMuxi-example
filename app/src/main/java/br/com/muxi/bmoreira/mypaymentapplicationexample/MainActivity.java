package br.com.muxi.bmoreira.mypaymentapplicationexample;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import muxi.pwps.sdk.data.PWPSCard;
import muxi.pwps.sdk.service.PWPSInitListener;
import muxi.pwps.sdk.data.PWPSTransaction;
import muxi.pwps.sdk.service.PWPSPaymentListener;
import muxi.pwps.sdk.service.PWPServicesManager;

import muxi.pwps.sdk.data.PWPSTransactionResult;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private PWPServicesManager pwpServicesManager;
    private static final String TAG = MainActivity.class.getSimpleName();

    private PWPSTransaction transaction;

    @BindView(R.id.tv_result)
    TextView result;
    @BindView(R.id.value)
    TextView value;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    private boolean initLib = false;

    @OnClick(R.id.btn_muxipay)
    public void onClickMuxiPay(View v) {
        if (pwpServicesManager != null) {

            createProgressBar();
            Log.d(TAG, "pwpServicesManager.startService");
            pwpServicesManager.startService(new PWPSInitListener() {
                @Override
                public void onInitSuccess() {
                    alertDialog.dismiss();
                    initLib = true;
                    Toast.makeText(MainActivity.this, "onInitSuccess", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onInitSuccess");

                }

                @Override
                public void onInitError() {
                    alertDialog.dismiss();
                    Toast.makeText(MainActivity.this, "onInitError", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onInitError");
                }
            });
        }else{
            Log.e(TAG, "onClickMuxiPay error pwpServicesManager != null");
        }

    }

    private void createProgressBar() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(R.layout.dialog);

        alertDialog = alertDialogBuilder.create();

        alertDialog.setCancelable(false);

        Window window = alertDialog.getWindow();
        if(window != null){
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        alertDialog.show();

    }


    PWPSPaymentListener paymentListener = new PWPSPaymentListener() {
        @Override
        public void onPaymentSucess(PWPSTransactionResult transactionStatus) {
            Log.d(TAG, "onPaymentSucess " + transactionStatus.toString());
            Toast.makeText(MainActivity.this, "onPaymentSucess", Toast.LENGTH_SHORT).show();

            result.setText(transactionStatus.getClientReceipt());
        }

        @Override
        public void onPaymentError(PWPSTransactionResult transactionStatus) {
            Log.d(TAG, "onPaymentError " + transactionStatus.getExtraInfo());
            Toast.makeText(MainActivity.this, "onPaymentError", Toast.LENGTH_SHORT).show();

            result.setText(transactionStatus.getExtraInfo());
        }
    };

    private void callPayment(PWPSTransaction pwpsTransaction, boolean pinpad){
        if(initLib) {
            if (pinpad){
                pwpServicesManager.makePayment(pwpsTransaction,paymentListener);
            }else{
                callPaymentWihoutPinpad(pwpsTransaction);
            }
        }else{
            Log.e(TAG, "Lib isnt initilized. end call payment");
        }

    }

    private void callPaymentWihoutPinpad(PWPSTransaction pwpsTransaction){

        PWPSCard pwpsCard = new PWPSCard();
        pwpsCard.setCvv("006");
        pwpsCard.setExpMonth("06");
        pwpsCard.setExpYear("2021");
        pwpsCard.setFirstName("TATIANA");
        pwpsCard.setLastName("ALMEIDA");
        pwpsCard.setNameOnCard("TATIANA L ALMEIDA");
        pwpsCard.setNumber("4220612449662574");


        pwpServicesManager.makePayment(pwpsCard,pwpsTransaction,paymentListener);
    }

    @OnClick(R.id.btn_pay)
    public void onClickPay(View v) {
        // Make your payment!

        int amount;
        String valueS = value.getText().toString();
        if (valueS == null || valueS.equals("")) {
            amount = 1000;
        } else {
            amount = Integer.parseInt(valueS);
        }
        // Fill Transaction Information
        transaction = new PWPSTransaction();
        transaction.setAmount(amount);
        transaction.setCurrency(PWPSTransaction.CurrencyType.BRL);
        transaction.setType(PWPSTransaction.TransactionType.CREDIT);

        if (pwpServicesManager != null) {
            result.setText("");
            callPayment(transaction,true);
        } else {
            result.setText("You should starts the service first!");
            Toast.makeText(this, "You should start the service first!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (pwpServicesManager == null) {
            Log.d(TAG, "onStart  instanciate PWPServicesManager " );
            pwpServicesManager = new PWPServicesManager(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pwpServicesManager != null) {
            pwpServicesManager.bindService();

        }else{
            Log.d(TAG,"onResume pwpServicesManager == null");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "call stopService" );

        if (pwpServicesManager != null) {
            Log.d(TAG, "pwpServicesManager.stopService()" );
            pwpServicesManager.stopService();
        }else{
            Log.d(TAG,"onStop pwpServicesManager == null");
        }
    }

}
