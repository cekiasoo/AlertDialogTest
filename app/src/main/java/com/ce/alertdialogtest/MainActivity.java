package com.ce.alertdialogtest;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private AppCompatButton mBtnShowDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnShowDialog = (AppCompatButton)findViewById(R.id.btn_show_dialog);
        View viewDialog = getLayoutInflater().inflate(R.layout.view_dialog, null);
        final AppCompatEditText etInput = (AppCompatEditText)viewDialog.findViewById(R.id.et_input);
        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("我是标题啊")
                .setView(viewDialog)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String text = etInput.getText().toString().trim();
                        if (!TextUtils.isEmpty(text)) {
                            Toast.makeText(MainActivity.this,
                                    "" + text, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "你还真随便", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        mBtnShowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
    }
}
