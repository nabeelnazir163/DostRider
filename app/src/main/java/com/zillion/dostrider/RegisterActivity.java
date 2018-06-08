package com.zillion.dostrider;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.zillion.dostrider.Common.Common;
import com.zillion.dostrider.Model.Rider;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RegisterActivity extends AppCompatActivity {

    Button btnsignin;
    Button btnRegister;
    RelativeLayout rootlayout;
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    DatabaseReference users;
    LoginButton loginButton;
    CallbackManager mCallbackManager;
    private SignInButton mGooglesignin;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleSignInClient;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/arkhip.regular.ttf").
                        setFontAttrId(R.attr.fontPath).
                        build());
        setContentView(R.layout.register_activity);

//        Button btn = (Button) findViewById(R.id.btn_signin);
//        Animation animation1 =
//                AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move);
//        btn.startAnimation(animation1);

        mCallbackManager = CallbackManager.Factory.create();


        init();
        Listeners();

//        init for firebase
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
        users = db.getReference(Common.user_rider_tbl);
        loginThroughFb();
        //google sign in options...
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = new GoogleApiClient.Builder(getApplicationContext()).
                enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(RegisterActivity.this, "you got some Error..", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

        final android.app.AlertDialog waitingdialog = new SpotsDialog(RegisterActivity.this);
        waitingdialog.show();
    }

    //initialization for th evariables......
    public void init() {

        btnsignin = findViewById(R.id.btn_signin);
        btnRegister = findViewById(R.id.btn_register);
        rootlayout = findViewById(R.id.root_layout);
        mGooglesignin = findViewById(R.id.login_google_btn);
//        serviceproviderlayout=findViewById(R.id.service_provider_layout);

    }

    public void loginThroughFb() {

        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("check", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("check", "facebook:onCancel");
                // ...
                //
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("check", "facebook:onError", error);
                // ...
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        //for the google sign in ..
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("googlesign_in", "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        Log.d("check", "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        //save user to db...
                        Rider user = new Rider();
                        user.setEmail(account.getEmail());
                        user.setName(account.getDisplayName());

                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                                setValue(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Log.d("check", "signInWithCredential:success");
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    updateUI(firebaseUser);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(rootlayout, "Failed" + e.getMessage(), Snackbar.LENGTH_SHORT).
                                    show();
                            }
                        });

                        // Sign in success, update UI with the signed-in user's information

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("check", "signInWithCredential:failure", task.getException());
                        Snackbar.make(findViewById(R.id.root_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        updateUI(null);
                    }

                    // ...
                }
            });
    }

    //for handling facebook token....
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("checek", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("check", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("check", "signInWithCredential:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(mAuthListener);
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }

    private void updateUI(FirebaseUser user) {
        Intent fb_intent = new Intent(RegisterActivity.this, Home.class);
        startActivity(fb_intent);
    }

    //listeners
    public void Listeners() {

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });
        btnsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogindialog();
            }
        });
        loginButton = findViewById(R.id.login_fb_button);
        mGooglesignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }

    //dialogue for the register user....
    private void showRegisterDialog() {

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View view = layoutInflater.inflate(R.layout.layout_register, null);
        final MaterialEditText edtEmail = view.findViewById(R.id.etEmail);
        final MaterialEditText edtusername = view.findViewById(R.id.etName);
        final MaterialEditText edtpass = view.findViewById(R.id.etPassword);
        final MaterialEditText edtPhone = view.findViewById(R.id.etPhone);

        Button register_button = view.findViewById(R.id.dialog_register_button);

        final AlertDialog alertDialog = new AlertDialog.Builder(RegisterActivity.this)
                .setView(view)
//                .setPositiveButton(android.R.string.ok, null)
//                .setNegativeButton(android.R.string.cancel, null)
                .show();

//        Button positive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//
//        Button negative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Do Your thing

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
                                                Snackbar.make(rootlayout, "Registered Successfully", Snackbar.LENGTH_SHORT).
                                                        show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(rootlayout, "Failed" + e.getMessage(), Snackbar.LENGTH_SHORT).
                                                show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootlayout, "Failed" + e.getMessage(), Snackbar.LENGTH_SHORT).
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

    //dialogue for login button....
    private void showLogindialog() {

       /* AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Login");
        dialog.setMessage("Please Enter Email for SignIn....");*/

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View view = layoutInflater.inflate(R.layout.layout_login, null);
        final MaterialEditText edtlEmail = view.findViewById(R.id.etEmail);
        final MaterialEditText edtlpass = view.findViewById(R.id.etPassword);
        final Button signin_button = view.findViewById(R.id.login_dialog_Signin_button);


//        dialog.setView(view);
        final AlertDialog alertDialoglogin = new AlertDialog.Builder(RegisterActivity.this)
                .setView(view)
//                .setPositiveButton(android.R.string.ok, null)
//                .setNegativeButton(android.R.string.cancel, null)
                .show();

//        Button positivelogin = alertDialoglogin.getButton(AlertDialog.BUTTON_POSITIVE);
//
//        Button negativelogin = alertDialoglogin.getButton(AlertDialog.BUTTON_NEGATIVE);

        signin_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                //check validation....
                if (TextUtils.isEmpty(edtlEmail.getText().toString())) {
                    Snackbar.make(view, "Please enter Email Address", Snackbar.LENGTH_SHORT).show();
                    return;

                }
                if (TextUtils.isEmpty(edtlpass.getText().toString())) {
                    Snackbar.make(rootlayout, "Please enter Password", Snackbar.LENGTH_SHORT).show();
                    return;

                }
                if (edtlpass.getText().toString().length() < 6) {
                    Snackbar.make(rootlayout, "Please enter Password", Snackbar.LENGTH_SHORT).show();
                    return;

                }

                final android.app.AlertDialog waitingdialog=new SpotsDialog(RegisterActivity.this);
                waitingdialog.show();
                //Login.....

                mAuth.signInWithEmailAndPassword(edtlEmail.getText().toString(), edtlpass.getText().toString()).
                        addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                waitingdialog.show();
                                startActivity(new Intent(RegisterActivity.this, Home.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingdialog.dismiss();
                        Snackbar.make(view, "Failed : " + e.getMessage(), Snackbar.LENGTH_SHORT).
                                show();
                    }
                });
            }
        });

        /*negativelogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialoglogin.dismiss();
            }
        });*/
    }
}