package com.example.darkcode.esppra;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.apache.commons.io.filefilter.RegexFileFilter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class Registration extends AppCompatActivity {

    int fID;

    public int findUnusedId() {
        while(findViewById(++fID) != null );
        return fID;
    } //for view id generation

    ProgressBar progS;

    int Dispwidth;
    int DispHeight;


    int lastStoreNoToBeGot;

    int MaxStoreNo;

    byte[] RLogo = null;

    boolean firstLoad;
    ImageView pbxBanner;

    RelativeLayout RLayout;

    ScrollView svStores;

    Intent intentCatalogueView;

    public static ArrayList<Integer> StoreLogoList = new ArrayList<>();

    AnimationDrawable animation = PersistentData.animation;

    boolean executionComplete;
    boolean totalNoOfStoresReached;
    private static ConnectionClass connectionClass;

    @Override
    public void onRestart() {
        super.onRestart();
        totalNoOfStoresReached = false;
        LoadStoresProgressively LSP = new LoadStoresProgressively();
        LSP.execute("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);


        checkPermissions();
        connectionClass = new ConnectionClass();
        firstLoad = true;
        executionComplete = true;
        MaxStoreNo = -1;
        totalNoOfStoresReached = false;

        if(animation.getNumberOfFrames() == 0)
        {
            animation.addFrame(getResources().getDrawable(R.drawable.big_bite2), 8000);
            animation.addFrame(getResources().getDrawable(R.drawable.dinner_bucket), 8000);

        }
        animation.setOneShot(false);



        Display display = getWindowManager().getDefaultDisplay();
        Dispwidth = (display.getWidth() / 2) - 10; // ((display.getWidth()*20)/100)
        DispHeight = Dispwidth;
        int bannerHeight = display.getWidth() / 3;

        progS = findViewById(R.id.progS);
        RLayout = findViewById(R.id.RLayout);
        svStores = findViewById(R.id.svStores);

        svStores.setPadding(10, 0, 20, 10);
        svStores.requestLayout();

        svStores.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged()
            {
                int rootHeight = svStores.getHeight();
                int childHeight = svStores.getChildAt(0).getHeight();
                int scrollY = svStores.getScrollY();
                if (((childHeight + 10) - rootHeight) == scrollY)
                { //- 10 for bottom padding
                    {
                        if (executionComplete = true) {
                            LoadStoresProgressively LoadData = new LoadStoresProgressively();
                            LoadData.execute("");
                        }
                    }
                }
            }
        });

        pbxBanner = findViewById(R.id.pbxBanner);
        pbxBanner.getLayoutParams().height =  bannerHeight;

        AnimationDrawable animation = PersistentData.animation;
        pbxBanner.setBackgroundDrawable(animation);

        // start the animation!
        animation.start();

        fID = PersistentData.lastfID;

        lastStoreNoToBeGot = 0;

        LoadMaxStoreNo LMSN = new LoadMaxStoreNo();
        LMSN.execute("");

        LoadStoresProgressively LSP = new LoadStoresProgressively();
        LSP.execute("");
    }

    private void checkPermissions() {
        String[] permissions = {Manifest.permission.CALL_PHONE, Manifest.permission.RECEIVE_SMS};

         //Call Permissions Check
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                PersistentData.callpermissiongranted = true;
            } else
                {
                ActivityCompat.requestPermissions(this, permissions, PersistentData.CALL_PERMISSION_REQUEST_CODE);
            }

            //Receive Permissions Check
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
            PersistentData.smspermissiongranted = true;
             } else
             {
                ActivityCompat.requestPermissions(this, permissions, PersistentData.SMS_PERMISSION_REQUEST_CODE);
             }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PersistentData.callpermissiongranted = false;
        PersistentData.smspermissiongranted = false;
        switch (requestCode) {
            case PersistentData.CALL_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            PersistentData.callpermissiongranted = false;
                            return;
                        }
                    }
                    PersistentData.callpermissiongranted = true;

                }
            }
            case PersistentData.SMS_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            PersistentData.smspermissiongranted = false;
                            return;
                        }
                    }
                    PersistentData.smspermissiongranted = true;

                }
            }
        }
    }

    public class LoadMaxStoreNo extends AsyncTask<String, Integer, String> {
        String z = "";
        Boolean isSuccess = false;

        @Override
        protected void onPreExecute()
        {
            progS.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String r)
        {
            progS.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                Connection con = connectionClass.CONN();
                // Connect to database
                if (con == null) {
                    z = "Check Your Internet Access!";
                    isSuccess = false;
                    return z;
                } else {
                    //Count StoreNos
                    String cmdGetMaxStoreNo = "Select Count([StoreNo.]) from [Store] where [Live] = 'Y'";
                    Statement stmtMaxStoreNo = con.createStatement();
                    ResultSet rsMaxStoreNo = stmtMaxStoreNo.executeQuery(cmdGetMaxStoreNo);

                    while (rsMaxStoreNo.next()) {
                        //Store No
                        try
                        {
                            MaxStoreNo = rsMaxStoreNo.getInt(1);
                        } catch (Exception ex) {
                            //Do Nothing
                        }
                    }

                }
            } catch (Exception ex) {
                isSuccess = false;
                z = ex.getMessage();
            }

            return z;
        }
    }


    public class LoadStoresProgressively extends AsyncTask<String, Integer, String> {
        String z = "";
        Boolean isSuccess = false;

        @Override
        protected void onPreExecute()
        {
          progS.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String r)
        {
            if(firstLoad == true)
            {
                if (totalNoOfStoresReached == false)
                {
                    if (PersistentData.ArryStoreNo.size() == 0) {
                        Toast.makeText(Registration.this, "No shop(s) currently available.", Toast.LENGTH_LONG).show();
                        executionComplete = true;
                    } else
                        {
                        if (PersistentData.ArryStoreNo.size() > 0) {
                            //Find the difference between the storeNo array and items already painted(StoreLogoList array) to avoid
                            // re-painting already painted stores
                            int totalNewItemsTobePainted = PersistentData.ArryStoreNo.size() - StoreLogoList.size();

                            for (int t = PersistentData.ArryStoreNo.size() - totalNewItemsTobePainted; t < PersistentData.ArryStoreNo.size(); t++) {
                                ImageView image = null;
                                //Populate views from arrays
                                image = new ImageView(Registration.this);


                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Dispwidth, DispHeight);
                                image.setLayoutParams(layoutParams);
                                //SupersparLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("Superspar")== 0)
                                {
                                    image.setImageResource(R.drawable.logosuperspar);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                //PicknPayLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("PicknPay")== 0)
                                {
                                    image.setImageResource(R.drawable.logopick_n_pay);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                //BuynSaveSparLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("BuynSave Spar")== 0)
                                {
                                    image.setImageResource(R.drawable.logobuynsave_spar);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                //ShopriteLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("Shoprite")== 0)
                                {
                                    image.setImageResource(R.drawable.logoshoprite);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                //USaveLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("USave")== 0)
                                {
                                    image.setImageResource(R.drawable.logousave);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                //SaveRiteLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("SaveRite")== 0)
                                {
                                    image.setImageResource(R.drawable.logosave_rite);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                //SpurLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("Spur")== 0)
                                {
                                    image.setImageResource(R.drawable.logospur);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                //OceanBasketLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("Ocean Basket")== 0)
                                {
                                    image.setImageResource(R.drawable.logoocean_basket);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                //GalitosLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("Galitos")== 0)
                                {
                                    image.setImageResource(R.drawable.logogalitos);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                //PizzaInnLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("Pizza Inn")== 0)
                                {
                                    image.setImageResource(R.drawable.logopizza_inn);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                //DebonairsLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("Debonairs")== 0)
                                {
                                    image.setImageResource(R.drawable.logodebonairs_pizza);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                //SteersLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("Steers")== 0)
                                {
                                    image.setImageResource(R.drawable.logosteers);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                //KFCLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("KFC")== 0)
                                {
                                    image.setImageResource(R.drawable.logokfc);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                //NandosLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("Nandos")== 0)
                                {
                                    image.setImageResource(R.drawable.logonandos);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                //KingPieLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("King Pie")== 0)
                                {
                                    image.setImageResource(R.drawable.logokingpie);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                //OKFoodsLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("OK Foods")== 0)
                                {
                                    image.setImageResource(R.drawable.logookfoods);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                //ClicksLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("Clicks Logo")== 0)
                                {
                                    image.setImageResource(R.drawable.logoclicks);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }

                                //HungryLionLogo
                                if(PersistentData.ArryStoreName.get(t).compareTo("Hungry Lion")== 0)
                                {
                                    image.setImageResource(R.drawable.logohungry_lion);
                                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                                }

                                final int generatedId = findUnusedId();
                                StoreLogoList.add(generatedId);


                                image.setId(generatedId);
                                if (t < 3) {

                                    if (t == 0) {
                                        image.setPadding(10, 5, 0, 5);

                                        RLayout.addView(image);
                                        image.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                intentCatalogueView = new Intent(Registration.this, CatalogView.class);

                                                intentCatalogueView.putExtra("currentStoreNo", PersistentData.ArryStoreNo.get(0));
                                                intentCatalogueView.putExtra("storeType", PersistentData.ArryType.get(0));
                                                intentCatalogueView.putExtra("storeName", PersistentData.ArryStoreName.get(0));

                                                startActivity(intentCatalogueView);
                                            }
                                        });
                                    }
                                    if (t == 1) {
                                        image.setPadding(10, 5, 0, 5);
                                        RLayout.addView(image);
                                        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) image.getLayoutParams();
                                        params2.addRule(RelativeLayout.RIGHT_OF, StoreLogoList.get(StoreLogoList.size() - 2));
                                        image.setLayoutParams(params2);

                                        image.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                intentCatalogueView = new Intent(Registration.this, CatalogView.class);

                                                intentCatalogueView.putExtra("currentStoreNo", PersistentData.ArryStoreNo.get(StoreLogoList.indexOf(generatedId)));
                                                intentCatalogueView.putExtra("storeType", PersistentData.ArryType.get(StoreLogoList.indexOf(generatedId)));
                                                intentCatalogueView.putExtra("storeName", PersistentData.ArryStoreName.get(StoreLogoList.indexOf(generatedId)));
                                                startActivity(intentCatalogueView);
                                            }
                                        });
                                    }
                                    if (t == 2) {
                                        image.setPadding(10, 5, 0, 5);
                                        RLayout.addView(image);
                                        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) image.getLayoutParams();
                                        params2.addRule(RelativeLayout.BELOW, StoreLogoList.get(StoreLogoList.size() - 2));
                                        image.setLayoutParams(params2);

                                        image.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                intentCatalogueView = new Intent(Registration.this, CatalogView.class);
                                                intentCatalogueView.putExtra("currentStoreNo", PersistentData.ArryStoreNo.get(StoreLogoList.indexOf(generatedId)));
                                                intentCatalogueView.putExtra("storeType", PersistentData.ArryType.get(StoreLogoList.indexOf(generatedId)));
                                                intentCatalogueView.putExtra("storeName", PersistentData.ArryStoreName.get(StoreLogoList.indexOf(generatedId)));

                                                startActivity(intentCatalogueView);
                                            }
                                        });
                                    }
                                }

                                if (t > 2) {
                                    if (t % 2 == 0) {

                                        image.setPadding(10, 5, 0, 5);
                                        RLayout.addView(image);
                                        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) image.getLayoutParams();
                                        params2.addRule(RelativeLayout.BELOW, StoreLogoList.get(StoreLogoList.size() - 3));
                                        image.setLayoutParams(params2);

                                        image.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                intentCatalogueView = new Intent(Registration.this, CatalogView.class);

                                                intentCatalogueView.putExtra("currentStoreNo", PersistentData.ArryStoreNo.get(StoreLogoList.indexOf(generatedId)));
                                                intentCatalogueView.putExtra("storeType", PersistentData.ArryType.get(StoreLogoList.indexOf(generatedId)));
                                                intentCatalogueView.putExtra("storeName", PersistentData.ArryStoreName.get(StoreLogoList.indexOf(generatedId)));
                                                startActivity(intentCatalogueView);
                                            }
                                        });
                                    }
                                    if (t % 2 == 1) {

                                        image.setPadding(10, 5, 0, 5);
                                        RLayout.addView(image);
                                        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) image.getLayoutParams();
                                        params2.addRule(RelativeLayout.BELOW, StoreLogoList.get(StoreLogoList.size() - 3));
                                        RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams) image.getLayoutParams();
                                        params3.addRule(RelativeLayout.RIGHT_OF, StoreLogoList.get(StoreLogoList.size() - 2));
                                        image.setLayoutParams(params3);

                                        image.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                intentCatalogueView = new Intent(Registration.this, CatalogView.class);

                                                intentCatalogueView.putExtra("currentStoreNo", PersistentData.ArryStoreNo.get(StoreLogoList.indexOf(generatedId)));
                                                intentCatalogueView.putExtra("storeType", PersistentData.ArryType.get(StoreLogoList.indexOf(generatedId)));
                                                intentCatalogueView.putExtra("storeName", PersistentData.ArryStoreName.get(StoreLogoList.indexOf(generatedId)));
                                                startActivity(intentCatalogueView);
                                            }
                                        });
                                    }
                                }
                            }

                        }
                        if (MaxStoreNo == PersistentData.ArryStoreNo.size()) {
                            totalNoOfStoresReached = true;
                        }

                    }
                }
                RLayout.requestLayout();
                progS.setVisibility(View.INVISIBLE);
            }
            if(firstLoad == false)
            {
                if (PersistentData.ArryStoreNo.size() == 0) {
                    Toast.makeText(Registration.this, "No shop(s) currently available.", Toast.LENGTH_LONG).show();
                    executionComplete = true;
                } else {
                    if (PersistentData.ArryStoreNo.size() > 0) {
                        //Find the difference between the storeNo array and items already painted(StoreLogoList array) to avoid
                        // re-painting already painted stores
                        int totalNewItemsTobePainted = PersistentData.ArryStoreNo.size() - StoreLogoList.size();

                        for (int t = PersistentData.ArryStoreNo.size() - totalNewItemsTobePainted; t < PersistentData.ArryStoreNo.size(); t++) {
                            ImageView image = null;
                            //Populate views from arrays
                            image = new ImageView(Registration.this);


                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Dispwidth, DispHeight);
                            image.setLayoutParams(layoutParams);

                            //SupersparLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("Superspar")== 0)
                            {
                                image.setImageResource(R.drawable.logosuperspar);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //PicknPayLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("PicknPay")== 0)
                            {
                                image.setImageResource(R.drawable.logopick_n_pay);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //BuynSaveSparLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("BuynSave Spar")== 0)
                            {
                                image.setImageResource(R.drawable.logobuynsave_spar);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //ShopriteLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("Shoprite")== 0)
                            {
                                image.setImageResource(R.drawable.logoshoprite);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //USaveLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("USave")== 0)
                            {
                                image.setImageResource(R.drawable.logousave);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //SaveRiteLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("SaveRite")== 0)
                            {
                                image.setImageResource(R.drawable.logosave_rite);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //SpurLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("Spur")== 0)
                            {
                                image.setImageResource(R.drawable.logospur);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //OceanBasketLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("Ocean Basket")== 0)
                            {
                                image.setImageResource(R.drawable.logoocean_basket);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //GalitosLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("Galitos")== 0)
                            {
                                image.setImageResource(R.drawable.logogalitos);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //PizzaInnLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("Pizza Inn")== 0)
                            {
                                image.setImageResource(R.drawable.logopizza_inn);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //DebonairsLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("Debonairs")== 0)
                            {
                                image.setImageResource(R.drawable.logodebonairs_pizza);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //SteersLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("Steers")== 0)
                            {
                                image.setImageResource(R.drawable.logosteers);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //KFCLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("KFC")== 0)
                            {
                                image.setImageResource(R.drawable.logokfc);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //NandosLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("Nandos")== 0)
                            {
                                image.setImageResource(R.drawable.logonandos);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //KingPieLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("King Pie")== 0)
                            {
                                image.setImageResource(R.drawable.logokingpie);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //OKFoodsLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("OK Foods")== 0)
                            {
                                image.setImageResource(R.drawable.logookfoods);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //ClicksLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("Clicks")== 0)
                            {
                                image.setImageResource(R.drawable.logoclicks);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
                            //HungryLionLogo
                            if(PersistentData.ArryStoreName.get(t).compareTo("Hungry Lion")== 0)
                            {
                                image.setImageResource(R.drawable.logohungry_lion);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                            }


                            final int generatedId = findUnusedId();
                            StoreLogoList.add(generatedId);


                            image.setId(generatedId);
                            if (t < 3) {

                                if (t == 0) {
                                    image.setPadding(10, 5, 0, 5);

                                    RLayout.addView(image);
                                    image.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            intentCatalogueView = new Intent(Registration.this, CatalogView.class);

                                            intentCatalogueView.putExtra("currentStoreNo", PersistentData.ArryStoreNo.get(0));
                                            intentCatalogueView.putExtra("storeType", PersistentData.ArryType.get(0));
                                            intentCatalogueView.putExtra("storeName", PersistentData.ArryStoreName.get(0));

                                            startActivity(intentCatalogueView);
                                        }
                                    });
                                }
                                if (t == 1) {
                                    image.setPadding(10, 5, 0, 5);
                                    RLayout.addView(image);
                                    RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) image.getLayoutParams();
                                    params2.addRule(RelativeLayout.RIGHT_OF, StoreLogoList.get(StoreLogoList.size() - 2));
                                    image.setLayoutParams(params2);

                                    image.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            intentCatalogueView = new Intent(Registration.this, CatalogView.class);

                                            intentCatalogueView.putExtra("currentStoreNo", PersistentData.ArryStoreNo.get(StoreLogoList.indexOf(generatedId)));
                                            intentCatalogueView.putExtra("storeType", PersistentData.ArryType.get(StoreLogoList.indexOf(generatedId)));
                                            intentCatalogueView.putExtra("storeName", PersistentData.ArryStoreName.get(StoreLogoList.indexOf(generatedId)));

                                            startActivity(intentCatalogueView);
                                        }
                                    });
                                }
                                if (t == 2) {
                                    image.setPadding(10, 5, 0, 5);
                                    RLayout.addView(image);
                                    RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) image.getLayoutParams();
                                    params2.addRule(RelativeLayout.BELOW, StoreLogoList.get(StoreLogoList.size() - 2));
                                    image.setLayoutParams(params2);

                                    image.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            intentCatalogueView = new Intent(Registration.this, CatalogView.class);
                                            intentCatalogueView.putExtra("currentStoreNo", PersistentData.ArryStoreNo.get(StoreLogoList.indexOf(generatedId)));
                                            intentCatalogueView.putExtra("storeType", PersistentData.ArryType.get(StoreLogoList.indexOf(generatedId)));
                                            intentCatalogueView.putExtra("storeName", PersistentData.ArryStoreName.get(StoreLogoList.indexOf(generatedId)));

                                            startActivity(intentCatalogueView);
                                        }
                                    });
                                }
                            }

                            if (t > 2) {
                                if (t % 2 == 0) {

                                    image.setPadding(10, 5, 0, 5);
                                    RLayout.addView(image);
                                    RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) image.getLayoutParams();
                                    params2.addRule(RelativeLayout.BELOW, StoreLogoList.get(StoreLogoList.size() - 3));
                                    image.setLayoutParams(params2);

                                    image.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            intentCatalogueView = new Intent(Registration.this, CatalogView.class);

                                            intentCatalogueView.putExtra("currentStoreNo", PersistentData.ArryStoreNo.get(StoreLogoList.indexOf(generatedId)));
                                            intentCatalogueView.putExtra("storeType", PersistentData.ArryType.get(StoreLogoList.indexOf(generatedId)));
                                            intentCatalogueView.putExtra("storeName", PersistentData.ArryStoreName.get(StoreLogoList.indexOf(generatedId)));

                                            startActivity(intentCatalogueView);
                                        }
                                    });
                                }
                                if (t % 2 == 1) {

                                    image.setPadding(10, 5, 0, 5);
                                    RLayout.addView(image);
                                    RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) image.getLayoutParams();
                                    params2.addRule(RelativeLayout.BELOW, StoreLogoList.get(StoreLogoList.size() - 3));
                                    RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams) image.getLayoutParams();
                                    params3.addRule(RelativeLayout.RIGHT_OF, StoreLogoList.get(StoreLogoList.size() - 2));
                                    image.setLayoutParams(params3);

                                    image.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            intentCatalogueView = new Intent(Registration.this, CatalogView.class);

                                            intentCatalogueView.putExtra("currentStoreNo", PersistentData.ArryStoreNo.get(StoreLogoList.indexOf(generatedId)));
                                            intentCatalogueView.putExtra("storeType", PersistentData.ArryType.get(StoreLogoList.indexOf(generatedId)));
                                            intentCatalogueView.putExtra("storeName", PersistentData.ArryStoreName.get(StoreLogoList.indexOf(generatedId)));
                                            startActivity(intentCatalogueView);
                                        }
                                    });
                                }
                            }
                        }

                    }
                    if (MaxStoreNo == PersistentData.ArryStoreNo.size()) {
                        totalNoOfStoresReached = true;

                    }

                }
            }
            progS.setVisibility(View.INVISIBLE);

        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                Connection con = connectionClass.CONN();
                // Connect to database
                if (con == null) {
                    z = "Check Your Internet Access!";
                    isSuccess = false;
                    return z;
                }
                else
                    {
                    if (totalNoOfStoresReached == false)
                    {
                        //Get stores in 10s
                        String cmdGetStores = "Select Top(10) [StoreNo.],[Store Name],[City],[Type] from [Store] where [Live] = 'Y' AND [StoreNo.] >  " + lastStoreNoToBeGot;
                        Statement stmtStores = con.createStatement();
                        ResultSet rsStores = stmtStores.executeQuery(cmdGetStores);

                        while (rsStores.next())
                        {
                            //Store No
                            Integer StoreNo = 0;
                            try {
                                StoreNo = rsStores.getInt(1);
                            } catch (Exception ex) {
                                //Do Nothing
                            }
                            //StoreName;
                            String StoreName = null;
                            try {
                                StoreName = rsStores.getString(2);
                            } catch (Exception ex) {
                                //Do Nothing
                            }

                            //String City
                            String City = null;
                            City = rsStores.getString(3);


                            //Type;
                            String Type = "";

                            Type = rsStores.getString(4);

                            Boolean storeAlreadyAdded = false;
                            for (int s = 0; s < PersistentData.ArryStoreNo.size(); s++) {
                                if (StoreNo == PersistentData.ArryStoreNo.get(s)) {
                                    storeAlreadyAdded = true;
                                }
                            }
                            if (storeAlreadyAdded == false)
                            {
                                PersistentData.ArryStoreNo.add(StoreNo);
                                PersistentData.ArryStoreName.add(StoreName);
                                PersistentData.ArryCity.add(City);
                                PersistentData.ArryType.add(Type);
                            }

                        }
                        if (PersistentData.ArryStoreNo.size() > 0)
                        {
                            lastStoreNoToBeGot = PersistentData.ArryStoreNo.get(PersistentData.ArryStoreNo.size() - 1);
                        } else {
                            lastStoreNoToBeGot = 0;
                        }
                    }
                }
            } catch (Exception ex) {
                isSuccess = false;
                z = ex.getMessage();
            }

            return z;
        }
    }
}
