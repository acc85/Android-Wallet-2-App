package info.blockchain.wallet.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import piuk.blockchain.android.R;
/**
 * Created by Raymond on 03/04/2015.
 */
public class PinCreateNewWalletFragment extends Fragment{

    private EditText pin1,pin2,pin3,pin4;
    private boolean deleteActive;


    public static String PIN_CREATE_NEW_FRAGMENT = "pin_create_new_fragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.pin_create_fragment_layout,null);

        pin1 = (EditText)view.findViewById(R.id.pin1);
        pin2 = (EditText)view.findViewById(R.id.pin2);
        pin3 = (EditText)view.findViewById(R.id.pin3);
        pin4 = (EditText)view.findViewById(R.id.pin4);

        pin1.requestFocus();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        pin1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!pin2.getText().toString().isEmpty()) {
                    return true;
                } else
                    deleteActive = false;
                return false;
            }
        });

        pin2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!pin3.getText().toString().isEmpty()) {
                    return true;
                } else
                    deleteActive = false;
                return false;
            }
        });

        pin3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!pin4.getText().toString().isEmpty()) {
                    return true;
                } else
                    deleteActive = false;
                return false;
            }
        });

        pin4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                deleteActive = false;
                return false;
            }
        });


        pin2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                EditText eView = (EditText) view;
                if (deleteActive) {
                    deleteActive = false;
                    return true;
                } else if (i == 67) {
                    if (eView.getText().length() < 1) {
                        deleteActive = true;
                        pin1.getText().clear();
                        pin1.requestFocus();
                        return true;
                    } else {
                        pin2.getText().clear();
                        pin1.requestFocus();
                        deleteActive = true;
                        return true;
                    }
                }
                return false;
            }
        });


        pin3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                EditText eView = (EditText) view;
                if (deleteActive) {
                    deleteActive = false;
                    return true;
                } else if (i == 67) {
                    if (eView.getText().length() < 1) {
                        deleteActive = true;
                        pin2.getText().clear();
                        pin2.requestFocus();
                        return true;
                    } else {
                        pin3.getText().clear();
                        pin2.requestFocus();
                        deleteActive = true;

                        return true;
                    }
                }
                return false;
            }
        });

        pin4.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                EditText eView = (EditText) view;
                if (deleteActive) {
                    deleteActive = false;
                    return true;
                } else if (i == 67) {
                    if (eView.getText().length() < 1) {
                        deleteActive = true;
                        pin3.getText().clear();
                        pin3.requestFocus();
                        return true;
                    } else {
                        pin4.getText().clear();
                        pin3.requestFocus();
                        deleteActive = true;
                        return true;
                    }
                }
                return false;
            }

        });


        pin1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    pin2.requestFocus();
                }
                checkFieldsFilled();
            }
        });


        pin2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    pin3.requestFocus();
                }
                checkFieldsFilled();
            }
        });

        pin3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    pin4.requestFocus();
                }
                checkFieldsFilled();
            }
        });

        pin4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                System.out.println("is changed");
                checkFieldsFilled();
            }
        });



        return view;
    }

    public String getPinString(){
        return pin1.getText().toString()+pin2.getText().toString()+pin3.getText().toString()+pin4.getText().toString();
    }

    public void clearPin(){
        pin1.getText().clear();
        pin2.getText().clear();
        pin3.getText().clear();
        pin4.getText().clear();
        pin1.requestFocus();
    }

    public void checkFieldsFilled(){
        if(pin1.getText().toString().isEmpty() ||
                pin2.getText().toString().isEmpty() ||
                pin3.getText().toString().isEmpty() ||
                pin4.getText().toString().isEmpty()){
        }else{
            ((PinCreateActivity)getActivity()).switchToConfirmPin(getPinString());
            clearPin();
        }
    }

}


