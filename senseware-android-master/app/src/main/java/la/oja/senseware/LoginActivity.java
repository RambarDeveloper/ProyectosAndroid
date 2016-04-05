package la.oja.senseware;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import la.oja.senseware.models.Project;
import la.oja.senseware.models.User;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, OnClickListener {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final String SHARED_PREFERENCES_KEY = "ActivitySharedPreferences_data";
    private String responseApi = null;
    protected boolean showSanckBar = false;

    String PROJECT_NUMBER="586199636323";
    String RID = null;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private ImageButton btnEmail;
    private ImageButton btnPsw;
    ApiCall call;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View start = (View) findViewById(R.id.logo);
        btnEmail = (ImageButton) findViewById(R.id.email_clear);
        btnPsw = (ImageButton) findViewById(R.id.password_clear);

        btnEmail.setVisibility(View.GONE);

        String mail = getIntent().getStringExtra("email");

        if(!TextUtils.isEmpty(mail)){
            TextView tv = (TextView) findViewById(R.id.input_email);
            tv.setText(mail);

        }

        String psw = getIntent().getStringExtra("psw");

        if(!TextUtils.isEmpty(psw))
        {
            TextView tv = (TextView) findViewById(R.id.input_password);
            tv.setText(psw);

        }


        mPasswordView = (EditText) findViewById(R.id.input_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_ACTION_SEND) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(),
                            InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });


        Button mEmailSignInButton = (Button) findViewById(R.id.btn_login);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(),
                        InputMethodManager.RESULT_UNCHANGED_SHOWN);
                attemptLogin();
            }
        });

        Typeface ultralight= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Ultralight.ttf");
        Typeface light= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Light.ttf");
        Typeface thin= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Thin.ttf");
        Typeface regular= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Regular.ttf");

        mEmailView = (EditText) findViewById(R.id.input_email);
        TextView mSignInLink = (TextView) findViewById(R.id.link_signup);
        TextView mForgetLink = (TextView) findViewById(R.id.link_forget);

        mSignInLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent in = new Intent(getApplicationContext(), RegisterActivity.class);
                in.putExtra("email", mEmailView.getText().toString());
                in.putExtra("psw", mPasswordView.getText().toString());
                startActivity(in);

            }
        });

        mForgetLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent in = new Intent(getApplicationContext(), ForgetPasswordActivity.class);
                in.putExtra("email", mEmailView.getText().toString());
                startActivity(in);
                //startActivity(new Intent(getApplicationContext(), HeadphonesActivity.class));

            }
        });

        mEmailView.setTypeface(thin);
        mPasswordView.setTypeface(thin);

        mSignInLink.setTypeface(ultralight);
        mForgetLink.setTypeface(ultralight);

        mEmailSignInButton.setTypeface(thin);

        mProgressView = findViewById(R.id.login_progress);

        mEmailView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0)
                    btnEmail.setVisibility(View.GONE);
                else
                    btnEmail.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEmailView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && !TextUtils.isEmpty(mEmailView.getText()))
                    btnEmail.setVisibility(View.VISIBLE);
                else
                    btnEmail.setVisibility(View.GONE);
            }
        });

        mPasswordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0)
                    btnPsw.setVisibility(View.GONE);
                else
                    btnPsw.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && !TextUtils.isEmpty(mPasswordView.getText()))
                    btnPsw.setVisibility(View.VISIBLE);
                else
                    btnPsw.setVisibility(View.GONE);
            }
        });

        btnEmail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TextView tv = (TextView) findViewById(R.id.input_email);
                tv.setText("");
                btnEmail.setVisibility(View.GONE);
            }
        });

        btnPsw.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                TextView tv = (TextView) findViewById(R.id.input_password);
                tv.setText("");
                btnPsw.setVisibility(View.GONE);
            }
        });

        GCMClientManager pushClientManager = new GCMClientManager(this, PROJECT_NUMBER);
        pushClientManager.registerIfNeeded(new GCMClientManager.RegistrationCompletedHandler() {
            @Override
            public void onSuccess(String registrationId, boolean isNewRegistration) {

                Log.d("Registration id", registrationId);
                //send this registrationId to your server
                RID = registrationId;
            }

            @Override
            public void onFailure(String ex) {
                super.onFailure(ex);
                Log.i("registrationID", "fallo");
            }
        });


    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String resp = "";

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)){
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
            YoYo.with(Techniques.Shake).playOn(findViewById(R.id.input_password));
        }else if(!TextUtils.isEmpty(password) && !isPassworShort(password)) {
            mPasswordView.setError("La contraseña es muy corta");
            focusView = mPasswordView;
            cancel = true;
            YoYo.with(Techniques.Shake).playOn(findViewById(R.id.input_password));
        }
        else if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError("Invalida contraseña, no puede contener ni \" o '" );
            focusView = mPasswordView;
            cancel = true;
            YoYo.with(Techniques.Shake).playOn(findViewById(R.id.input_password));
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
            YoYo.with(Techniques.Shake).playOn(findViewById(R.id.input_email));
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
            YoYo.with(Techniques.Shake).playOn(findViewById(R.id.input_email));

        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();


        } else {
            showProgress(true);
            new HttpRequestLogin(email, password).execute();

        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic

      //  return email.contains("@");
        return  android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPassworShort(String password) {
        return password.length() > 4;
    }



    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        boolean valid = true;

        //if(!password.contains("'") || !password.contains("\""))
        if((password.indexOf('\'') >= 0 || password.indexOf('"') >= 0))
            valid = false;

       return valid;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

          /*  mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            }); */

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    /*
    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }*/

    @Override
    public void onClick(View v) {
        finish();
        Intent in = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
        in.putExtra("email", mEmailView.getText().toString());
        startActivity(in);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public class HttpRequestLogin extends AsyncTask<Void, Void, String> {

        String mail;
        String pass;
        String  responseApi = "";

        public HttpRequestLogin(String email, String password) {
            mail = email;
            pass = password;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                final String url =  getString(R.string.urlAPI) + "login";

                SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                boolean newRegisterId = prefs.getBoolean("newRegistrationID", false);
                String data = "";

                if(newRegisterId)
                   data = "{'registration_id': '"+RID+"', 'platform': 'Android' }";

                call = new ApiCall(mail, pass);
                String resp = call.callPost(url, data);


                //convert the response from string to JsonObject
                JSONObject info = new JSONObject(resp);
                int status  = info.getInt("status");
                String message = info.getString("message");

                if (status == 200 && message.equals("OK")) {

                    String result = info.getString("result");


                    if(result.equals("Datos incorrectos") || result.equals("Email no registrado")) {
                        responseApi = result;
                    }
                    else
                    {
                        responseApi = "success";
                        JSONObject obj = new JSONObject(info.get("result").toString());

                        User userData = new User();
                        userData.setId_user(Integer.parseInt((String) obj.get("id_user")));
                        userData.setEmail(mail);
                        //userData.setPhone((String) obj.get("phone"));
                        userData.setPassword((String) obj.get("password"));

                        Project project = new Project();
                        project.setId_project(Integer.parseInt((String) obj.get("id_project")));
                        project.setId_user(Integer.parseInt((String) obj.get("id_user")));
                        project.setNa_project((String) obj.get("na_project"));

                        SharedPreferences settings = getSharedPreferences(SHARED_PREFERENCES_KEY, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt("id_user", userData.getId_user());
                        editor.putString("email", obj.getString("email"));
                        editor.putString("phone", obj.getString("phone"));
                        editor.putString("password", obj.getString("password"));
                        editor.putInt("id_project", project.getId_project());
                        editor.putString("na_project", obj.getString("na_project"));
                        editor.putInt("day", obj.getInt("day"));
                        editor.putInt("current", obj.getInt("current"));
                        editor.putInt("max_day", obj.getInt("day"));
                        editor.putInt("max_current", obj.getInt("current"));
                       // editor.putInt("suscription", obj.getInt("suscripton"));
                        editor.commit();

                    }
                }
                else
                {
                    responseApi = "Error de Conexión";
                }
            } catch (Exception e) {
                responseApi = "Error de Conexión";


            }

            return responseApi;
        }

        @Override
        protected void onPostExecute(String resp) {
           if(!resp.isEmpty() && (resp.equals("Datos incorrectos") || resp.equals("Email no registrado"))) {

                mEmailView.setError(resp);
                View focusView = null;
                focusView = mEmailView;

                focusView.requestFocus();
                showProgress(false);
            }
            else if(!resp.isEmpty() && resp.equals("Error de Conexión"))
            {

                View focusView = null;
                focusView = mEmailView;

                focusView.requestFocus();
                showProgress(false);

                Snackbar snackbar = Snackbar
                        .make(findViewById(android.R.id.content), responseApi, Snackbar.LENGTH_LONG);

                snackbar.show();


            }
            else {

                responseApi = null;
                finish();

                mAuthTask = new UserLoginTask(mail, pass);
                mAuthTask.execute((Void) null);


                startActivity(new Intent(LoginActivity.this, HeadphonesActivity.class));
                finish();
            }
          //  super.onPostExecute(resp);
        return;
        }


    }


}

