package com.lexoff.animediary.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.preference.PreferenceManager;

import com.lexoff.animediary.Constants;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Util.Utils;

public class PINFragment extends BaseFragment {

    private int mode=0;
    private Runnable callback=null;

    private EditText inputView;
    private ImageView lockView;
    private View numbersLayout, bottomButtonsLayout;

    private SharedPreferences defPrefs;
    private String pwd;

    public PINFragment() {
        //empty
    }

    public static PINFragment newInstance(int mode, Runnable callback) {
        PINFragment fragment = new PINFragment();
        fragment.setMode(mode);
        fragment.setCallback(callback);
        return fragment;
    }

    private void setMode(int mode){
        this.mode=mode;
    }

    private void setCallback(Runnable callback){
        this.callback=callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        defPrefs=PreferenceManager.getDefaultSharedPreferences(requireContext());

        //this is not a security bug
        //since pin is stored openly in default sharedpreferences
        //because I do not think it is neccesery to securely save pin
        //this is just anime tracking app, so the only purpose of this fragment's function
        //to block access to the app from curious guests of smartphone
        //also, this will require deleting pin value from sp
        //which requires root to perform, which allow to manually get app's database
        //so this will be some kind of backup in case user forget pin, but needs to enter
        pwd=defPrefs.getString(Constants.PIN_VALUE, "9876");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pin, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        int navbarHeight= Utils.getNavBarHeight(requireContext());
        rootView.setPadding(0, 0, 0, navbarHeight);

        numbersLayout=rootView.findViewById(R.id.numbers_layout);

        bottomButtonsLayout=rootView.findViewById(R.id.bottom_buttons_layout);
        bottomButtonsLayout.setVisibility(mode==1 ? View.VISIBLE : View.INVISIBLE);

        View cancelBtn=bottomButtonsLayout.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                defPrefs.edit().putBoolean(Constants.USE_PASSWORD_BLOCK, false).commit();

                requireActivity().onBackPressed();
            });
        });

        View continueBtn=bottomButtonsLayout.findViewById(R.id.continue_btn);
        continueBtn.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                requireActivity().onBackPressed();
            });
        });

        lockView=rootView.findViewById(R.id.lock_view);
        if (isWorkMode()){
            lockView.setVisibility(View.VISIBLE);
        }

        inputView=rootView.findViewById(R.id.input_view);
        inputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isWorkMode() && pwd.equals(s.toString())) {
                    numbersLayout.setClickable(false);

                    lockView.setImageResource(R.drawable.ic_unlocked_white);

                    if (callback != null) {
                        callback.run();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        View oneBtn=rootView.findViewById(R.id.one_btn);
        oneBtn.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> {
            inputView.setText(inputView.getText()+"1");
        }));

        View twoBtn=rootView.findViewById(R.id.two_btn);
        twoBtn.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> {
            inputView.setText(inputView.getText()+"2");
        }));

        View threeBtn=rootView.findViewById(R.id.three_btn);
        threeBtn.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> {
            inputView.setText(inputView.getText()+"3");
        }));

        View fourBtn=rootView.findViewById(R.id.four_btn);
        fourBtn.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> {
            inputView.setText(inputView.getText()+"4");
        }));

        View fiveBtn=rootView.findViewById(R.id.five_btn);
        fiveBtn.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> {
            inputView.setText(inputView.getText()+"5");
        }));

        View sixBtn=rootView.findViewById(R.id.six_btn);
        sixBtn.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> {
            inputView.setText(inputView.getText()+"6");
        }));

        View sevenBtn=rootView.findViewById(R.id.seven_btn);
        sevenBtn.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> {
            inputView.setText(inputView.getText()+"7");
        }));

        View eightBtn=rootView.findViewById(R.id.eight_btn);
        eightBtn.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> {
            inputView.setText(inputView.getText()+"8");
        }));

        View nineBtn=rootView.findViewById(R.id.nine_btn);
        nineBtn.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> {
            inputView.setText(inputView.getText()+"9");
        }));

        View zeroBtn=rootView.findViewById(R.id.zero_btn);
        zeroBtn.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> {
            inputView.setText(inputView.getText()+"0");
        }));

        View clearBtn=rootView.findViewById(R.id.clear_btn);
        clearBtn.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> {
            String text=inputView.getText().toString();
            if (!text.isEmpty()) {
                inputView.setText(text.substring(0, text.length() - 1));
            }
        }));
    }

    //separate fun in case it will be more conditions
    //so, to not copy-paste all of them
    private boolean isWorkMode(){
        return mode==0;
    }


    @Override
    public boolean onBackPressed() {
        if (!isWorkMode()) {
            defPrefs.edit().putString(Constants.PIN_VALUE, inputView.getText().toString()).commit();
        }

        return true;
    }
}
