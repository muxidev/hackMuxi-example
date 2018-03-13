package br.com.muxi.bmoreira.mypaymentapplicationexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import br.com.muxi.bmoreira.pwpservices_sdk.PWPServicesSDK.Service.PWPSInitListener;
import br.com.muxi.bmoreira.pwpservices_sdk.PWPServicesSDK.data.PWPSTransaction;
import br.com.muxi.bmoreira.pwpservices_sdk.PWPServicesSDK.Service.PWPSPaymentListener;
import br.com.muxi.bmoreira.pwpservices_sdk.PWPServicesSDK.Service.PWPServicesManager;

import br.com.muxi.bmoreira.pwpservices_sdk.PWPServicesSDK.data.PWPSTransactionResult;
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

            Log.d(TAG, "pwpServicesManager.startService");
            pwpServicesManager.startService(new PWPSInitListener() {
                @Override
                public void onInitSuccess() {
                    initLib = true;
                    Toast.makeText(MainActivity.this, "onInitSuccess", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onInitSuccess");
                }

                @Override
                public void onInitError() {
                    Toast.makeText(MainActivity.this, "onInitError", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onInitError");
                }
            });
        }else{
            Log.e(TAG, "onClickMuxiPay error pwpServicesManager != null");
        }

    }

    private void callPayment(PWPSTransaction pwpsTransaction){

        if(initLib) {
            pwpServicesManager.makePayment(pwpsTransaction, new PWPSPaymentListener() {
                @Override
                public void onPaymentSucess(PWPSTransactionResult transactionStatus) {
                    Log.d(TAG, "onPaymentSucess " + transactionStatus.toString());
                    Toast.makeText(MainActivity.this, "onPaymentSucess", Toast.LENGTH_SHORT).show();

                    result.setText(transactionStatus.getClientReceipt());
                }

                @Override
                public void onPaymentError(PWPSTransactionResult transactionStatus) {
                    Log.d(TAG,"onPaymentError "+ transactionStatus.getStatusMessage());
                    Toast.makeText(MainActivity.this, "onPaymentError", Toast.LENGTH_SHORT).show();

                    result.setText(transactionStatus.getStatusMessage());
                }
            });
        }else{
            Log.e(TAG, "Lib isnt initilized. end call payment");
        }
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
            callPayment(transaction);
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
