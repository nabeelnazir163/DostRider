package com.zillion;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.zillion.dostrider.Home;
import com.zillion.dostrider.Model.Rider;
import com.zillion.dostrider.R;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase db;
    DatabaseReference users;
    FirebaseAuth.AuthStateListener mAuthListener;
    MaterialEditText edtEmail;
    MaterialEditText edtusername;
    MaterialEditText edtpass;
    MaterialEditText edtPhone;
    MaterialEditText edtProfession;
    Button register_button;



    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/arkhip.regular.ttf").
                        setFontAttrId(R.attr.fontPath).
                        build());
        setContentView(R.layout.register_rider);
        edtEmail = findViewById(R.id.etEmail);
        edtusername = findViewById(R.id.etName);
        edtpass = findViewById(R.id.etPassword);
        edtPhone = findViewById(R.id.etPhone);
        register_button = findViewById(R.id.dialog_register_button);

        Listener();



        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Intent googleintent = new Intent(RegisterActivity.this, Home.class);
                    startActivity(googleintent);


                }
            }
        };

        if (mAuth.getCurrentUser() != null) {
            Intent i = new Intent(RegisterActivity.this, Home.class);
            startActivity(i);
        }

        db = FirebaseDatabase.getInstance();
        users = db.getReference("Riders");
    }

    private void Listener() {
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(view, "Please enter Email Address", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (!isValidEmail(edtEmail.getText().toString())) {
                    Snackbar.make(view, "Please enter Valid Email Address", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(edtusername.getText().toString())) {
                    Snackbar.make(view, "Please enter Username", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtpass.getText().toString())) {
                    Snackbar.make(view, "Please enter Password", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if ((edtpass.getText().toString()).length() < 6) {
                    Snackbar.make(view, "Please enter Password of 6 characters or numbers.", Snackbar.LENGTH_SHORT);
                    return;
                }

                if (TextUtils.isEmpty(edtPhone.getText().toString())) {
                    Snackbar.make(view, "Please enter Phone Number", Snackbar.LENGTH_SHORT).show();
                    return;
                }


                final android.app.AlertDialog waitingdialog = new SpotsDialog(RegisterActivity.this);
                waitingdialog.show();
                //register new user.....

                mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtpass.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //save user to db...
                                Rider user = new Rider();
                                user.setEmail(edtEmail.getText().toString());
                                user.setName(edtusername.getText().toString());
                                user.setPassword(edtpass.getText().toString());
                                user.setPhone(edtPhone.getText().toString());


                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                                        setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(view, "Registered Successfully", Snackbar.LENGTH_LONG).
                                                        show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(view, "Failed" + e.getMessage(), Snackbar.LENGTH_LONG).
                                                show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(view, "Failed" + e.getMessage(), Snackbar.LENGTH_LONG).
                                show();
                    }
                });
            }
        });


    }




    public final static boolean isValidEmail(String target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

}
