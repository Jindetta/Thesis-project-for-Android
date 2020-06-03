package com.piceasoft.thesis_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private DigitClassifier mDigitClassifier;
    private DrawableView mDrawableView;
    private TextView mDataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDigitClassifier = new DigitClassifier(this);

        mDrawableView = findViewById(R.id.image);
        mDataView = findViewById(R.id.data);

        updateText(null);
    }

    @Override
    protected void onStart() {
        mDrawableView.invalidate();
        super.onStart();
    }

    public void buttonClickListener(View view) {
        switch(view.getId()) {
            case R.id.clearButton: {
                mDrawableView.reset();
                updateText(null);

                break;
            }
            case R.id.classifyButton: {
                mDigitClassifier.asyncClassify(mDrawableView.getRenderView())
                                .addOnSuccessListener(this::classificationFinished)
                                .addOnFailureListener(this::classificationFailed);

                break;
            }
        }
    }

    private void classificationFinished(Classification classification) {
        if(!mDrawableView.isEmpty()) {
            updateText(classification);
        }
    }

    private void classificationFailed(@NonNull Exception exception) {
        exception.printStackTrace();
        updateText(null);
    }

    private void updateText(Classification classification) {
        Spanned text;

        if(classification == null) {
            String result_text = getString(R.string.result_not_available);

            text = Html.fromHtml(getString(R.string.result_text, result_text, result_text), Html.FROM_HTML_MODE_LEGACY);
        } else {
            text = Html.fromHtml(getString(R.string.result_text, classification.getConfidence(), classification.getDigit()));
        }

        mDataView.setText(text);
    }
}
