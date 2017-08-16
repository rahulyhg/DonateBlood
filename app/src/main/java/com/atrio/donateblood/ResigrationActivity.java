package com.atrio.donateblood;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

public class ResigrationActivity extends AppCompatActivity {
    AutoCompleteTextView atvPlaces;
    PlacesTask placesTask;
    ParserTask parserTask;
    Spinner spin_state, sp_bloodgr;
    //    RadioButton rb_male,rb_female;
    RadioButton radioSexButton;
    RadioGroup rg_group;
    CheckBox cb_never, cb_above, cb_below;
    Button btn_reg;
    EditText et_name, et_age, et_weight;
    String state_data, blood_data, radio_data, cb_data, name, age, weight, city_data;
    private DatabaseReference db_ref;
    private FirebaseDatabase db_instance;
    private FirebaseUser user;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resigration);
        mAuth=FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        spin_state = (Spinner) findViewById(R.id.spin_state);
        sp_bloodgr = (Spinner) findViewById(R.id.spin_bloodGrp);
//        rb_male=(RadioButton) findViewById(R.id.radioMale);
//        rb_female=(RadioButton) findViewById(R.id.radioFemale);
        rg_group = (RadioGroup) findViewById(R.id.radioSex);
        cb_never = (CheckBox) findViewById(R.id.cb_never);
        cb_above = (CheckBox) findViewById(R.id.cb_above);
        cb_below = (CheckBox) findViewById(R.id.cb_below);
        btn_reg = (Button) findViewById(R.id.bt_reg);
        et_name = (EditText) findViewById(R.id.input_name);
        et_age = (EditText) findViewById(R.id.input_age);
        et_weight = (EditText) findViewById(R.id.input_weight);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        atvPlaces = (AutoCompleteTextView) findViewById(R.id.atv_places);
        atvPlaces.setThreshold(1);

        atvPlaces.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesTask = new PlacesTask();
                placesTask.execute(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });
        sp_bloodgr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                blood_data = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spin_state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                state_data = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (cb_never.isChecked()) {
            cb_never.setChecked(false);
        } else if (cb_below.isChecked()) {
            cb_below.setChecked(false);
        } else {
            cb_above.setChecked(false);
        }
        cb_never.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb_never.setChecked(true);
                cb_below.setChecked(false);
                cb_above.setChecked(false);
                cb_data=cb_never.getText().toString();
            }
        });
        cb_below.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb_never.setChecked(false);
                cb_below.setChecked(true);
                cb_above.setChecked(false);
                cb_data=cb_below.getText().toString();

            }
        });
        cb_above.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb_never.setChecked(false);
                cb_below.setChecked(false);
                cb_above.setChecked(true);
                cb_data=cb_never.getText().toString();

            }
        });

        db_instance = FirebaseDatabase.getInstance();

        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connMgr = (ConnectivityManager)getApplicationContext().getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo == null) {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                }else {
                    if (validate()) {

                        db_ref = db_instance.getReference();

                        int selectedId = rg_group.getCheckedRadioButtonId();
                        radioSexButton = (RadioButton)findViewById(selectedId);
                        name = et_name.getText().toString();
                        age = et_age.getText().toString();
                        weight = et_weight.getText().toString();
                        city_data = atvPlaces.getText().toString();
                        radio_data = radioSexButton.getText().toString();

                        createUser(name, age, weight, blood_data,state_data, city_data, radio_data, cb_data);
                        et_name.setText("");
                        et_age.setText("");
                        et_weight.setText("");
                        atvPlaces.setText("");
//                                          dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "SuccessFully Register", Toast.LENGTH_LONG).show();


                    }
                }


            }
        });
    }

    private void createUser(String name, String age, String weight, String blood_data, String state_data, String city_data, String radio_data, String cb_data) {
        UserDetail userDetail=new UserDetail();

        userDetail.setName(name);
        userDetail.setAge(age);
        userDetail.setWeight(weight);
        userDetail.setBloodgroup(blood_data);
        userDetail.setState(state_data);
        userDetail.setCity(city_data);
        userDetail.setGender(radio_data);
        userDetail.setTimeperiod(cb_data);

        db_ref.child(state_data).child(city_data).child(user.getUid()).setValue(userDetail);

    }

    private boolean validate() {
// check whether the field is empty or not
        if (et_name.getText().toString().trim().length() < 1) {
            et_name.setError("Please Fill This Field");
            et_name.requestFocus();
            return false;

        } else if (et_age.getText().toString().trim().length() < 1) {
            et_age.setError("Please Fill This Field");
            et_age.requestFocus();
            return false;

        } else if (et_weight.getText().toString().trim().length() < 1) {
            et_weight.setError("Please Fill This Field");
            et_weight.requestFocus();
            return false;
        }

        else if (blood_data.equals("Select Your Blood Group")) {
            Toast.makeText(getApplicationContext(), "Select Your Blood Group", Toast.LENGTH_LONG).show();
            return false;


        }else if (state_data.equals("Select Your State")){
            Toast.makeText(getApplicationContext(), "Select Your state", Toast.LENGTH_LONG).show();
            return false;

        }
      else if (atvPlaces.getText().toString().trim().length() < 1) {
            atvPlaces.setError("Please Fill This Field");
            atvPlaces.requestFocus();
            return false;

        }  else  if (cb_data.equals("")) {
            Toast.makeText(getApplicationContext(), "Select Donation Period", Toast.LENGTH_LONG).show();
            return false;

        }  else
            return true;

    }

    private class PlacesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=AIzaSyAG-AdjAgToyXceK6-ghWS38ho8cALPaUw";

            String input = "";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

            // place type to be searched
            String types = "types=geocode";

            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = input + "&" + types + "&" + sensor + "&" + key;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url =
//                    "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=India&sensor=false&types=(regions)&key=AIzaSyAG-AdjAgToyXceK6-ghWS38ho8cALPaUw";
                    "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameters + "&components=country:IN";

            try {
                // Fetching the data from we service
                data = downloadUrl(url);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Creating ParserTask
            parserTask = new ParserTask();

            // Starting Parsing the JSON string returned by Web Service
            parserTask.execute(result);
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.i("data4565", "" + data);

            br.close();

        } catch (Exception e) {
            Log.i("url45", "" + e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJSONParser placeJsonParser = new PlaceJSONParser(state_data);

            try {
                jObject = new JSONObject(jsonData[0]);

                // Getting the parsed data as a List construct
                places = placeJsonParser.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            String[] from = new String[]{"description"};
            Log.i("data4555", "" + from.length);

//            String[] desdatas=from.substring(from.indexOf(state_data));

            int[] to = new int[]{android.R.id.text1};

            // Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

//            Log.i("data45", "" + adapter);
            // Setting the adapter
            atvPlaces.setAdapter(adapter);
        }
    }

}
