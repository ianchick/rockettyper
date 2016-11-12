package com.example.ian.rockettyper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

import org.w3c.dom.Text;

import static android.R.attr.max;
import static android.R.id.edit;
import static android.R.id.input;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.ian.rockettyper.R.array.sentences;
import static com.example.ian.rockettyper.R.id.start_button;
import static com.example.ian.rockettyper.R.string.settings;

public class MainActivity extends AppCompatActivity {
    private final long START_TIME = 60000;
    private final long INTERVAL = 1000;

    private TextView timer;
    private EditText editText;
    private TextView textView;

    private String[] sentences;
    private String activeWord;
    private String activeSentence;
    private String[] wordArray;

    private StringBuilder correctWordsList;

    private int wordIndex;
    private int wordCount;

    //Timer
    private CountDownTimer countDownTimer = new CountDownTimer(START_TIME,INTERVAL) {
        @Override
        public void onTick(long millisUntilFinished) {
            timer.setText("Seconds Left: " + millisUntilFinished / INTERVAL);

            if(millisUntilFinished<=10000){
                timer.setTextColor(Color.parseColor("#ff0000"));
            }
        }
        @Override
        public void onFinish() {
            timer.setTextColor(Color.parseColor("#ffffff"));

            timer.setText("Done!");
            editText.removeTextChangedListener(inputWatcher);
            textView.setText("Word Count: " + wordCount + "\n" + wordCount/(START_TIME/60000) + " WPM");

            findViewById(R.id.stop_button).setVisibility(GONE);
            findViewById(R.id.start_button).setVisibility(VISIBLE);
            editText.setVisibility(GONE);
            findViewById(R.id.best_score).setVisibility(VISIBLE);
            editText.setText("");

            TextView scoreView = (TextView)findViewById(R.id.best_score);
            setBestScore((int)(wordCount/(START_TIME/60000)), scoreView);

            hideSoftKeyboard(editText, MainActivity.this);
        }
    };
    private int sentenceIndex;
    //Input text watcher
    private final TextWatcher inputWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        @Override
        public void afterTextChanged(Editable s) {

            if(isMatchText(s.toString(),activeWord)){

                correctWordsList.append(activeWord);
                correctWordsList.append(" ");
                StringBuilder remainingWords = new StringBuilder();
                for(int i=wordIndex+1;i<wordArray.length;i++){
                    remainingWords.append(wordArray[i]);
                    remainingWords.append(" ");
                }
                String first = "<font color='#32CD32'>"+correctWordsList.toString()+"</font>";
                String second = remainingWords.toString();
                textView.setText(Html.fromHtml(first + second),TextView.BufferType.SPANNABLE);

                if (activeWord.contains(".")) {
                    editText.setText("");
                    setActiveSentence();
                    correctWordsList = new StringBuilder();
                    textView.setText(activeSentence);
                } else {
                    wordIndex++;
                    editText.setText("");
                }
                wordCount++;
            }
            activeWord = wordArray[wordIndex];
        }
    };

    //Initialized MainActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText)findViewById(R.id.text_input);
        textView = (TextView)findViewById(R.id.active_sentence);

        correctWordsList = new StringBuilder();

        findViewById(R.id.start_button).setVisibility(VISIBLE);
        findViewById(R.id.stop_button).setVisibility(GONE);
        editText.setVisibility(GONE);
        editText.setText("");

        Button settings = (Button)findViewById(R.id.settings_button);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Setting.class));
            }
        });
    }

    //Starts the clock, and sets up first sentence by random.
    public void onStartClick(View view){
        editText.addTextChangedListener(inputWatcher);

        //Setting first sentence.
        sentences = getResources().getStringArray(R.array.sentences);
        setActiveSentence();
        textView.setText(activeSentence);

        wordCount = 0;

        timer = (TextView)findViewById(R.id.timer);
        countDownTimer.start();

        editText.setVisibility(VISIBLE);
        findViewById(R.id.stop_button).setVisibility(VISIBLE);
        findViewById(R.id.start_button).setVisibility(GONE);
        findViewById(R.id.best_score).setVisibility(GONE);

        showKeyboard(editText, this);

        correctWordsList = new StringBuilder();
    }


    //Stop the clock while it is still running
    public void onStopClick(View view){
        countDownTimer.cancel();
        editText.removeTextChangedListener(inputWatcher);
        findViewById(R.id.start_button).setVisibility(VISIBLE);
        findViewById(R.id.stop_button).setVisibility(GONE);
        editText.setVisibility(GONE);
        textView.setText("");
        editText.setText("");
        timer.setText("Time: 1 Minute");

        hideSoftKeyboard(editText, this);
    }


    //Helper for checking if strings match with string and added space.
    private boolean isMatchText(String first, String second){
        if(first.equals(second+" ")){
            return true;
        }
        return false;
    }

    //Splitting sentence String into String array of individual words.
    private String[] splitSentence(String s){
        return s.split(" ");
    }

    //Picks random number between max and min.
    private int randomNum(int max, int min){
        Random rand = new Random();
        int randomNum = rand.nextInt((max-min + 1)+min);
        return randomNum;
    }

    //Sets new random sentence to activeSentence and puts index back to 0.
    private void setActiveSentence(){
        sentenceIndex = randomNum(sentences.length-1,0);
        wordIndex = 0;
        activeSentence = sentences[sentenceIndex];
        wordArray = splitSentence(activeSentence);
        activeWord = wordArray[wordIndex];
    }

    private void setBestScore(int score, TextView view){
        //getting preferences
        SharedPreferences prefs = this.getSharedPreferences("rocketTyperScore", Context.MODE_PRIVATE);
        int bestScore = prefs.getInt("bestScore", 0); //0 is the default value

        if(score > bestScore){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("bestScore", score);
            editor.commit();
            view.setText("Congrats! New Best Score!!\n" + prefs.getInt("bestScore",0));
        } else {
            view.setText("Best Score: " + prefs.getInt("bestScore", 0));
        }
    }

    private void showKeyboard(EditText mEtSearch, Context context) {
        mEtSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    private void hideSoftKeyboard(EditText editText, Context context) {
        editText.clearFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}
