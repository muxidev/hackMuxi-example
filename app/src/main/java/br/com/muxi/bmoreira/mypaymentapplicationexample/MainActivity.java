package br.com.muxi.bmoreira.mypaymentapplicationexample;

import android.animation.Animator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import br.com.muxi.bmoreira.pwpservices_sdk.PWPServicesSDK.Service.PWPSCancellationListener;
import br.com.muxi.bmoreira.pwpservices_sdk.PWPServicesSDK.data.PWPSTransaction;
import br.com.muxi.bmoreira.pwpservices_sdk.PWPServicesSDK.Service.PWPSPaymentListener;
import br.com.muxi.bmoreira.pwpservices_sdk.PWPServicesSDK.Service.PWPServicesManager;

import br.com.muxi.bmoreira.pwpservices_sdk.PWPServicesSDK.data.PWPSTransactionStatus;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements PWPSPaymentListener, PWPSCancellationListener {

    private PWPServicesManager pwpServicesManager;

    private PWPSTransaction transaction;

    @BindView(R.id.tv_result)
    TextView result;
    @BindView(R.id.tv_notification)
    TextView notification;
    @BindView(R.id.value)
    TextView value;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    private View buttonView;

    @OnClick(R.id.btn_transactStatus)
    public void onClickTransactStatus(View v) {

        buttonView = v;

        final View dialogView = View.inflate(this, R.layout.dialog_status, null);
        updateDialogViewData(dialogView);

        final Dialog dialog = new Dialog(this, R.style.Theme_AppCompat_Dialog_Alert);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                revealShow(buttonView, dialogView);
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }

    private void updateDialogViewData(View dialogView) {
        if (currentStatus != null) {
            TextView tvDialogTranState = dialogView.findViewById(R.id.dialog_tran_state);
            tvDialogTranState.setText(getString(R.string.dialog_transactionstate)
                    + " "
                    + currentStatus.getTransactionState().name());

            TextView tvDialogTranId = dialogView.findViewById(R.id.dialog_tran_id);
            tvDialogTranId.setText(getString(R.string.dialog_transaction_id)
                    + " "
                    + currentStatus.getNsu());

            TextView tvDialogTranAutNum = dialogView.findViewById(R.id.dialog_tran_aut_num);
            tvDialogTranAutNum.setText(getString(R.string.dialog_transaction_authorization_number)
                    + " "
                    + currentStatus.getAutNum());

            TextView tvDialogTranDescriptionMsg = dialogView.findViewById(R.id.dialog_tran_description_msg);
            tvDialogTranDescriptionMsg.setText(getString(R.string.dialog_description_message)
                    + " "
                    + currentStatus.getDescriptionMsg());

            TextView tvDialogTranFinishedStatus = dialogView.findViewById(R.id.dialog_tran_finished_status);
            tvDialogTranFinishedStatus.setText(getString(R.string.dialog_transaction_finished_status)
                    + " "
                    + currentStatus.getTransactionFinishedStatus().name());

            TextView tvDialogTranMissingData = dialogView.findViewById(R.id.dialog_tran_missing_data);
            tvDialogTranMissingData.setText(getString(R.string.dialog_missing_data)
                    + " "
                    + currentStatus.getMissingData());

            TextView tvDialogTranReceipt = dialogView.findViewById(R.id.dialog_tran_receipt);
            tvDialogTranReceipt.setText(getString(R.string.dialog_receipt_text)
                    + " "
                    + currentStatus.getReceiptTxt());
        }
    }

    private void revealShow(View v, View dialogView) {
        int dialogViewWidth = dialogView.getWidth();
        int dialogViewHeight = dialogView.getHeight();
        int[] dialogAbsoluteLocation = new int[2];
        dialogView.getLocationOnScreen(dialogAbsoluteLocation);

        int[] btnAbsoluteLocation = new int[2];
        v.getLocationOnScreen(btnAbsoluteLocation);
        int btnCenterX = btnAbsoluteLocation[0] + (v.getWidth() / 2);
        int btnCenterY = btnAbsoluteLocation[1] + (v.getHeight() / 2);

        int startAnimationX = btnCenterX - dialogAbsoluteLocation[0];
        int startAnimationY = btnCenterY - dialogAbsoluteLocation[1];
        int endRadius = (int) Math.hypot(dialogViewWidth, dialogViewHeight);

        Animator revealAnimator = ViewAnimationUtils.createCircularReveal(dialogView,
                startAnimationX,
                startAnimationY,
                0,
                endRadius);
        revealAnimator.setInterpolator(new FastOutSlowInInterpolator());
        revealAnimator.setDuration(375); //Following material design guide... aprox 375ms for big animations
        revealAnimator.start();
    }

    @OnClick(R.id.btn_muxipay)
    public void onClickMuxiPay(View v) {
        if (pwpServicesManager != null) {
            pwpServicesManager.stopService();
        }

        pwpServicesManager.startService();
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
            notification.setText("");
            pwpServicesManager.makePayment(transaction, this);
        } else {
            result.setText("");
            notification.setText("You should start the service first!");
            Toast.makeText(this, "You should start the service first!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (pwpServicesManager == null) {
            pwpServicesManager = new PWPServicesManager(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (pwpServicesManager != null) {
            pwpServicesManager.stopService();
        }
    }

    private PWPSTransactionStatus currentStatus;

    @Override
    public void onPayment(PWPSTransactionStatus transactionStatus) {
        currentStatus = transactionStatus;
        result.setText(transactionStatus.toString());
    }

    @Override
    public void onCancelling(PWPSTransactionStatus transactionStatus) {
        currentStatus = transactionStatus;
        result.setText(transactionStatus.toString());
    }

    @Override
    public void onNotify(String message) {
        notification.setText(message);
    }

    @Override
    public void onCancelOperation(String message) {
        notification.setText(message);
        Toast.makeText(this, "Operation Canceled!", Toast.LENGTH_SHORT).show();
    }


}
