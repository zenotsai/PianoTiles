package com.czp.pianotiles;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private CountDownView mCountDownView;
    private PianoTilesView mPianoTilesView;
    private RelativeLayout mMarkRela;
    private AlertScoreDialog mAlertScoreDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }
    private void initView() {
        mPianoTilesView = (PianoTilesView) findViewById(R.id.pianoTilesView);
        mCountDownView = (CountDownView) findViewById(R.id.countTextView);
        mMarkRela = (RelativeLayout) findViewById(R.id.markRela);
        mCountDownView.setData(Arrays.asList("3","2","1","开始"));
        mCountDownView.init();
        mCountDownView.setCountDownListener(new CountDownView.CountDownListener() {
            @Override
            public void finish() {
                mMarkRela.setVisibility(View.GONE);
                mPianoTilesView.setZOrderOnTop(true);
                mPianoTilesView.startGame();
            }
        });

        mAlertScoreDialog = new AlertScoreDialog.Builder(MainActivity.this)
                .setFinishClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("DEMO","点击点击");
                        finish();
                        mAlertScoreDialog.dismiss();
                    }
                })
                .setRestartClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlertScoreDialog.dismiss();
                        mPianoTilesView.restart();
                        mMarkRela.setVisibility(View.VISIBLE);
                        mCountDownView.init();

                    }
                })
                .builder();


        mPianoTilesView.setGameListener(new PianoTilesView.GameListener() {
            @Override
            public void gameEnd(final  String number) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("DEMO","number == "+number);
                        if(mAlertScoreDialog!=null){
                            mAlertScoreDialog.setScore(number);
                            mAlertScoreDialog.show();
                        }

                    }
                });

            }
        });
    }
}
