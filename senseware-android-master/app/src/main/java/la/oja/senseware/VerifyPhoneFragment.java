package la.oja.senseware;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import la.oja.senseware.R;
import la.oja.senseware.data.sensewareDataSource;
import la.oja.senseware.models.Project;
import la.oja.senseware.models.User;

/**
 * Created by Administrador on 19-01-2016.
 */
public class VerifyPhoneFragment extends BaseFlagFragment {

    String PROJECT_NUMBER="586199636323";
    String RID = null;
    ApiCall call;

    public VerifyPhoneFragment() {
    }

    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    private static final String SHARED_PREFERENCES_KEY = "ActivitySharedPreferences_data";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.register_form, container, false);

        mRegView = rootView.findViewById(R.id.reg_form);
        mProgressView = rootView.findViewById(R.id.reg_progress);
        initUI(rootView);

        GCMClientManager pushClientManager = new GCMClientManager(getActivity(), PROJECT_NUMBER);
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

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initCodes(getActivity());
    }

    @Override
    protected void send() {
        showProgress(true);
        hideKeyboard(mPhoneEdit);
        mPhoneEdit.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String phone = validate();

        boolean cancel = false;
        View focusView = null;

        if (phone == null) {
            mPhoneEdit.requestFocus();
            mPhoneEdit.setError("El número no puede estar vacio");
            focusView = mPhoneEdit;
            cancel = true;
            YoYo.with(Techniques.Shake).playOn(getActivity().findViewById(R.id.phone));
        }else
        {
            boolean isValid = isValidNumebr();
            if (!isValid)
            {
                mPhoneEdit.requestFocus();
                mPhoneEdit.setError("Número invalido");
                focusView = mPhoneEdit;
                cancel = true;
                YoYo.with(Techniques.Shake).playOn(getActivity().findViewById(R.id.phone));
            }
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
            YoYo.with(Techniques.Shake).playOn(getActivity().findViewById(R.id.password));
        }else if(!TextUtils.isEmpty(password) && !isPassworShort(password)) {
            mPasswordView.setError("La contraseña es muy corta");
            focusView = mPasswordView;
            cancel = true;
            YoYo.with(Techniques.Shake).playOn(getActivity().findViewById(R.id.password));
        }
        else if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError("Invalida contraseña, no puede contener ni \" o '" );
            focusView = mPasswordView;
            cancel = true;
            YoYo.with(Techniques.Shake).playOn(getActivity().findViewById(R.id.password));
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
            YoYo.with(Techniques.Shake).playOn(getActivity().findViewById(R.id.email));
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
            YoYo.with(Techniques.Shake).playOn(getActivity().findViewById(R.id.email));
        }

        if (cancel) {
            showProgress(false);
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            showProgress(true);
            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setPhone(phone);
            user.setId_utype(2);
            new HttpRequestRegister(user).execute();

        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return  android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPassworShort(String password) {
        return password.length() > 4;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        boolean valid =true;

        //if(!password.contains("'") || !password.contains("\""))
        if((password.indexOf('\'') >= 0 || password.indexOf('"') >= 0))
            valid = false;

        return valid;
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


            if (success) {
                getActivity().finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {

        }
    }

    private class HttpRequestRegister extends AsyncTask<Void, Void, User> {
        final String TAG = "Api";
        User user_2;
        String resp = "";

        public HttpRequestRegister(User user) {
            user_2=user;
        }

        @Override
        protected User doInBackground(Void... params) {
            User userData = null;
            Project project = null;

            try {
                final String url =  getString(R.string.urlAPI) + "users";

                call = new ApiCall(user_2.getEmail(), user_2.getPassword());
                user_2.setPassword("");
                resp = call.callPost(url, user_2);

                //convert the response from string to JsonObject
                JSONObject info = new JSONObject(resp);
                int status  = info.getInt("status");
                String message = info.getString("message");

                if (status == 200 && message.equals("OK")) {

                    String result = info.getString("result");
                    resp = result;

                    if (!result.equals("Email ya registrado")) {

                        JSONObject obj = new JSONObject(result);

                        userData = new User();
                        userData.setId_user(Integer.parseInt((String) obj.get("id_user")));
                        userData.setEmail((String) obj.get("email"));
                        userData.setPhone((String) obj.get("phone"));
                        userData.setPassword((String) obj.get("password"));

                        project = new Project();
                        project.setId_project(Integer.parseInt((String) obj.get("id_project")));
                        project.setId_user(Integer.parseInt((String) obj.get("id_user")));
                        project.setNa_project((String) obj.get("na_project"));

                        SharedPreferences settings = getActivity().getSharedPreferences(SHARED_PREFERENCES_KEY, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt("id_user", userData.getId_user());
                        editor.putInt("id_project", project.getId_project());
                        editor.putString("email", obj.getString("email"));
                        editor.putString("password", obj.getString("password"));
                        editor.putString("phone", obj.getString("phone"));

                        editor.commit();


                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                resp = "Error de conexión";
            }
            return userData;
        }

        @Override
        protected void onPostExecute(User user)
        {

            if(user == null && !TextUtils.isEmpty(resp) && resp.equals("Email ya registrado")){

                mEmailView.setError(resp);
                View focusView = null;
                focusView = mEmailView;
                YoYo.with(Techniques.Shake).playOn(getActivity().findViewById(R.id.email));
                focusView.requestFocus();
                showProgress(false);
            }
            else if(user == null && TextUtils.isEmpty(resp)  ){

                mEmailView.setError("No se puo crear el usuario");
                View focusView = null;
                focusView = mEmailView;
                YoYo.with(Techniques.Shake).playOn(getActivity().findViewById(R.id.email));
                showProgress(false);

            }
            else if(user == null && !TextUtils.isEmpty(resp) && resp.equals("Error de conexión"))
            {
                View focusView = null;
                focusView = mEmailView;

                focusView.requestFocus();
                showProgress(false);

                Snackbar snackbar = Snackbar
                        .make(getActivity().findViewById(android.R.id.content), resp, Snackbar.LENGTH_LONG);

                snackbar.show();


            }
            else{

           //     mAuthTask = new UserLoginTask(user.getEmail(), user.getPassword());
           //     mAuthTask.execute((Void) null);

                saveRegistrationID(RID);
                startActivity(new Intent(getActivity(), HeadphonesActivity.class));
            }
        }
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
            mRegView.setVisibility(show ? View.GONE : View.VISIBLE);

        }
    }

    public void saveRegistrationID(String registrationId){

        SharedPreferences settings = getActivity().getSharedPreferences(SHARED_PREFERENCES_KEY, 0);
        String url =  getString(R.string.urlAPI) +"appinstall";

        String mail = settings.getString("email", "");
        String pass = settings.getString("password", "");
        int id_user = settings.getInt("id_user", 0);

        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(),
                Context.MODE_PRIVATE);;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("newRegistrationID", false);
        editor.commit();


        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String date = sdf.format(c.getTime());

        String data = "{'id_user': "+id_user+", 'registration_id': '"+registrationId+"', 'platform':'Android', 'date_install':'"+date+"'}";

        ContentValues values_hook = new ContentValues();
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATA, data);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATE, date);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_HOOK, "appinstall");
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_TYPE, "POST");
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_UPLOAD, 0);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_URL, url);

        SaveHook obj = new SaveHook(getActivity().getApplicationContext(), values_hook, settings);

    }
}
