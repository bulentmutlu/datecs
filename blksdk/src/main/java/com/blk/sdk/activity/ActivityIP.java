package com.blk.sdk.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.blk.sdk.UI;
import com.blk.sdk.c;
import com.blk.sdk.R;

public class ActivityIP extends BaseActivity {
    private CustomEditText[] edits = new CustomEditText[4];

    private SoftKeyboardPasswordStyle softKeyboardPassword;
    private FrameLayout flkeyBoardContainer;
    private boolean fDelHandled = false, fInit = false;

    public int endStatus= UI.NONE;
    public String value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);

        Log.i("ActivityIP", "ActivityIP  Start : " + Thread.currentThread().getId());

        initView(); //emot
        setListeners(this);
        fInit = true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (endStatus == UI.NONE) endStatus = UI.TIMEOUT;
    }
    @Override
    public void onBackPressed() {
        endStatus = UI.CANCEL;
        super.onBackPressed();
    }
    private void initView() {
        String h = getIntent().getStringExtra("header");
        String initial = getIntent().getStringExtra("initial");
        String[] iniitalIPParts = null;
        if (initial != null) iniitalIPParts = initial.split("\\.");
        int max = 3;

        ((TextView) findViewById(R.id.header)).setText(h);

        int[] ids = new int[]{R.id.customEditText2, R.id.customEditText7, R.id.customEditText8, R.id.customEditText9};
        for (int i = 0; i < 4; ++i)
        {
            edits[i] = (CustomEditText) findViewById(ids[i]);
            edits[i].setInputType(InputType.TYPE_NULL);
            edits[i].setIMEEnabled(false, true);
            edits[i].setSelectAllOnFocus(true);
            edits[i].setFilters(new InputFilter[] {new InputFilter.LengthFilter(max)});
            edits[i].addTextChangedListener(new ActivityIP.IPTextWatcher(i) {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    super.onTextChanged(s, start, before, count);
                }
            });
            if (iniitalIPParts != null && iniitalIPParts[i] != null && c.atoi(iniitalIPParts[i].getBytes()) <= 255)
            {
                edits[i].setText(iniitalIPParts[i]);
            }
        }

        flkeyBoardContainer = (FrameLayout) findViewById(R.id.fl_trans_softkeyboard);
        flkeyBoardContainer.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        flkeyBoardContainer.setVisibility(View.VISIBLE);

        softKeyboardPassword = (SoftKeyboardPasswordStyle) findViewById(R.id.soft_keyboard_view_password);
        softKeyboardPassword.setVisibility(View.VISIBLE);
        value = "0.0.0.0";
    }

    private void setListeners(final ActivityIP a) {

        softKeyboardPassword.setOnItemClickListener(new SoftKeyboardPasswordStyle.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int index) {
                if (index == KeyEvent.KEYCODE_ENTER) {

                    value = "";
                    if (edits[0].getText().toString() == null || c.atoi(edits[0].getText().toString().getBytes()) == 0) {
                        Log.i("ActivityIP", "onItemClick  Start : " + Thread.currentThread().getId());
                        UI.Toast("Lütfen geçerli ip giriniz");
                        return;
                    }
                    for (int i = 0; i < 4; ++i)
                    {
                        if (edits[i].getText().toString() == null ||
                                edits[i].getText().toString().length() == 0 ||
                                c.atoi(edits[i].getText().toString().getBytes()) > 255) {
                            UI.Toast("Lütfen geçerli ip giriniz");
                            return;
                        }

                        value += edits[i].getText().toString() + ((i < 3) ? "." : "");
                    }
                    endStatus = UI.OK;
                    finish();
                } else if (index == SoftKeyboardPosStyle.KEY_EVENT_CANCEL) {
                    value = null;
                    endStatus = UI.CANCEL;
                    finish();
                }
                else if (index == KeyEvent.KEYCODE_DEL)
                {
                    if (!fDelHandled) {
                        for (int i = 1; i < 4; ++i) {
                            if (edits[i].isFocused()) {
                                if (edits[i].getText().toString().length() == 0) {

                                    final int e = i - 1;
                                    a.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            edits[e].setFocusable(true);
                                            edits[e].requestFocus();
                                            edits[e].setSelection(edits[e].getText().length());

                                            BaseInputConnection inputConnection = new BaseInputConnection(edits[e], true);
                                            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                                        }
                                    });
                                }
                                break;
                            }
                        }
                    }
                    else fDelHandled = false;
                }
            }
        });


        edits[0].setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edits[0].setFocusable(true);
                //KeyBoardUtils.show(v.getContext(), flkeyBoardContainer);
                flkeyBoardContainer.setVisibility(View.VISIBLE);
                //llMenu.setVisibility(View.GONE);
                return false;
            }
        });
        edits[1].setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edits[1].setFocusable(true);
                //KeyBoardUtils.show(v.getContext(), flkeyBoardContainer);
                flkeyBoardContainer.setVisibility(View.VISIBLE);
                //llMenu.setVisibility(View.GONE);
                return false;
            }
        });
        edits[2].setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edits[2].setFocusable(true);
                //KeyBoardUtils.show(v.getContext(), flkeyBoardContainer);
                flkeyBoardContainer.setVisibility(View.VISIBLE);
                //llMenu.setVisibility(View.GONE);
                return false;
            }
        });
        edits[3].setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edits[3].setFocusable(true);
                //KeyBoardUtils.show(v.getContext(), flkeyBoardContainer);
                flkeyBoardContainer.setVisibility(View.VISIBLE);
                //llMenu.setVisibility(View.GONE);
                return false;
            }
        });
    }

    public class IPTextWatcher implements TextWatcher {

        private boolean mEditing;
        private String strPre = "";
        private int e;

        public IPTextWatcher(int e) {
            mEditing = false;
            this.e = e;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        @Override
        public void afterTextChanged(Editable s) {
            if (!fInit) return;

            if (!mEditing) {
                mEditing = true;
                String digits = s.toString().replace(".", "").trim().replaceAll("[^(0-9)]", "");
                String str = "";

                if (digits.length() < strPre.length()) fDelHandled = true;

                if (e < 3 &&
                        (digits.length() == 3 || (digits.length() == 2 && c.atoi(digits.getBytes()) > 25)))
                {
                    //str = strPre;
                    edits[e + 1].setFocusable(true);
                    edits[e + 1].requestFocus();
//                    byte c = (byte) ((byte) digits.charAt(digits.length() - 1) - '0');
//
//                    BaseInputConnection inputConnection = new BaseInputConnection(edits[e + 1], true);
//                    inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0 + c));
                }

                if (c.atoi(digits.getBytes()) > 255) {
                    str = strPre;

                    if (e < 3)
                    {
                        edits[e + 1].setFocusable(true);
                        edits[e + 1].requestFocus();

                        byte c = (byte) ((byte) digits.charAt(digits.length() - 1) - '0');

                        BaseInputConnection inputConnection = new BaseInputConnection(edits[e + 1], true);
                        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0 + c));
                    }
                }
                else {
                    str = digits;//String.format("%d", Long.parseLong(digits) / 100, Long.parseLong(digits) % 100);
                }

                try {
                    s.replace(0, s.length(), str);
                    strPre = str;
                } catch (NumberFormatException nfe) {
                    s.clear();
                }
                mEditing = false;
            }
        }
    }

}
