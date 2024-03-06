package com.blk.sdk.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.TransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.blk.sdk.UI;
import com.blk.sdk.string;
import com.blk.sdk.Utility;
import com.blk.sdk.TickTimer;
import com.blk.sdk.c;
import com.blk.sdk.R;

public class ActivityPosPed extends BaseActivity implements TickTimer.TickTimerListener {

    public enum INPUT_STYLE {AMOUNT, NUMBER, PAN, EXPDATE}

    private CustomEditText edtAmount;
    private SoftKeyboardPosStyle softKeyboard;
    private SoftKeyboardPasswordStyle softKeyboardPassword;
    private FrameLayout flkeyBoardContainer;
    private int min;
    private boolean fAcceptZero;
    private TickTimer timer;

    public INPUT_STYLE style = INPUT_STYLE.AMOUNT;
    public int endStatus = UI.NONE;
    public String value;

    void TimerReset(boolean fRestart) {
        if (timer != null) {
            timer.cancel();
            if (fRestart) timer.start();
        }
    }

    static class AmountTextWatcher implements TextWatcher {

        private boolean mEditing;
        private String strPre = "";
        private final int MAX_DIGITS = 9;
        private final ActivityPosPed activity;

        public AmountTextWatcher(ActivityPosPed activity) {
            mEditing = false;
            this.activity = activity;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!mEditing) {
                mEditing = true;
                String digits = s.toString().replace(".", "").trim().replaceAll("[^(0-9)]", "");
                String str = "";
                if (digits.length() > MAX_DIGITS) {
                    str = strPre;
                } else {
                    if (digits == null || digits.length() == 0)
                        str = "0.00";
                    else {
                        str = String.format("%d.%02d", Long.parseLong(digits) / 100, Long.parseLong(digits) % 100);
                    }
                }
                try {
                    s.replace(0, s.length(), str);
                    strPre = str;
                } catch (NumberFormatException nfe) {
                    s.clear();
                }
                mEditing = false;
                activity.TimerReset(true);
            }
        }
    }

    static class PanTextWatcher implements TextWatcher {

        private boolean mEditing;
        private String strPre = "";
        private final int MAX_DIGITS = 19;
        private final ActivityPosPed activity;

        public PanTextWatcher(ActivityPosPed activity) {
            mEditing = false;
            this.activity = activity;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!mEditing) {
                mEditing = true;
                String digits = s.toString().replace(" ", "").trim().replaceAll("[^(0-9)]", "");
                String str = "";
                boolean fDel = string.DeleteChar(strPre, ' ').length() >= digits.length();

                Utility.log("digits(" + digits + ") strPre(" + strPre + ") fDel(" + fDel + ")");

                if (digits.length() > MAX_DIGITS) {
                    str = strPre;
                } else {
                    if (digits == null || digits.length() == 0)
                        str = "";
                    else {
                        for (int i = 0; i < digits.length(); ++i) {
                            str += digits.charAt(i);
                            if (i % 4 == 3) str += " ";
                        }
                        if (fDel && strPre.endsWith(" ") && strPre.equals(str) && str.length() > 4)
                            str = str.substring(0, str.length() - 2);
                    }
                }

                s.replace(0, s.length(), str);
                strPre = str;
                mEditing = false;
                activity.TimerReset(true);
            }
        }
    }

    static class ExpDateTextWatcher implements TextWatcher {

        private boolean mEditing = false;
        private String strPre = "";
        private final int MAX_DIGITS = 4;
        private final ActivityPosPed activity;

        public ExpDateTextWatcher(ActivityPosPed activity) {
            this.activity = activity;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!mEditing) {
                mEditing = true;
                String digits = s.toString().replace(" ", "").trim().replaceAll("[^(0-9)]", "");
                String str = "";
                boolean fDel = string.DeleteChar(strPre, '/').length() >= digits.length();
                String base = "__/__";

                Utility.log("digits(" + digits + ") strPre(" + strPre + ") fDel(" + fDel + ")");

                if (digits.length() > MAX_DIGITS) {
                    str = strPre;
                } else {
                    while (true) {
                        if (digits == null || digits.length() == 0) str = "";
                        else {

                            if ((digits.length() >= 1 && c.atoi(digits.substring(0, 1).getBytes()) > 1) ||
                                    (digits.length() >= 2 && (c.atoi(digits.substring(0, 2).getBytes()) == 0 || c.atoi(digits.substring(0, 2).getBytes()) > 12))) {
                                str = strPre;
                                break;
                            }

                            for (int i = 0; i < digits.length(); ++i) {
                                str += digits.charAt(i);
                                if (i == 1) str += "/";
                            }
                            str += base.substring(str.length(), base.length());

                            if (fDel && strPre.equals(str) && str.length() > 0 && digits.length() > 0) {
                                digits = digits.substring(0, digits.length() - 1);
                                str = "";
                                continue;
                            }
                        }
                        break;
                    }
                }

                s.replace(0, s.length(), str);
                strPre = str;
                mEditing = false;
                activity.TimerReset(true);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        initView(); //emot
        setListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TimerReset(false);
        if (endStatus == UI.NONE) { // == UI.NONE) {
            endStatus = UI.TIMEOUT;
        }
    }

    @Override
    public void onTickTimerFinish() {

        TimerReset(false);
        endStatus = UI.TIMEOUT;
        Utility.log("onTickTimerFinish");
        finish();
    }

    @Override
    public void onTick(long leftTime) {
    }

    private class HiddenPassTransformationMethod implements TransformationMethod {

        private char DOT = '*';

        @Override
        public CharSequence getTransformation(final CharSequence charSequence, final View view) {
            return new PassCharSequence(charSequence);
        }

        @Override
        public void onFocusChanged(final View view, final CharSequence charSequence, final boolean b, final int i,
                                   final Rect rect) {
            //nothing to do here
        }

        private class PassCharSequence implements CharSequence {

            private final CharSequence charSequence;

            public PassCharSequence(final CharSequence charSequence) {
                this.charSequence = charSequence;
            }

            @Override
            public char charAt(final int index) {
                return DOT;
            }

            @Override
            public int length() {
                return charSequence.length();
            }

            @Override
            public CharSequence subSequence(final int start, final int end) {
                return new PassCharSequence(charSequence.subSequence(start, end));
            }
        }
    }

    TextView header;

    private void initView() {
        Intent i = getIntent();
        String h = i.getStringExtra("header");
        int max = i.getIntExtra("max", 10);
        min = i.getIntExtra("min", 1);
        fAcceptZero = i.getBooleanExtra("fAcceptZero", false);
        String initial = i.getStringExtra("initial");
        int timeout = i.getIntExtra("timeout", 30);
        boolean fMask = i.getBooleanExtra("fMask", false);
        style = (INPUT_STYLE) i.getSerializableExtra("style");
        boolean isBankRef=i.getBooleanExtra("isBankRef",false);

        header = (TextView) findViewById(R.id.header);
        header.setGravity(Gravity.CENTER);
        header.setText(h);

        edtAmount = (CustomEditText) findViewById(R.id.amount_edtext);
        edtAmount.setHint(style == INPUT_STYLE.AMOUNT ? "0.00" : (style == INPUT_STYLE.PAN ? "" : "0"));
        edtAmount.setInputType(InputType.TYPE_NULL);
        edtAmount.setIMEEnabled(false, true);
        edtAmount.setFilters(new InputFilter[]{new InputFilter.LengthFilter(max)});

        if (isBankRef)
            edtAmount.setTextSize(35f);

        if (initial != null && initial.length() > 0) {
            edtAmount.setText(initial);
            edtAmount.setSelection(edtAmount.getText().length());
        }
        if (fMask) {
            edtAmount.setGravity(Gravity.CENTER);
            edtAmount.setHint("");
            edtAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            edtAmount.setTransformationMethod(new HiddenPassTransformationMethod());
        }

        if (style == INPUT_STYLE.PAN) {
            edtAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) 28);
        }
        if (style == INPUT_STYLE.EXPDATE) {
            edtAmount.setGravity(Gravity.CENTER);
            edtAmount.setHint("__/__");
        }
        flkeyBoardContainer = (FrameLayout) findViewById(R.id.fl_trans_softkeyboard);
        flkeyBoardContainer.setOnTouchListener((v, event) -> true);
        flkeyBoardContainer.setVisibility(View.VISIBLE);

        softKeyboard = (SoftKeyboardPosStyle) findViewById(R.id.soft_keyboard_view);
        softKeyboardPassword = (SoftKeyboardPasswordStyle) findViewById(R.id.soft_keyboard_view_password);
        softKeyboard.setVisibility(style == INPUT_STYLE.AMOUNT ? View.VISIBLE : View.INVISIBLE);
        softKeyboardPassword.setVisibility(style != INPUT_STYLE.AMOUNT ? View.VISIBLE : View.INVISIBLE);

        if (timeout > 0) {
            timer = new TickTimer(timeout, 1);
            timer.setTimeCountListener(this);
            timer.start();
        }
    }

    private void setListeners() {
        softKeyboard.setOnItemClickListener(new SoftKeyboardPosStyle.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int index) {
                if (index == KeyEvent.KEYCODE_ENTER) {

                    //this.getContext();

                    value = edtAmount.getText().toString().trim();
                    if (value != null && !value.equals("0.00") && !value.equals("")) {
                        //KeyBoardUtils.hide(THIS, flkeyBoardContainer);
                        TimerReset(false);
                        endStatus = UI.OK;
                        finish();
                    }

                } else if (index == SoftKeyboardPosStyle.KEY_EVENT_CANCEL) {
                    value = null;
                    TimerReset(false);
                    endStatus = UI.CANCEL;
                    finish();
                }
            }
        });

        softKeyboardPassword.setOnItemClickListener(new SoftKeyboardPasswordStyle.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int index) {
                if (index == KeyEvent.KEYCODE_ENTER) {

                    value = edtAmount.getText().toString().replace(" ", "").trim().replaceAll("[^(0-9)]", "");
                    if (value != null && !value.equals("") && value.length() >= min) {
                        if (!fAcceptZero && c.atol(value.getBytes()) == 0)
                            return;

                        TimerReset(false);
                        endStatus = UI.OK;
                        finish();
                    }

                } else if (index == SoftKeyboardPosStyle.KEY_EVENT_CANCEL) {
                    value = null;
                    TimerReset(false);
                    endStatus = UI.CANCEL;
                    finish();
                }
            }
        });


        edtAmount.setOnTouchListener((v, event) -> {
            edtAmount.setFocusable(true);
            //KeyBoardUtils.show(v.getContext(), flkeyBoardContainer);
            flkeyBoardContainer.setVisibility(View.VISIBLE);
            //llMenu.setVisibility(View.GONE);
            return false;
        });

        if (style == INPUT_STYLE.AMOUNT) {
            edtAmount.addTextChangedListener(new AmountTextWatcher(this) {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    super.onTextChanged(s, start, before, count);
                }
            });
        } else if (style == INPUT_STYLE.PAN) {
            edtAmount.addTextChangedListener(new PanTextWatcher(this) {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    super.onTextChanged(s, start, before, count);
                }
            });
        } else if (style == INPUT_STYLE.EXPDATE) {
            edtAmount.addTextChangedListener(new ExpDateTextWatcher(this) {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    super.onTextChanged(s, start, before, count);
                }
            });
        } else {
            edtAmount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    TimerReset(true);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        TimerReset(false);
        endStatus = UI.CANCEL;
        super.onBackPressed();
    }
}
