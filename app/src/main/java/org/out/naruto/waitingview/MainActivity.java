package org.out.naruto.waitingview;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import org.out.naruto.view.WaitingView;

public class MainActivity extends AppCompatActivity {

    private WaitingView waitingView;
    private Button button;
    private AlertDialog.Builder dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        waitingView = (WaitingView) findViewById(R.id.id_main_waitingview);
        button = (Button) findViewById(R.id.id_main_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDialog();
//                waitingView.startAnim();
            }
        });

    }

    private void ShowDialog() {
        LayoutInflater layoutInflater = getLayoutInflater();
        RelativeLayout view = (RelativeLayout) layoutInflater.inflate(R.layout.dialog, null);

        WaitingView test = new WaitingView(MainActivity.this);
        view.addView(test);
        dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setView(view);
        dialog.show();
    }

}
