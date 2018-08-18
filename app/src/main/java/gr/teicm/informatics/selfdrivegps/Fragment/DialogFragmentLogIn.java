package gr.teicm.informatics.selfdrivegps.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import gr.teicm.informatics.selfdrivegps.R;

public class DialogFragmentLogIn extends android.app.DialogFragment {
    private final static String TAG = "DialogLogIn";

    private EditText etEmailToLogIn, etPasswordToLogIn;

    private FirebaseAuth mAuth;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final ViewGroup nullParent = null; //To override the warning about null
        final View mView = inflater.inflate(R.layout.activity_log_in, nullParent);

        etEmailToLogIn = mView.findViewById(R.id.et_email_log_in);
        etPasswordToLogIn = mView.findViewById(R.id.et_password_log_in);
        mAuth = FirebaseAuth.getInstance();

        TextView tvCreateAccount = mView.findViewById(R.id.tv_create_account);
        tvCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Create dialog for create account
                AlertDialog.Builder createAccountBuilder = new AlertDialog.Builder(getActivity());
                final View createAccountView = inflater.inflate(R.layout.activity_create_account, nullParent);
                createAccountBuilder.setView(createAccountView)
                        .setMessage("Create an account")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                .show();
            }
        });

        builder.setView(mView)
                .setMessage("Log in")
                .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAuth.signOut();
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startSignIn();
                    }
                });
        return builder.create();
    }

    //Start to sign in. If you signed in then you must sign out because account stay in app
    private void startSignIn(){
        String mEmail = etEmailToLogIn.getText().toString();
        String mPassword = etPasswordToLogIn.getText().toString();

        if(TextUtils.isEmpty(mEmail) || TextUtils.isEmpty(mPassword)){
            Log.d(TAG, "Fill is empty !");
        }else{
            mAuth.signInWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(!task.isSuccessful()){
                        Log.d(TAG, "You're email/password isn't correct");
                    }
                }
            });
        }
    }
}
