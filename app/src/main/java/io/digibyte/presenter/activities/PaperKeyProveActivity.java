package io.digibyte.presenter.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import java.util.Locale;
import java.util.Random;

import io.digibyte.R;
import io.digibyte.presenter.activities.util.BRActivity;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.animation.BRDialog;
import io.digibyte.tools.animation.SpringAnimator;
import io.digibyte.tools.manager.BRReportsManager;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.security.SmartValidator;
import io.digibyte.tools.util.Bip39Reader;
import io.digibyte.tools.util.Utils;


public class PaperKeyProveActivity extends BRActivity {
    private static final String TAG = PaperKeyProveActivity.class.getName();
    private Button submit;
    private EditText wordEditFirst;
    private EditText wordEditSecond;
    private TextInputLayout wordContainerFirst;
    private TextInputLayout wordContainerSecond;
    private SparseArray<String> sparseArrayWords = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_key_prove);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        submit = findViewById(R.id.button_submit);
        wordEditFirst = findViewById(R.id.word_edittext_first);
        wordContainerFirst = findViewById(R.id.word_container_first);
        wordEditSecond = findViewById(R.id.word_edittext_second);
        wordContainerSecond = findViewById(R.id.word_container_second);
        wordEditFirst.addTextChangedListener(new BRTextWatcher());
        wordEditSecond.addTextChangedListener(new BRTextWatcher());
        wordEditSecond.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_NULL) {
                submit.performClick();
                return true;
            }
            return false;
        });

        submit.setOnClickListener(v -> {
            if (isWordCorrect(true) && isWordCorrect(false)) {
                Utils.hideKeyboard(PaperKeyProveActivity.this);
                BRSharedPrefs.putPhraseWroteDown(PaperKeyProveActivity.this, true);
                BRAnimator.showBreadSignal(PaperKeyProveActivity.this, getString(R.string.Alerts_paperKeySet), getString(R.string.Alerts_paperKeySetSubheader), R.drawable.signal_icon_graphic, () -> {
                    BRAnimator.startBreadActivity(PaperKeyProveActivity.this, false);
                    overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                    finishAffinity();
                });
            } else {
                if (!isWordCorrect(true)) {
                    wordEditFirst.setTextColor(getColor(R.color.red_text));
                    SpringAnimator.failShakeAnimation(PaperKeyProveActivity.this, wordEditFirst);
                }
                if (!isWordCorrect(false)) {
                    wordEditSecond.setTextColor(getColor(R.color.red_text));
                    SpringAnimator.failShakeAnimation(PaperKeyProveActivity.this, wordEditSecond);
                }
            }
        });
        String cleanPhrase = getIntent().getExtras() == null ? null : getIntent().getStringExtra("phrase");
        String wordArray[] = cleanPhrase.split(" ");
        if (wordArray.length == 12 && cleanPhrase.charAt(cleanPhrase.length() - 1) == '\0') {
            BRDialog.showCustomDialog(this, getString(R.string.JailbreakWarnings_title),
                    getString(R.string.Alert_keystore_generic_android), getString(R.string.Button_ok), null, brDialogView -> brDialogView.dismissWithAnimation(), null, null, 0);
            BRReportsManager.reportBug(new IllegalArgumentException("Paper Key error, please contact support at breadwallet.com"), false);
        } else {
            randomWordsSetUp(wordArray);
        }
        new Handler().postDelayed(() -> {
            wordContainerFirst.setHint(String.format(Locale.getDefault(), getString(R.string.ConfirmPaperPhrase_word), (sparseArrayWords.keyAt(0) + 1)));
            wordContainerFirst.bringToFront();
            wordContainerSecond.setHint(String.format(Locale.getDefault(), getString(R.string.ConfirmPaperPhrase_word), (sparseArrayWords.keyAt(1) + 1)));
            wordContainerSecond.bringToFront();
        }, 10);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    private void randomWordsSetUp(String[] words) {
        final Random random = new Random();
        int n = random.nextInt(10) + 1;
        sparseArrayWords.append(n, words[n]);
        while (sparseArrayWords.get(n) != null) {
            n = random.nextInt(10) + 1;
        }
        sparseArrayWords.append(n, words[n]);
    }

    private boolean isWordCorrect(boolean first) {
        if (first) {
            String edit = Bip39Reader.cleanWord(wordEditFirst.getText().toString());
            return SmartValidator.isWordValid(PaperKeyProveActivity.this, edit) && edit.equalsIgnoreCase(sparseArrayWords.get(sparseArrayWords.keyAt(0)));
        } else {
            String edit = Bip39Reader.cleanWord(wordEditSecond.getText().toString());
            return SmartValidator.isWordValid(PaperKeyProveActivity.this, edit) && edit.equalsIgnoreCase(sparseArrayWords.get(sparseArrayWords.keyAt(1)));
        }
    }

    private void validateWord(EditText view) {
        String word = view.getText().toString();
        boolean valid = SmartValidator.isWordValid(this, word);
        view.setTextColor(getColor(valid ? R.color.light_gray : R.color.red_text));
        if (isWordCorrect(true)) {
            view.setTextColor(getColor(R.color.green_text));
        }
    }

    private class BRTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validateWord(wordEditFirst);
            validateWord(wordEditSecond);

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
