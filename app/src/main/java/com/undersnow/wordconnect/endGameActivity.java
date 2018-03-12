package com.undersnow.wordconnect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import burgerrain.R;


// diplays and writes score to preferences
public class endGameActivity extends AppCompatActivity {
    private TextView scoreTextView;
    private TextView highScoreTextView;
    private int currentUserScore;
    private int highScore = 0;
    private int defaultValue = 0;

    private MainGamePanel gamePanel;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);

        // show ad
        AdView adViewStart = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("291361B6853EA3C74FB32BE3AB84F735").build();
        adViewStart.loadAd(adRequest);
//        MainActivity.refreshAd();
        Bundle bundle = getIntent().getExtras();
        currentUserScore = bundle.getInt(getString(R.string.saved_high_score));
        // print score
        scoreTextView = (TextView) findViewById(R.id.user_score);
        highScoreTextView = (TextView) findViewById(R.id.high_score);

        // set bckgr color
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
            scoreTextView.getRootView().setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
        else
            scoreTextView.getRootView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        // read in
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        highScore = sharedPreferences.getInt(getString(R.string.saved_high_score), defaultValue);

        // if score is greater than high score set it to the new high score
        if (currentUserScore > highScore) {
            // put in score
            SharedPreferences.Editor editor = sharedPreferences.edit();
            MainActivity.instance.updateLeaderboard(currentUserScore);
            editor.putInt(getString(R.string.saved_high_score), currentUserScore);
            editor.commit();
            scoreTextView.setText(getString(R.string.new_high_score_set) + ": " + currentUserScore + "!");
        } else {
            String displayHighScore = getString(R.string.your_high_score) + highScore;
            String displayDotsConnected = getString(R.string.you_connected) + currentUserScore + " " +
                    ((currentUserScore == 1) ? getString(R.string.dot) : getString(R.string.dots));
            highScoreTextView.setText(displayHighScore);
            scoreTextView.setText(displayDotsConnected);
        }
    }

    public void openMainMenu(View view) {
        startActivity(new Intent(endGameActivity.this, MainActivity.class));
    }

    public void tryAgain(View view) {
        gamePanel = new MainGamePanel(this);
        setContentView(gamePanel);
    }
    public void showLeaderboards(View view) {

      MainActivity.instance.showLeaderboards(view);
    }

    @Override
    protected void onStop() {
        if(gamePanel != null)
            gamePanel.shutDownThread();
        super.onStop();
    }

}