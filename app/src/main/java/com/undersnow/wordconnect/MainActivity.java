package com.undersnow.wordconnect;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

import burgerrain.R;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final Map<Character, Integer> drawables = new HashMap<>();
    public static MainActivity instance =null;
    private TextView displayHighScore = null; // Textview that displays current highscore
    private MainGamePanel gamePanel = null; // the game engine
    private int highScore = 0; // actual highscore number
    private ImageView volume = null; // Image of the volume, mute when pressed

    private SharedPreferences sharedPreferences = null; // preferences

    private static InterstitialAd mInterstitialAd;
    private static AdRequest  adViewStart, adInterRequest;

    public String PACKAGE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        instance=this;
        // ads
initDrawables();
        AdView adViewStart = (AdView) findViewById(R.id.adViewStart);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("291361B6853EA3C74FB32BE3AB84F735") .build();
        adViewStart.loadAd(adRequest);

        mInterstitialAd = newInterstitialAd();
        adInterRequest = new AdRequest.Builder().addTestDevice("291361B6853EA3C74FB32BE3AB84F735")
                .setRequestAgent("android_studio:ad_template").build();
        refreshAd();

        // initialize view variables
        displayHighScore = (TextView) findViewById(R.id.start_screen_high_score);
        volume = (ImageView) findViewById(R.id.volume);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        highScore = sharedPreferences.getInt(getString(R.string.saved_high_score), 0);

        String displayHighScoreString = getString(R.string.your_high_score) + highScore;
        displayHighScore.setText(displayHighScoreString);

        if (!sharedPreferences.getBoolean("sound", true))
            volume.setImageResource(R.drawable.volumemute);

        setBackgroundColor(R.color.colorPrimary);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // load interstitial ad


        PACKAGE_NAME = getApplicationContext().getPackageName();
      //  signIn();
    }

    private void initDrawables() {
        for (int i = 'a'; i<='z'; i++){
            drawables.put((char)i,   this.getResources().getIdentifier("alpha_"+((char)i), "drawable", this.getPackageName()));
            drawables.put((char)(i-'a'+'A'),   this.getResources().getIdentifier("alphas_"+((char)i), "drawable", this.getPackageName()));
        }
    }

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.inter));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {

            }

            @Override
            public void onAdClosed() {
                gamePanel = new MainGamePanel(MainActivity.this);
                setContentView(gamePanel);

            }
        });
        return interstitialAd;
    }
    private void setBackgroundColor(int color) {
        // set bckgr color
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
            displayHighScore.getRootView().setBackgroundColor(getResources().getColor(color, null));
        else
            displayHighScore.getRootView().setBackgroundColor(getResources().getColor(color));
    }

    // called when volume button is clicked
    public void changeVolumeState(View v) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // change audio preference
        if (sharedPreferences.getBoolean("sound", true)) {
            editor.putBoolean("sound", false);
            volume.setImageResource(R.drawable.volumemute);
        } else {
            editor.putBoolean("sound", true);
            volume.setImageResource(R.drawable.volume);
        }
        editor.commit();
    }

    public void openTutorial(View v) {
       startActivity(new Intent(MainActivity.this, TutorialActivity.class));
    }

    public void startGame(View view) {
        if(mInterstitialAd!=null && mInterstitialAd.isLoaded()){
          //  mInterstitialAd.show();
            gamePanel = new MainGamePanel(this);
            setContentView(gamePanel);
        }else {
            gamePanel = new MainGamePanel(this);
            setContentView(gamePanel);
        }

    }

    

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if (gamePanel != null)
            gamePanel.shutDownThread();
        super.onStop();
    }

    public static boolean isLoaded(InterstitialAd interstitialAd) {
        return interstitialAd.isLoaded();
    }

    public static InterstitialAd getInterstitialAd() {
        return mInterstitialAd;
    }

    public static InterstitialAd refreshAd() {
        mInterstitialAd.loadAd(adInterRequest);
        return mInterstitialAd;
    }













    public void  shareApp(View v){

        Intent sharingIntent = new Intent(
                android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_string, 10));
        startActivity(sharingIntent);

    }






// Leaderboard part
    private LeaderboardsClient mLeaderboardsClient;

    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;




    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }
    private void signInSilently() {
        if(isSignedIn()) return  ;
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            // The signed in account is stored in the task's result.
                            GoogleSignInAccount signedInAccount = task.getResult();

                        } else {
                            onDisconnect();

                        }
                    }
                });
    }
    private void signIn() {
        if(isSignedIn()) return  ;
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            // The signed in account is stored in the task's result.
                            GoogleSignInAccount signedInAccount = task.getResult();

                        } else {
                            startSignInIntent();

                        }
                    }
                });
    }
    private void startSignInIntent() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    // The signed in account is stored in the result.
                    GoogleSignInAccount signedInAccount = result.getSignInAccount();

                } else {
                    String message = result.getStatus().getStatusMessage();
                    if (message == null || message.isEmpty()) {
                        message = "Sign in error";
                        onDisconnect();
                    }
               /* new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();*/
                }
            }
        }catch (Exception e){
e.printStackTrace();
        }

    }






    private void onDisconnect() {
        Log.d(TAG, "onDisconnected()");

        mLeaderboardsClient = null;

    }



    public void showLeaderboards(View v) {
        Log.e("showLeaderboards" , "showLeaderboards");
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(googleSignInAccount==null) return;
        mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
        mLeaderboardsClient.getLeaderboardIntent(getString(R.string.leaderboard_burgerrain))
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_UNUSED);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleException(e, "leaderboards_exception");
                    }
                });
    }

    private void handleException(Exception e, String details) {
        int status = 0;

        if (e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            status = apiException.getStatusCode();
        }

        String message = "status_exception_error";

       /* new AlertDialog.Builder(Luncher.this)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, null)
                .show();*/
    }

    public int getScore() {
        //Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this)).getLeaderboardIntent(getString(R.string.leaderboard_burgerrain)).
        return 0;
    }

    public void updateLeaderboard(int score) {
        if(!isSignedIn()) return;
        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .submitScore(getString(R.string.leaderboard_burgerrain), score);
    }


    public void rateApp(View v)
    {
        try
        {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        }
        catch (ActivityNotFoundException e)
        {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }

    private Intent rateIntentForUrl(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21)
        {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        }
        else
        {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }


}
