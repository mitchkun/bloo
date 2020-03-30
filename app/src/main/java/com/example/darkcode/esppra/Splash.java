package com.example.darkcode.esppra;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import static android.support.v4.content.ContextCompat.getSystemService;

public class Splash extends AppCompatActivity {

    Connection con;
    String Appversion = "v1.0";
    ProgressBar progS;
    private static ConnectionClass connectionClass;
    Intent launchHome;
    Boolean AppValid = true;
    Date retrievedDate;
    Timestamp ts;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Display display = getWindowManager().getDefaultDisplay();

        progS = findViewById(R.id.progBarSplash);
        progS.setVisibility(View.VISIBLE);

        connectionClass = new ConnectionClass();

        chkVersion chkVersion = new chkVersion();
        chkVersion.execute();

    }

    public class chkVersion extends AsyncTask<String, String, String> {
        String z = "";
        Boolean isSuccess = false;

        @Override
        protected void onPreExecute(){
        }

        @Override
        protected void onPostExecute(String r)
        {

            if(isSuccess == true)
            {
                if(AppValid == true)
                {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getPrices gP =  new getPrices();
                            gP.execute("");
                        }
                    }, 0);
                }
                if(AppValid == false)
                {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            Toast.makeText(getBaseContext(), "Bloo critical updates have been implemented. Please Download latest version. Siyabonga", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }, 3000);
                }
            }
            if(isSuccess == false)
            {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        Toast.makeText(getBaseContext(), z, Toast.LENGTH_LONG).show();
                        finish();
                    }
                }, 3000);
            }
        }

        @Override
        protected String doInBackground(String... strings)
        {

            try {
                Connection con = connectionClass.CONN();// Connect to database
                if (con == null) {
                    z = "Check Your Internet Access!";
                    isSuccess = false;
                    return z;
                } else
                {
                    //Expiry Date
                    String cmdExpiryDate = "Select [Expiry Date] from [Version Settings] where [VersionNo.] = '"+ Appversion +"'";
                    Statement stmtExpiryDate = con.createStatement();
                    ResultSet rsExpiryDate = stmtExpiryDate.executeQuery(cmdExpiryDate);

                    while (rsExpiryDate.next())
                    {
                        retrievedDate = rsExpiryDate.getDate(1);
                    }

                    String cmdGetMaxMNo = "Select CURRENT_TIMESTAMP";
                    Statement stmtMaxMNo = con.createStatement();
                    ResultSet rsMaxMNo = stmtMaxMNo.executeQuery(cmdGetMaxMNo);

                    if (rsMaxMNo.next())
                    {
                        ts = rsMaxMNo.getTimestamp(1);
                    }

                    if(retrievedDate.before(ts))
                    {
                        AppValid = false;
                    }

                    isSuccess = true;
                }
            }
            catch (Exception ex)
            {
                isSuccess = false;
                z = ex.getMessage();
            }
            return z;
        }
    }

    public class getPrices extends AsyncTask<String, String, String> {
        String z = "";
        Boolean isSuccess = false;

        @Override
        protected void onPreExecute(){
        }

        @Override
        protected void onPostExecute(String r)
        {

            if(isSuccess == true)
            {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        launchHome = new Intent(Splash.this, Registration.class);
                        startActivity(launchHome);
                        finish();
                    }
                }, 0);
            }
            if(isSuccess == false)
            {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        Toast.makeText(getBaseContext(), z, Toast.LENGTH_LONG).show();
                        finish();
                    }
                }, 3000);
            }
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                Connection con = connectionClass.CONN();// Connect to database
                if (con == null) {
                    z = "Check Your Internet Access!";
                    isSuccess = false;
                    return z;
                } else {
                    //Check if username already exists
                    String cmdGetPriceList = "Select * from [Price List]";
                    Statement stmtPriceList = con.createStatement();
                    ResultSet rsPriceList = stmtPriceList.executeQuery(cmdGetPriceList);

                    while (rsPriceList.next())
                    {
                        // [PNo.]
                        Integer PNo = null;
                        try {
                            PNo = rsPriceList.getInt(1);
                        } catch (Exception xSNo) {

                        }
                        // [Area]
                        String Area = null;
                        try {
                            Area = rsPriceList.getString(2);
                        } catch (Exception xService) {

                        }
                        // [Price]
                        String DeliveryPrice = null;
                        try {
                            DeliveryPrice = rsPriceList.getString(3);
                        } catch (Exception xPrice) {

                        }
                        // [DeliveryType]
                        String DeliveryType = null;
                        try {
                            DeliveryType = rsPriceList.getString(4);
                        } catch (Exception xCapacity) {

                        }

                        boolean pNoExists = false;
                        for (int r = 0; r < PersistentData.ArryPNo.size(); r++) {
                            if (PersistentData.ArryPNo.get(r) == PNo) {
                                pNoExists = true;
                            }
                        }

                        if (pNoExists == false) {
                            PersistentData.ArryPNo.add(PNo);

                            PersistentData.ArryArea.add(Area);
                            PersistentData.ArryDeliveryPrice.add(DeliveryPrice);
                            PersistentData.ArryDeliveryType.add(DeliveryType);
                        }

                    }

                    isSuccess = true;
                }
            } catch (Exception ex) {
                isSuccess = false;
                z = ex.getMessage();
            }

            return z;
        }
    }

}

