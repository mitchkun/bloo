package com.example.darkcode.esppra;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import static java.sql.Types.NULL;

public class CheckOutActivity extends AppCompatActivity {

    ImageView checkOut;

    int Dispwidth;
    int DispHeight;
    ScrollView svCatalogue;
    ScrollView svShoppingCart;
    ImageView pbxBanner;
    ProgressBar progS;
    Boolean executionComplete = true;

    String DeliveryFeeString;

    Typeface tnrBold;
    Typeface tnrRegular;//Bold

    Timestamp ts;
    private static ConnectionClass connectionClass;

    public static ArrayList<Integer> CondensedShoppingCart= new ArrayList<>();
    public static ArrayList<Integer> ShoppingCartItemQuantities = new ArrayList<>();

    ArrayList<String> lstDeliveryType = new ArrayList<>();
    ArrayList<String> lstInstantLocations = new ArrayList<>();
    ArrayList<String> lstInstantPrices = new ArrayList<>();
    ArrayList<String> lstPoolLocations = new ArrayList<>();
    ArrayList<String> lstPoolPrices = new ArrayList<>();

    TextView txtDeliverTo, txtDeliveryAddress, txtCellphone;

    Spinner cboArea, cboDeliveryType;

    TextView txtSubTotal, txtDeliveryCharge, txtTotalAmount;

    LinearLayout CartLayout;
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
                if (grantResults.length > 0)
                {
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
                if (grantResults.length > 0)
                {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        connectionClass = new ConnectionClass();

        tnrBold = Typeface.createFromAsset(getAssets(), "timesbd.ttf");
        //Regular
        tnrRegular = Typeface.createFromAsset(getAssets(), "times.ttf");

        CartLayout = findViewById(R.id.CartLayout);

        lstDeliveryType.add("Instant");
        lstDeliveryType.add("Pooled");

        progS = findViewById(R.id.progS);
        progS.setVisibility(View.INVISIBLE);

        txtDeliverTo = findViewById(R.id.txtDeliverTo);
        txtDeliveryAddress = findViewById(R.id.txtDeliveryAddress);
        txtCellphone = findViewById(R.id.txtCellphone);


        txtSubTotal = findViewById(R.id.txtSubTotal3);
        txtDeliveryCharge = findViewById(R.id.txtDeliveryCharge3);
        txtTotalAmount = findViewById(R.id.txtTotal3);

        Display display = getWindowManager().getDefaultDisplay();
        Dispwidth = (display.getWidth() / 2) - 10; // ((display.getWidth()*20)/100)
        DispHeight = Dispwidth;
        int bannerHeight = display.getWidth() / 3;

        svCatalogue = findViewById(R.id.svCatalogue2);
        svShoppingCart = findViewById(R.id.svShoppingCart2);

        svCatalogue.getLayoutParams().width = (display.getWidth() / 2);
        svShoppingCart.getLayoutParams().width = (display.getWidth() / 2);

        svCatalogue.getLayoutParams().width = Dispwidth;
        svCatalogue.setPadding(10, 0, 20, 10);
        svCatalogue.requestLayout();


        cboArea = findViewById(R.id.cboArea);

        cboDeliveryType = findViewById(R.id.cboDeliveryType);
        ArrayAdapter adtDelivType = new ArrayAdapter<String>(CheckOutActivity.this, android.R.layout.simple_spinner_item, lstDeliveryType);
        cboDeliveryType.setAdapter(adtDelivType);

        cboDeliveryType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {


                if(cboDeliveryType.getSelectedItemPosition() == 0)
                {
                    lstInstantLocations.clear();
                    lstInstantPrices.clear();
                    lstPoolLocations.clear();
                    lstPoolPrices.clear();

                    for (int s = 0; s < (PersistentData.ArryPNo.size() / 2); s++)
                    {
                        if (PersistentData.ArryDeliveryType.get(s).compareTo("Instant") == 0)
                        {
                            lstInstantLocations.add(PersistentData.ArryArea.get(s));
                            lstInstantPrices.add(PersistentData.ArryDeliveryPrice.get(s));
                        }

                    }

                    ArrayAdapter adtLocations = new ArrayAdapter<>(CheckOutActivity.this, android.R.layout.simple_spinner_item, lstInstantLocations);
                    cboArea.setAdapter(adtLocations);
                }
                    if(cboDeliveryType.getSelectedItemPosition() == 1)
                    {
                        lstInstantLocations.clear();
                        lstInstantPrices.clear();
                        lstPoolLocations.clear();
                        lstPoolPrices.clear();

                        for (int l = (PersistentData.ArryPNo.size()  / 2) - 1; l < (PersistentData.ArryPNo.size()); l++)
                        {
                            if (PersistentData.ArryDeliveryType.get(l).compareTo("Pooled") == 0) {

                                lstPoolLocations.add(PersistentData.ArryArea.get(l));
                                lstPoolPrices.add(PersistentData.ArryDeliveryPrice.get(l));

                            }
                        }
                        ArrayAdapter adtLocations = new ArrayAdapter<String>(CheckOutActivity.this, android.R.layout.simple_spinner_item, lstPoolLocations);
                        cboArea.setAdapter(adtLocations);
                    }

            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cboArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                int selectedItemPos = cboArea.getSelectedItemPosition();
             if(cboDeliveryType.getSelectedItemPosition() == 0)
             {

                 for(int u = 0; u < PersistentData.ArryArea.size(); u++)
                 {
                     if(lstInstantLocations.get(selectedItemPos) == PersistentData.ArryArea.get(u))
                     {
                        DeliveryFeeString = String.format("%.2f", new BigDecimal(PersistentData.ArryDeliveryPrice.get(u)));
                     }
                 }
             }
                if(cboDeliveryType.getSelectedItemPosition() == 1)
                {

                    for(int u = 0; u < PersistentData.ArryArea.size(); u++)
                    {
                        if(lstPoolLocations.get(selectedItemPos) == PersistentData.ArryArea.get(u))
                        {
                            DeliveryFeeString = String.format("%.2f", new BigDecimal(PersistentData.ArryDeliveryPrice.get(u)));
                        }
                    }
                }

                txtDeliveryCharge.setText(DeliveryFeeString);
                CalculateTotalAmount();
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        checkOut = findViewById(R.id.pbxFinalCheckOut);



        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Round to one digit

                int totalAmt = (int) Math.ceil(Double.parseDouble(String.valueOf(txtTotalAmount.getText()))); //always rounds up to the nearest whole

                try {
                    if ((PersistentData.callpermissiongranted)&&(PersistentData.smspermissiongranted)) {
                        startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:*007*1*2*101248*"+String.valueOf(totalAmt)+"*Bloo CheckOut"+Uri.encode("#"))));

                        if (executionComplete == true)
                        {
                            long endWaitTime = System.currentTimeMillis() + 15*1000;
                            while(System.currentTimeMillis() < endWaitTime && PersistentData.paymentConfirmed == false)
                            {

                                if(PersistentData.paymentConfirmed == true)
                                {
                                    RegisterOrder RO = new RegisterOrder();
                                    RO.execute("");
                                }
                                else
                                 {
                                    Thread.sleep(1000);
                                 }
                            }


                        }
                        else
                        {
                            Toast.makeText(getBaseContext(), "Your order registration is still in progress.Please await its completion. Siyabonga.",Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        checkPermissions();
                     }
                }
                catch(Exception ex)
                {
                    Toast.makeText(getBaseContext(), ex.getMessage(),Toast.LENGTH_LONG).show();
                }

            }
        });

        loadCurrentCartItems();

        pbxBanner = findViewById(R.id.pbxBanner2);
        pbxBanner.getLayoutParams().height =  bannerHeight;


        AnimationDrawable animation = PersistentData.animation;
        pbxBanner.setBackgroundDrawable(animation);
    }

    @Override
    public void onBackPressed()
    {
        double currentSubTotal = Double.parseDouble(String.valueOf(txtSubTotal.getText()));
        PersistentData.totalPrice = currentSubTotal;
        PersistentData.currentSubTotal = currentSubTotal;

        finish();
    }
    private void CalculateTotalAmount()
    {
        try {
              double Dfee = Double.parseDouble(DeliveryFeeString);
              String.format("%.2f", new BigDecimal(Dfee));

              double st = PersistentData.currentSubTotal;
              String.format("%.2f", new BigDecimal(st));

              double totalPrice = Dfee + st;
              txtSubTotal.setText(String.format("%.2f", new BigDecimal(st)));

              txtTotalAmount.setText(String.format("%.2f", new BigDecimal(totalPrice)));
    }
    catch(Exception x)
        {

        }

}

    private void loadCurrentCartItems()
    {
        if(PersistentData.ArryShoppingCartItemNo.size() > 0)
        {
            for(int r = 0; r < PersistentData.ArryShoppingCartItemNo.size(); r++)
            {

                TextView txtNewCartItemInfo = new TextView(CheckOutActivity.this);
                txtNewCartItemInfo.setTextColor(Color.BLACK);
                txtNewCartItemInfo.setText("\n"+ String.valueOf(PersistentData.ArryShoppingCartItemBrand.get(r))+" "+String.valueOf(PersistentData.ArryShoppingCartItemDescription.get(r)) +"@"+String.valueOf(PersistentData.ArryShoppingCartItemPrice.get(r)));
                txtNewCartItemInfo.setTypeface(tnrRegular);

                CartLayout.addView(txtNewCartItemInfo);

            }
        }
    }

    private void condenseShoppingCart()
    {
        //Check if CondensedShoppingCart already has number
        for(int d = 0; d < PersistentData.ArryShoppingCartItemNo.size(); d++)
        {
            int numberofItems = 0;

               boolean itemNoAlreadyCondensed = false;
             if(CondensedShoppingCart.contains(PersistentData.ArryShoppingCartItemNo.get(d)))
             {
                 itemNoAlreadyCondensed = true;
             }
             if(itemNoAlreadyCondensed == false)
              {
                for(int y = 0; y < PersistentData.ArryShoppingCartItemNo.size(); y++)
                {
                    if(PersistentData.ArryShoppingCartItemNo.get(d) == PersistentData.ArryShoppingCartItemNo.get(y))
                    {
                        numberofItems++;
                    }
                }
                  CondensedShoppingCart.add(PersistentData.ArryShoppingCartItemNo.get(d));
                  ShoppingCartItemQuantities.add(numberofItems);
             }
        }
    }

    public class RegisterOrder extends AsyncTask<String, String, String>
    {
        String z = "";
        Boolean isSuccess = false;

        @Override
        protected void onPreExecute()
        {
            condenseShoppingCart();
            progS.setVisibility(View.VISIBLE);
            executionComplete = false;
        }

        @Override
        protected void onPostExecute(String r)
        {
            progS.setVisibility(View.INVISIBLE);


            if(isSuccess == false)
            {
                Toast.makeText(getBaseContext(), z.toString(), Toast.LENGTH_LONG).show();
            }
            if(isSuccess == true)
            {
                Toast.makeText(getBaseContext(), "Thank you for shopping with Bloo. Your order is now in process. Siyabonga.", Toast.LENGTH_LONG).show();

                //Clear shopping cart
                PersistentData.ArryShoppingCartItemNo.clear();
                PersistentData.ArryShoppingCartItemBrand.clear();
                PersistentData.ArryShoppingCartItemDescription.clear();
                PersistentData.ArryShoppingCartItemPrice.clear();

                //Clear subtotals
                PersistentData.currentSubTotal = 0.00;
                PersistentData.totalPrice = 0.00;
                CondensedShoppingCart.clear();
                ShoppingCartItemQuantities.clear();

                //Home
                finish();
            }
            executionComplete = true;
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
                } else
                    {

                    String DeliverTo = String.valueOf(txtDeliverTo.getText());
                    String DeliveryAddress = String.valueOf(cboArea.getSelectedItem()) +": "+ String.valueOf(txtDeliveryAddress.getText());
                    String Cellphone = String.valueOf(txtCellphone.getText());
                    String Email = "cymphiwear@gmail.com";

                    int IN1 = 0;
                    int QTYIN1 = 0;
                    int IN2 = 0;
                    int QTYIN2 = 0;
                    int IN3 = 0;
                    int QTYIN3 = 0;
                    int IN4 = 0;
                    int QTYIN4 = 0;
                    int IN5 = 0;
                    int QTYIN5 = 0;
                    int IN6 = 0;
                    int QTYIN6 = 0;
                    int IN7 = 0;
                    int QTYIN7 = 0;
                    int IN8 = 0;
                    int QTYIN8 = 0;
                    int IN9 = 0;
                    int QTYIN9 = 0;
                    int IN10 = 0;
                    int QTYIN10 = 0;
                    int IN11 = 0;
                    int QTYIN11 = 0;
                    int IN12 = 0;
                    int QTYIN12 = 0;
                    int IN13 = 0;
                    int QTYIN13 = 0;
                    int IN14 = 0;
                    int QTYIN14 = 0;
                    int IN15 = 0;
                    int QTYIN15 = 0;
                    int IN16 = 0;
                    int QTYIN16 = 0;
                    int IN17 = 0;
                    int QTYIN17 = 0;
                    int IN18 = 0;
                    int QTYIN18 = 0;
                    int IN19 = 0;
                    int QTYIN19 = 0;
                    int IN20 = 0;
                    int QTYIN20 = 0;
                    int IN21 = 0;
                    int QTYIN21 = 0;
                    int IN22 = 0;
                    int QTYIN22 = 0;
                    int IN23 = 0;
                    int QTYIN23 = 0;
                    int IN24 = 0;
                    int QTYIN24 = 0;
                    int IN25 = 0;
                    int QTYIN25 = 0;
                    int IN26 = 0;
                    int QTYIN26 = 0;
                    int IN27 = 0;
                    int QTYIN27 = 0;
                    int IN28 = 0;
                    int QTYIN28 = 0;
                    int IN29 = 0;
                    int QTYIN29 = 0;
                    int IN30 = 0;
                    int QTYIN30 = 0;
                    int IN31 = 0;
                    int QTYIN31 = 0;
                    int IN32 = 0;
                    int QTYIN32 = 0;
                    int IN33 = 0;
                    int QTYIN33 = 0;
                    int IN34 = 0;
                    int QTYIN34 = 0;
                    int IN35 = 0;
                    int QTYIN35 = 0;
                    int IN36 = 0;
                    int QTYIN36 = 0;
                    int IN37 = 0;
                    int QTYIN37 = 0;
                    int IN38 = 0;
                    int QTYIN38 = 0;
                    int IN39 = 0;
                    int QTYIN39 = 0;
                    int IN40 = 0;
                    int QTYIN40 = 0;
                    int IN41 = 0;
                    int QTYIN41 = 0;
                    int IN42 = 0;
                    int QTYIN42 = 0;
                    int IN43 = 0;
                    int QTYIN43 = 0;
                    int IN44 = 0;
                    int QTYIN44 = 0;
                    int IN45 = 0;
                    int QTYIN45 = 0;
                    int IN46 = 0;
                    int QTYIN46 = 0;
                    int IN47 = 0;
                    int QTYIN47 = 0;
                    int IN48 = 0;
                    int QTYIN48 = 0;
                    int IN49 = 0;
                    int QTYIN49 = 0;
                    int IN50 = 0;
                    int QTYIN50 = 0;
                    int IN51 = 0;
                    int QTYIN51 = 0;
                    int IN52 = 0;
                    int QTYIN52 = 0;
                    int IN53 = 0;
                    int QTYIN53 = 0;
                    int IN54 = 0;
                    int QTYIN54 = 0;
                    int IN55 = 0;
                    int QTYIN55 = 0;
                    int IN56 = 0;
                    int QTYIN56 = 0;
                    int IN57 = 0;
                    int QTYIN57 = 0;
                    int IN58 = 0;
                    int QTYIN58 = 0;
                    int IN59 = 0;
                    int QTYIN59 = 0;
                    int IN60 = 0;
                    int QTYIN60 = 0;
                    int IN61 = 0;
                    int QTYIN61 = 0;
                    int IN62 = 0;
                    int QTYIN62 = 0;
                    int IN63 = 0;
                    int QTYIN63 = 0;
                    int IN64 = 0;
                    int QTYIN64 = 0;
                    int IN65 = 0;
                    int QTYIN65 = 0;
                    int IN66 = 0;
                    int QTYIN66 = 0;
                    int IN67 = 0;
                    int QTYIN67 = 0;
                    int IN68 = 0;
                    int QTYIN68 = 0;
                    int IN69 = 0;
                    int QTYIN69 = 0;
                    int IN70 = 0;
                    int QTYIN70 = 0;
                    int IN71 = 0;
                    int QTYIN71 = 0;
                    int IN72 = 0;
                    int QTYIN72 = 0;
                    int IN73 = 0;
                    int QTYIN73 = 0;
                    int IN74 = 0;
                    int QTYIN74 = 0;
                    int IN75 = 0;
                    int QTYIN75 = 0;
                    int IN76 = 0;
                    int QTYIN76 = 0;
                    int IN77 = 0;
                    int QTYIN77 = 0;
                    int IN78 = 0;
                    int QTYIN78 = 0;
                    int IN79 = 0;
                    int QTYIN79 = 0;
                    int IN80 = 0;
                    int QTYIN80 = 0;
                    int IN81 = 0;
                    int QTYIN81 = 0;
                    int IN82 = 0;
                    int QTYIN82 = 0;
                    int IN83 = 0;
                    int QTYIN83 = 0;
                    int IN84 = 0;
                    int QTYIN84 = 0;
                    int IN85 = 0;
                    int QTYIN85 = 0;
                    int IN86 = 0;
                    int QTYIN86 = 0;
                    int IN87 = 0;
                    int QTYIN87 = 0;
                    int IN88 = 0;
                    int QTYIN88 = 0;
                    int IN89 = 0;
                    int QTYIN89 = 0;
                    int IN90 = 0;
                    int QTYIN90 = 0;
                    int IN91 = 0;
                    int QTYIN91 = 0;
                    int IN92 = 0;
                    int QTYIN92 = 0;
                    int IN93 = 0;
                    int QTYIN93 = 0;
                    int IN94 = 0;
                    int QTYIN94 = 0;
                    int IN95 = 0;
                    int QTYIN95 = 0;
                    int IN96 = 0;
                    int QTYIN96 = 0;
                    int IN97 = 0;
                    int QTYIN97 = 0;
                    int IN98 = 0;
                    int QTYIN98 = 0;
                    int IN99 = 0;
                    int QTYIN99 = 0;
                    int IN100 = 0;
                    int QTYIN100 = 0;


                         try {
                         IN1 = CondensedShoppingCart.get(0);
                         QTYIN1 = ShoppingCartItemQuantities.get(0);
                             }
                             catch(Exception x)
                             {

                             }
                          try {
                     IN2 = CondensedShoppingCart.get(1);
                     QTYIN2 = ShoppingCartItemQuantities.get(1);
                              }
                              catch(Exception x)
                              {

                    }
                        try {
                     IN3 = CondensedShoppingCart.get(2);
                     QTYIN3 = ShoppingCartItemQuantities.get(2);
                        }
                        catch(Exception x)
                        {

                        }
                            try {
                     IN4 = CondensedShoppingCart.get(3);
                     QTYIN4 = ShoppingCartItemQuantities.get(3);
                            }
                            catch(Exception x)
                            {

                            }
                                try {
                     IN5 = CondensedShoppingCart.get(4);
                     QTYIN5 = ShoppingCartItemQuantities.get(4);
                                }
                                catch(Exception x)
                                {

                                }
                                    try {
                     IN6 = CondensedShoppingCart.get(5);
                     QTYIN6 = ShoppingCartItemQuantities.get(5);
                                    }
                                    catch(Exception x)
                                    {

                                    }
                                        try {
                     IN7 = CondensedShoppingCart.get(6);
                     QTYIN7 = ShoppingCartItemQuantities.get(6);
                                        }
                                        catch(Exception x)
                                        {

                                        }
                                            try {
                     IN8 = CondensedShoppingCart.get(7);
                     QTYIN8 = ShoppingCartItemQuantities.get(7);
                                            }
                                            catch(Exception x)
                                            {

                                            }
                                                try {
                     IN9 = CondensedShoppingCart.get(8);
                     QTYIN9 = ShoppingCartItemQuantities.get(8);
                                                }
                                                catch(Exception x)
                                                {

                                                }
                    try {
                     IN10 = CondensedShoppingCart.get(9);
                     QTYIN10 = ShoppingCartItemQuantities.get(9);
                      }
                    catch(Exception x)
                      {

                      }

                    try {
                     IN11 = CondensedShoppingCart.get(10);
                     QTYIN11 = ShoppingCartItemQuantities.get(10);
                      }
                      catch(Exception x)
                      {

                      }

                    try {
                     IN12 = CondensedShoppingCart.get(11);
                     QTYIN12 = ShoppingCartItemQuantities.get(11);
                       }
                    catch(Exception x)
                       {

                       }
                    try {
                     IN13 = CondensedShoppingCart.get(12);
                     QTYIN13 = ShoppingCartItemQuantities.get(12);
    }
                    catch(Exception x)
    {

    }
                    try {
                     IN14 = CondensedShoppingCart.get(13);
                     QTYIN14 = ShoppingCartItemQuantities.get(13);
}
                    catch(Exception x)
                            {

                            }
                    try {
                     IN15 = CondensedShoppingCart.get(14);
                     QTYIN15 = ShoppingCartItemQuantities.get(14);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN16 = CondensedShoppingCart.get(15);
                     QTYIN16 = ShoppingCartItemQuantities.get(15);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN17 = CondensedShoppingCart.get(16);
                     QTYIN17 = ShoppingCartItemQuantities.get(16);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN18 = CondensedShoppingCart.get(17);
                     QTYIN18 = ShoppingCartItemQuantities.get(17);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN19 = CondensedShoppingCart.get(18);
                     QTYIN19 = ShoppingCartItemQuantities.get(18);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN20 = CondensedShoppingCart.get(19);
                     QTYIN20 = ShoppingCartItemQuantities.get(19);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN21 = CondensedShoppingCart.get(20);
                     QTYIN21 = ShoppingCartItemQuantities.get(20);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN22 = CondensedShoppingCart.get(21);
                     QTYIN22 = ShoppingCartItemQuantities.get(21);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN23 = CondensedShoppingCart.get(22);
                     QTYIN23 = ShoppingCartItemQuantities.get(22);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN24 = CondensedShoppingCart.get(23);
                     QTYIN24 = ShoppingCartItemQuantities.get(23);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN25 = CondensedShoppingCart.get(24);
                     QTYIN25 = ShoppingCartItemQuantities.get(24);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN26 = CondensedShoppingCart.get(25);
                     QTYIN26 = ShoppingCartItemQuantities.get(25);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN27 = CondensedShoppingCart.get(26);
                     QTYIN27 = ShoppingCartItemQuantities.get(26);
                            }
                            catch(Exception x)
                            {

                            }
                         try {
                     IN28 = CondensedShoppingCart.get(27);
                     QTYIN28 = ShoppingCartItemQuantities.get(27);
                            }
                            catch(Exception x)
                            {

                            }
                          try {
                     IN29 = CondensedShoppingCart.get(28);
                     QTYIN29 = ShoppingCartItemQuantities.get(28);
                            }
                            catch(Exception x)
                            {

                            }
                            try {
                     IN30 = CondensedShoppingCart.get(29);
                     QTYIN30 = ShoppingCartItemQuantities.get(29);
                            }
                            catch(Exception x)
                            {

                            }
                            try {
                     IN31 = CondensedShoppingCart.get(30);
                     QTYIN31 = ShoppingCartItemQuantities.get(30);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN32 = CondensedShoppingCart.get(31);
                     QTYIN32 = ShoppingCartItemQuantities.get(31);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN33 = CondensedShoppingCart.get(32);
                     QTYIN33 = ShoppingCartItemQuantities.get(32);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN34 = CondensedShoppingCart.get(33);
                     QTYIN34 = ShoppingCartItemQuantities.get(33);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN35 = CondensedShoppingCart.get(34);
                     QTYIN35 = ShoppingCartItemQuantities.get(34);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN36 = CondensedShoppingCart.get(35);
                     QTYIN36 = ShoppingCartItemQuantities.get(35);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN37 = CondensedShoppingCart.get(36);
                     QTYIN37 = ShoppingCartItemQuantities.get(36);
                    }
                    catch(Exception x)
                    {

                    }
                        try {

                     IN38 = CondensedShoppingCart.get(37);
                     QTYIN38 = ShoppingCartItemQuantities.get(37);
                            }
                            catch(Exception x)
                            {

                            }
                        try {
                     IN39 = CondensedShoppingCart.get(38);
                     QTYIN39 = ShoppingCartItemQuantities.get(38);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN40 = CondensedShoppingCart.get(39);
                     QTYIN40 = ShoppingCartItemQuantities.get(39);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN41 = CondensedShoppingCart.get(40);
                     QTYIN41 = ShoppingCartItemQuantities.get(40);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN42 = CondensedShoppingCart.get(41);
                     QTYIN42 = ShoppingCartItemQuantities.get(41);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN43 = CondensedShoppingCart.get(42);
                     QTYIN43 = ShoppingCartItemQuantities.get(42);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN44 = CondensedShoppingCart.get(43);
                     QTYIN44 = ShoppingCartItemQuantities.get(43);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN45 = CondensedShoppingCart.get(44);
                     QTYIN45 = ShoppingCartItemQuantities.get(44);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN46 = CondensedShoppingCart.get(45);
                     QTYIN46 = ShoppingCartItemQuantities.get(45);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN47 = CondensedShoppingCart.get(46);
                     QTYIN47 = ShoppingCartItemQuantities.get(46);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN48 = CondensedShoppingCart.get(47);
                     QTYIN48 = ShoppingCartItemQuantities.get(47);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN49 = CondensedShoppingCart.get(48);
                     QTYIN49 = ShoppingCartItemQuantities.get(48);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN50 = CondensedShoppingCart.get(49);
                     QTYIN50 = ShoppingCartItemQuantities.get(49);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN51 = CondensedShoppingCart.get(50);
                     QTYIN51 = ShoppingCartItemQuantities.get(50);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN52 = CondensedShoppingCart.get(51);
                     QTYIN52 = ShoppingCartItemQuantities.get(51);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN53 = CondensedShoppingCart.get(52);
                     QTYIN53 = ShoppingCartItemQuantities.get(52);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN54 = CondensedShoppingCart.get(53);
                     QTYIN54 = ShoppingCartItemQuantities.get(53);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN55 = CondensedShoppingCart.get(54);
                     QTYIN55 = ShoppingCartItemQuantities.get(54);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN56 = CondensedShoppingCart.get(55);
                     QTYIN56 = ShoppingCartItemQuantities.get(55);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN57 = CondensedShoppingCart.get(56);
                     QTYIN57 = ShoppingCartItemQuantities.get(56);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN58 = CondensedShoppingCart.get(57);
                     QTYIN58 = ShoppingCartItemQuantities.get(57);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN59 = CondensedShoppingCart.get(58);
                     QTYIN59 = ShoppingCartItemQuantities.get(58);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN60 = CondensedShoppingCart.get(59);
                     QTYIN60 = ShoppingCartItemQuantities.get(59);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN61 = CondensedShoppingCart.get(60);
                     QTYIN61 = ShoppingCartItemQuantities.get(60);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN62 = CondensedShoppingCart.get(61);
                     QTYIN62 = ShoppingCartItemQuantities.get(61);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN63 = CondensedShoppingCart.get(62);
                     QTYIN63 = ShoppingCartItemQuantities.get(62);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN64 = CondensedShoppingCart.get(63);
                     QTYIN64 = ShoppingCartItemQuantities.get(63);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN65 = CondensedShoppingCart.get(64);
                     QTYIN65 = ShoppingCartItemQuantities.get(64);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN66 = CondensedShoppingCart.get(65);
                     QTYIN66 = ShoppingCartItemQuantities.get(65);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN67 = CondensedShoppingCart.get(66);
                     QTYIN67 = ShoppingCartItemQuantities.get(66);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN68 = CondensedShoppingCart.get(67);
                     QTYIN68 = ShoppingCartItemQuantities.get(67);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN69 = CondensedShoppingCart.get(68);
                     QTYIN69 = ShoppingCartItemQuantities.get(68);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN70 = CondensedShoppingCart.get(69);
                     QTYIN70 = ShoppingCartItemQuantities.get(69);
                    }
                    catch(Exception x)
                    {

                    }
                        try {
                     IN71 = CondensedShoppingCart.get(70);
                     QTYIN71 = ShoppingCartItemQuantities.get(70);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN72 = CondensedShoppingCart.get(71);
                     QTYIN72 = ShoppingCartItemQuantities.get(71);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN73 = CondensedShoppingCart.get(72);
                     QTYIN73 = ShoppingCartItemQuantities.get(72);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN74 = CondensedShoppingCart.get(73);
                     QTYIN74 = ShoppingCartItemQuantities.get(73);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN75 = CondensedShoppingCart.get(74);
                     QTYIN75 = ShoppingCartItemQuantities.get(74);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN76 = CondensedShoppingCart.get(75);
                     QTYIN76 = ShoppingCartItemQuantities.get(75);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN77 = CondensedShoppingCart.get(76);
                     QTYIN77 = ShoppingCartItemQuantities.get(76);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN78 = CondensedShoppingCart.get(77);
                     QTYIN78 = ShoppingCartItemQuantities.get(77);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN79 = CondensedShoppingCart.get(78);
                     QTYIN79 = ShoppingCartItemQuantities.get(78);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN80 = CondensedShoppingCart.get(79);
                     QTYIN80 = ShoppingCartItemQuantities.get(79);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN81 = CondensedShoppingCart.get(80);
                     QTYIN81 = ShoppingCartItemQuantities.get(80);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN82 = CondensedShoppingCart.get(81);
                     QTYIN82 = ShoppingCartItemQuantities.get(81);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN83 = CondensedShoppingCart.get(82);
                     QTYIN83 = ShoppingCartItemQuantities.get(82);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN84 = CondensedShoppingCart.get(83);
                     QTYIN84 = ShoppingCartItemQuantities.get(83);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN85 = CondensedShoppingCart.get(84);
                     QTYIN85 = ShoppingCartItemQuantities.get(84);
                            }
                            catch(Exception x)
                            {

                            }
                    try {
                     IN86 = CondensedShoppingCart.get(85);
                     QTYIN86 = ShoppingCartItemQuantities.get(85);
                            }
                            catch(Exception x)
                            {

                            }
                            try {
                     IN87 = CondensedShoppingCart.get(86);
                     QTYIN87 = ShoppingCartItemQuantities.get(86);
                            }
                            catch(Exception x)
                            {

                            }
                        try {
                     IN88 = CondensedShoppingCart.get(87);
                     QTYIN88 = ShoppingCartItemQuantities.get(87);
                            }
                            catch(Exception x)
                            {

                            }
                        try {
                     IN89 = CondensedShoppingCart.get(88);
                     QTYIN89 = ShoppingCartItemQuantities.get(88);
                            }
                            catch(Exception x)
                            {

                            }
                        try {
                     IN90 = CondensedShoppingCart.get(89);
                     QTYIN90 = ShoppingCartItemQuantities.get(89);
                            }
                            catch(Exception x)
                            {

                            }
                        try {
                     IN91 = CondensedShoppingCart.get(90);
                     QTYIN91 = ShoppingCartItemQuantities.get(90);
                            }
                            catch(Exception x)
                            {

                            }
                        try {
                     IN92 = CondensedShoppingCart.get(91);
                     QTYIN92 = ShoppingCartItemQuantities.get(91);
                            }
                            catch(Exception x)
                            {

                            }
                        try {
                     IN93 = CondensedShoppingCart.get(92);
                     QTYIN93 = ShoppingCartItemQuantities.get(92);
                            }
                            catch(Exception x)
                            {

                            }
                        try {
                     IN94 = CondensedShoppingCart.get(93);
                     QTYIN94 = ShoppingCartItemQuantities.get(93);
                            }
                            catch(Exception x)
                            {

                            }
                        try {
                     IN95 = CondensedShoppingCart.get(94);
                     QTYIN95 = ShoppingCartItemQuantities.get(94);
                            }
                            catch(Exception x)
                            {

                            }
                        try {
                     IN96 = CondensedShoppingCart.get(95);
                     QTYIN96 = ShoppingCartItemQuantities.get(95);
                            }
                            catch(Exception x)
                            {

                            }
                        try {
                     IN97 = CondensedShoppingCart.get(96);
                     QTYIN97 = ShoppingCartItemQuantities.get(96);
                            }
                            catch(Exception x)
                            {

                            }
                        try {
                     IN98 = CondensedShoppingCart.get(97);
                     QTYIN98 = ShoppingCartItemQuantities.get(97);
                            }
                            catch(Exception x)
                            {

                            }
                        try {
                     IN99 = CondensedShoppingCart.get(98);
                     QTYIN99 = ShoppingCartItemQuantities.get(98);
                            }
                            catch(Exception x)
                            {

                            }
                try {
                     IN100 = CondensedShoppingCart.get(99);
                     QTYIN100 = ShoppingCartItemQuantities.get(99);
                            }
                            catch(Exception x)
                            {

                            }

                    String Subtotal = String.valueOf(txtSubTotal.getText());
                    String DeliveryFee = String.valueOf(txtDeliveryCharge.getText());
                    String TotalAmount = String.valueOf(txtTotalAmount.getText());
                    String DeliveryType = String.valueOf(cboDeliveryType.getSelectedItem());

                    String cmdGetMaxMNo = "Select CURRENT_TIMESTAMP";
                    Statement stmtMaxMNo = con.createStatement();
                    ResultSet rsMaxMNo = stmtMaxMNo.executeQuery(cmdGetMaxMNo);

                    if (rsMaxMNo.next())
                    {
                        ts = rsMaxMNo.getTimestamp(1);
                    }
                    //Do Nothing

                    //if ((DeliverTo.compareTo("") != 0) && (DeliveryAddress.compareTo("") != 0) && (Cellphone.compareTo("") != 0) && (Subtotal.compareTo("") != 0) && (DeliveryFee.compareTo("") != 0) && (TotalAmount.compareTo("") != 0))
                    //{
                        //Get server timestamp.
                        try{


                        String cmdAddBooking = "Insert into [Orders] values(?,?,?,?,?" +
                                ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?" +
                                ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?" +
                                ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?" +
                                ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?" +
                                ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?" +
                                ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?" +
                                ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?" +
                                ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?" +
                                ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?" +
                                ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?" +
                                ",?,?,?,?,?)";

                        PreparedStatement stmtAddBooking = con.prepareStatement(cmdAddBooking);
                        //Statement Values


                        stmtAddBooking.setString(1, DeliverTo);
                        stmtAddBooking.setString(2, DeliveryAddress);
                        stmtAddBooking.setString(3, Cellphone);
                        stmtAddBooking.setString(4, Email);
                        stmtAddBooking.setString(5, ts.toString());//Timestamp


                               stmtAddBooking.setInt(6, IN1);
                               stmtAddBooking.setInt(7, QTYIN1);
                           if(IN2 != 0) {
                               stmtAddBooking.setInt(8, IN2);
                               stmtAddBooking.setInt(9, QTYIN2);
                            }
                            else
                            {
                               stmtAddBooking.setNull(8, NULL);
                               stmtAddBooking.setNull(9, NULL);
                            }

                            if(IN3 != 0) {
                                stmtAddBooking.setInt(10, IN3);
                                stmtAddBooking.setInt(11, QTYIN3);
                            }
                            else
                            {
                                stmtAddBooking.setNull(10, NULL);
                                stmtAddBooking.setNull(11, NULL);
                            }


                            if(IN4 != 0) {
                                stmtAddBooking.setInt(12, IN4);
                                stmtAddBooking.setInt(13, QTYIN4);
                            }
                            else
                            {
                                stmtAddBooking.setNull(12, NULL);
                                stmtAddBooking.setNull(13, NULL);
                            }

                            if(IN5 != 0) {
                                stmtAddBooking.setInt(14, IN5);
                                stmtAddBooking.setInt(15, QTYIN5);
                            }
                            else
                            {
                                stmtAddBooking.setNull(14, NULL);
                                stmtAddBooking.setNull(15, NULL);
                            }

                            if(IN6 != 0) {
                                stmtAddBooking.setInt(16, IN6);
                                stmtAddBooking.setInt(17, QTYIN6);
                            }
                            else
                            {
                                stmtAddBooking.setNull(16, NULL);
                                stmtAddBooking.setNull(17, NULL);
                            }


                            if(IN7 != 0) {
                                stmtAddBooking.setInt(18, IN7);
                                stmtAddBooking.setInt(19, QTYIN7);
                            }
                            else
                            {
                                stmtAddBooking.setNull(18, NULL);
                                stmtAddBooking.setNull(19, NULL);
                            }

                            if(IN8 != 0) {
                                stmtAddBooking.setInt(20, IN8);
                                stmtAddBooking.setInt(21, QTYIN8);
                            }
                            else
                            {
                                stmtAddBooking.setNull(20, NULL);
                                stmtAddBooking.setNull(21, NULL);
                            }

                            if(IN9 != 0) {
                                stmtAddBooking.setInt(22, IN9);
                                stmtAddBooking.setInt(23, QTYIN9);
                            }
                            else
                            {
                                stmtAddBooking.setNull(22, NULL);
                                stmtAddBooking.setNull(23, NULL);
                            }

                            if(IN10 != 0) {
                                stmtAddBooking.setInt(24, IN10);
                                stmtAddBooking.setInt(25, QTYIN10);
                            }
                            else
                            {
                                stmtAddBooking.setNull(24, NULL);
                                stmtAddBooking.setNull(25, NULL);
                            }

                            if(IN11 != 0) {
                                stmtAddBooking.setInt(26, IN11);
                                stmtAddBooking.setInt(27, QTYIN11);
                            }
                            else
                            {
                                stmtAddBooking.setNull(26, NULL);
                                stmtAddBooking.setNull(27, NULL);
                            }

                            if(IN12 != 0) {
                                stmtAddBooking.setInt(28, IN12);
                                stmtAddBooking.setInt(29, QTYIN12);
                            }
                            else
                            {
                                stmtAddBooking.setNull(28, NULL);
                                stmtAddBooking.setNull(29, NULL);
                            }

                            if(IN13 != 0) {
                                stmtAddBooking.setInt(30, IN13);
                                stmtAddBooking.setInt(31, QTYIN13);
                            }
                            else
                            {
                                stmtAddBooking.setNull(30, NULL);
                                stmtAddBooking.setNull(31, NULL);
                            }

                            if(IN14 != 0) {
                                stmtAddBooking.setInt(32, IN14);
                                stmtAddBooking.setInt(33, QTYIN14);
                            }
                            else
                            {
                                stmtAddBooking.setNull(32, NULL);
                                stmtAddBooking.setNull(33, NULL);
                            }

                            if(IN15 != 0) {
                                stmtAddBooking.setInt(34, IN15);
                                stmtAddBooking.setInt(35, QTYIN15);
                            }
                            else
                            {
                                stmtAddBooking.setNull(34, NULL);
                                stmtAddBooking.setNull(35, NULL);
                            }

                            if(IN16 != 0) {
                                stmtAddBooking.setInt(36, IN16);
                                stmtAddBooking.setInt(37, QTYIN16);
                            }
                            else
                            {
                                stmtAddBooking.setNull(36, NULL);
                                stmtAddBooking.setNull(37, NULL);
                            }

                            if(IN17 != 0) {
                                stmtAddBooking.setInt(38, IN17);
                                stmtAddBooking.setInt(39, QTYIN17);
                            }
                            else
                            {
                                stmtAddBooking.setNull(38, NULL);
                                stmtAddBooking.setNull(39, NULL);
                            }

                            if(IN18 != 0) {
                                stmtAddBooking.setInt(40, IN18);
                                stmtAddBooking.setInt(41, QTYIN18);
                            }
                            else
                            {
                                stmtAddBooking.setNull(40, NULL);
                                stmtAddBooking.setNull(41, NULL);
                            }

                            if(IN19 != 0) {
                                stmtAddBooking.setInt(42, IN19);
                                stmtAddBooking.setInt(43, QTYIN19);
                            }
                            else
                            {
                                stmtAddBooking.setNull(42, NULL);
                                stmtAddBooking.setNull(43, NULL);
                            }

                            if(IN20 != 0) {
                                stmtAddBooking.setInt(44, IN20);
                                stmtAddBooking.setInt(45, QTYIN20);
                            }
                            else
                            {
                                stmtAddBooking.setNull(44, NULL);
                                stmtAddBooking.setNull(45, NULL);
                            }

                            if(IN21 != 0) {
                                stmtAddBooking.setInt(46, IN21);
                                stmtAddBooking.setInt(47, QTYIN21);
                            }
                            else
                            {
                                stmtAddBooking.setNull(46, NULL);
                                stmtAddBooking.setNull(47, NULL);
                            }
                            if(IN22 != 0) {
                                stmtAddBooking.setInt(48, IN22);
                                stmtAddBooking.setInt(49, QTYIN22);
                            }
                            else
                            {
                                stmtAddBooking.setNull(48, NULL);
                                stmtAddBooking.setNull(49, NULL);
                            }

                            if(IN23 != 0) {
                                stmtAddBooking.setInt(50, IN23);
                                stmtAddBooking.setInt(51, QTYIN23);
                            }
                            else
                            {
                                stmtAddBooking.setNull(50, NULL);
                                stmtAddBooking.setNull(51, NULL);
                            }

                            if(IN24 != 0) {
                                stmtAddBooking.setInt(52, IN24);
                                stmtAddBooking.setInt(53, QTYIN24);
                            }
                            else
                            {
                                stmtAddBooking.setNull(52, NULL);
                                stmtAddBooking.setNull(53, NULL);
                            }

                            if(IN25 != 0) {
                                stmtAddBooking.setInt(54, IN25);
                                stmtAddBooking.setInt(55, QTYIN25);
                            }
                            else
                            {
                                stmtAddBooking.setNull(54, NULL);
                                stmtAddBooking.setNull(55, NULL);
                            }

                            if(IN26 != 0) {
                                stmtAddBooking.setInt(56, IN26);
                                stmtAddBooking.setInt(57, QTYIN26);
                            }
                            else
                            {
                                stmtAddBooking.setNull(56, NULL);
                                stmtAddBooking.setNull(57, NULL);
                            }

                            if(IN27 != 0) {
                                stmtAddBooking.setInt(58, IN27);
                                stmtAddBooking.setInt(59, QTYIN27);
                            }
                            else
                            {
                                stmtAddBooking.setNull(58, NULL);
                                stmtAddBooking.setNull(59, NULL);
                            }

                            if(IN28 != 0) {
                                stmtAddBooking.setInt(60, IN28);
                                stmtAddBooking.setInt(61, QTYIN28);
                            }
                            else
                            {
                                stmtAddBooking.setNull(60, NULL);
                                stmtAddBooking.setNull(61, NULL);
                            }

                            if(IN29 != 0) {
                                stmtAddBooking.setInt(62, IN29);
                                stmtAddBooking.setInt(63, QTYIN29);
                            }
                            else
                            {
                                stmtAddBooking.setNull(62, NULL);
                                stmtAddBooking.setNull(63, NULL);
                            }

                            if(IN30 != 0) {
                                stmtAddBooking.setInt(64, IN30);
                                stmtAddBooking.setInt(65, QTYIN30);
                            }
                            else
                            {
                                stmtAddBooking.setNull(64, NULL);
                                stmtAddBooking.setNull(65, NULL);
                            }

                            if(IN31 != 0) {
                                stmtAddBooking.setInt(66, IN31);
                                stmtAddBooking.setInt(67, QTYIN31);
                            }
                            else
                            {
                                stmtAddBooking.setNull(66, NULL);
                                stmtAddBooking.setNull(67, NULL);
                            }

                            if(IN32 != 0) {
                                stmtAddBooking.setInt(68, IN32);
                                stmtAddBooking.setInt(69, QTYIN32);
                            }
                            else
                            {
                                stmtAddBooking.setNull(68, NULL);
                                stmtAddBooking.setNull(69, NULL);
                            }

                            if(IN33 != 0) {
                                stmtAddBooking.setInt(70, IN33);
                                stmtAddBooking.setInt(71, QTYIN33);
                            }
                            else
                            {
                                stmtAddBooking.setNull(70, NULL);
                                stmtAddBooking.setNull(71, NULL);
                            }

                            if(IN34 != 0) {
                                stmtAddBooking.setInt(72, IN34);
                                stmtAddBooking.setInt(73, QTYIN34);
                            }
                            else
                            {
                                stmtAddBooking.setNull(72, NULL);
                                stmtAddBooking.setNull(73, NULL);
                            }

                            if(IN35 != 0) {
                                stmtAddBooking.setInt(74, IN35);
                                stmtAddBooking.setInt(75, QTYIN35);
                            }
                            else
                            {
                                stmtAddBooking.setNull(74, NULL);
                                stmtAddBooking.setNull(75, NULL);
                            }

                            if(IN36 != 0) {
                                stmtAddBooking.setInt(76, IN36);
                                stmtAddBooking.setInt(77, QTYIN36);
                            }
                            else
                            {
                                stmtAddBooking.setNull(76, NULL);
                                stmtAddBooking.setNull(77, NULL);
                            }

                            if(IN37 != 0) {
                                stmtAddBooking.setInt(78, IN37);
                                stmtAddBooking.setInt(79, QTYIN37);
                            }
                            else
                            {
                                stmtAddBooking.setNull(78, NULL);
                                stmtAddBooking.setNull(79, NULL);
                            }

                            if(IN38 != 0) {
                                stmtAddBooking.setInt(80, IN38);
                                stmtAddBooking.setInt(81, QTYIN38);
                            }
                            else
                            {
                                stmtAddBooking.setNull(80, NULL);
                                stmtAddBooking.setNull(81, NULL);
                            }

                            if(IN39 != 0) {
                                stmtAddBooking.setInt(82, IN39);
                                stmtAddBooking.setInt(83, QTYIN39);
                            }
                            else
                            {
                                stmtAddBooking.setNull(82, NULL);
                                stmtAddBooking.setNull(83, NULL);
                            }

                            if(IN40 != 0) {
                                stmtAddBooking.setInt(84, IN40);
                                stmtAddBooking.setInt(85, QTYIN40);
                            }
                            else
                            {
                                stmtAddBooking.setNull(84, NULL);
                                stmtAddBooking.setNull(85, NULL);
                            }

                            if(IN41 != 0) {
                                stmtAddBooking.setInt(86, IN41);
                                stmtAddBooking.setInt(87, QTYIN41);
                            }
                            else
                            {
                                stmtAddBooking.setNull(86, NULL);
                                stmtAddBooking.setNull(87, NULL);
                            }

                            if(IN42 != 0) {
                                stmtAddBooking.setInt(88, IN42);
                                stmtAddBooking.setInt(89, QTYIN42);
                            }
                            else
                            {
                                stmtAddBooking.setNull(88, NULL);
                                stmtAddBooking.setNull(89, NULL);
                            }

                            if(IN43 != 0) {
                                stmtAddBooking.setInt(90, IN43);
                                stmtAddBooking.setInt(91, QTYIN43);
                            }
                            else
                            {
                                stmtAddBooking.setNull(90, NULL);
                                stmtAddBooking.setNull(91, NULL);
                            }

                            if(IN44 != 0) {
                                stmtAddBooking.setInt(92, IN44);
                                stmtAddBooking.setInt(93, QTYIN44);
                            }
                            else
                            {
                                stmtAddBooking.setNull(92, NULL);
                                stmtAddBooking.setNull(93, NULL);
                            }

                            if(IN45 != 0) {
                                stmtAddBooking.setInt(94, IN45);
                                stmtAddBooking.setInt(95, QTYIN45);
                            }
                            else
                            {
                                stmtAddBooking.setNull(94, NULL);
                                stmtAddBooking.setNull(95, NULL);
                            }

                            if(IN46 != 0) {
                                stmtAddBooking.setInt(96, IN46);
                                stmtAddBooking.setInt(97, QTYIN46);
                            }
                            else
                            {
                                stmtAddBooking.setNull(96, NULL);
                                stmtAddBooking.setNull(97, NULL);
                            }

                            if(IN47 != 0) {
                                stmtAddBooking.setInt(98, IN47);
                                stmtAddBooking.setInt(99, QTYIN47);
                            }
                            else
                            {
                                stmtAddBooking.setNull(98, NULL);
                                stmtAddBooking.setNull(99, NULL);
                            }

                            if(IN48 != 0) {
                                stmtAddBooking.setInt(100, IN48);
                                stmtAddBooking.setInt(101, QTYIN48);
                            }
                            else
                            {
                                stmtAddBooking.setNull(100, NULL);
                                stmtAddBooking.setNull(101, NULL);
                            }

                            if(IN49 != 0) {
                                stmtAddBooking.setInt(102, IN49);
                                stmtAddBooking.setInt(103, QTYIN49);
                            }
                            else
                            {
                                stmtAddBooking.setNull(102, NULL);
                                stmtAddBooking.setNull(103, NULL);
                            }

                            if(IN50 != 0) {
                                stmtAddBooking.setInt(104, IN50);
                                stmtAddBooking.setInt(105, QTYIN50);
                            }
                            else
                            {
                                stmtAddBooking.setNull(104, NULL);
                                stmtAddBooking.setNull(105, NULL);
                            }

                            if(IN51 != 0) {
                                stmtAddBooking.setInt(106, IN51);
                                stmtAddBooking.setInt(107, QTYIN51);
                            }
                            else
                            {
                                stmtAddBooking.setNull(106, NULL);
                                stmtAddBooking.setNull(107, NULL);
                            }

                            if(IN52 != 0) {
                                stmtAddBooking.setInt(108, IN52);
                                stmtAddBooking.setInt(109, QTYIN52);
                            }
                            else
                            {
                                stmtAddBooking.setNull(108, NULL);
                                stmtAddBooking.setNull(109, NULL);
                            }

                            if(IN53 != 0) {
                                stmtAddBooking.setInt(110, IN53);
                                stmtAddBooking.setInt(111, QTYIN53);
                            }
                            else
                            {
                                stmtAddBooking.setNull(110, NULL);
                                stmtAddBooking.setNull(111, NULL);
                            }

                            if(IN54 != 0) {
                                stmtAddBooking.setInt(112, IN54);
                                stmtAddBooking.setInt(113, QTYIN54);
                            }
                            else
                            {
                                stmtAddBooking.setNull(112, NULL);
                                stmtAddBooking.setNull(113, NULL);
                            }

                            if(IN55 != 0) {
                                stmtAddBooking.setInt(114, IN55);
                                stmtAddBooking.setInt(115, QTYIN55);
                            }
                            else
                            {
                                stmtAddBooking.setNull(114, NULL);
                                stmtAddBooking.setNull(115, NULL);
                            }

                            if(IN56 != 0) {
                                stmtAddBooking.setInt(116, IN56);
                                stmtAddBooking.setInt(117, QTYIN56);
                            }
                            else
                            {
                                stmtAddBooking.setNull(116, NULL);
                                stmtAddBooking.setNull(117, NULL);
                            }

                            if(IN57 != 0) {
                                stmtAddBooking.setInt(118, IN57);
                                stmtAddBooking.setInt(119, QTYIN57);
                            }
                            else
                            {
                                stmtAddBooking.setNull(118, NULL);
                                stmtAddBooking.setNull(119, NULL);
                            }

                            if(IN58 != 0) {
                                stmtAddBooking.setInt(120, IN58);
                                stmtAddBooking.setInt(121, QTYIN58);
                            }
                            else
                            {
                                stmtAddBooking.setNull(120, NULL);
                                stmtAddBooking.setNull(121, NULL);
                            }

                            if(IN59 != 0) {
                                stmtAddBooking.setInt(122, IN59);
                                stmtAddBooking.setInt(123, QTYIN59);
                            }
                            else
                            {
                                stmtAddBooking.setNull(122, NULL);
                                stmtAddBooking.setNull(123, NULL);
                            }

                            if(IN60 != 0) {
                                stmtAddBooking.setInt(124, IN60);
                                stmtAddBooking.setInt(125, QTYIN60);
                            }
                            else
                            {
                                stmtAddBooking.setNull(124, NULL);
                                stmtAddBooking.setNull(125, NULL);
                            }


                            if(IN61 != 0) {
                                stmtAddBooking.setInt(126, IN61);
                                stmtAddBooking.setInt(127, QTYIN61);
                            }
                            else
                            {
                                stmtAddBooking.setNull(126, NULL);
                                stmtAddBooking.setNull(127, NULL);
                            }

                            if(IN62 != 0) {
                                stmtAddBooking.setInt(128, IN62);
                                stmtAddBooking.setInt(129, QTYIN62);
                            }
                            else
                            {
                                stmtAddBooking.setNull(128, NULL);
                                stmtAddBooking.setNull(129, NULL);
                            }

                            if(IN63 != 0) {
                                stmtAddBooking.setInt(130, IN63);
                                stmtAddBooking.setInt(131, QTYIN63);
                            }
                            else
                            {
                                stmtAddBooking.setNull(130, NULL);
                                stmtAddBooking.setNull(131, NULL);
                            }

                            if(IN64 != 0) {
                                stmtAddBooking.setInt(132, IN64);
                                stmtAddBooking.setInt(133, QTYIN64);
                            }
                            else
                            {
                                stmtAddBooking.setNull(132, NULL);
                                stmtAddBooking.setNull(133, NULL);
                            }

                            if(IN65 != 0) {
                                stmtAddBooking.setInt(134, IN65);
                                stmtAddBooking.setInt(135, QTYIN65);
                            }
                            else
                            {
                                stmtAddBooking.setNull(134, NULL);
                                stmtAddBooking.setNull(135, NULL);
                            }

                            if(IN66 != 0) {
                                stmtAddBooking.setInt(136, IN66);
                                stmtAddBooking.setInt(137, QTYIN66);
                            }
                            else
                            {
                                stmtAddBooking.setNull(136, NULL);
                                stmtAddBooking.setNull(137, NULL);
                            }

                            if(IN67 != 0) {
                                stmtAddBooking.setInt(138, IN67);
                                stmtAddBooking.setInt(139, QTYIN67);
                            }
                            else
                            {
                                stmtAddBooking.setNull(138, NULL);
                                stmtAddBooking.setNull(139, NULL);
                            }

                            if(IN68 != 0) {
                                stmtAddBooking.setInt(140, IN68);
                                stmtAddBooking.setInt(141, QTYIN68);
                            }
                            else
                            {
                                stmtAddBooking.setNull(140, NULL);
                                stmtAddBooking.setNull(141, NULL);
                            }

                            if(IN69 != 0) {
                                stmtAddBooking.setInt(142, IN69);
                                stmtAddBooking.setInt(143, QTYIN69);
                            }
                            else
                            {
                                stmtAddBooking.setNull(142, NULL);
                                stmtAddBooking.setNull(143, NULL);
                            }

                            if(IN70 != 0) {
                                stmtAddBooking.setInt(144, IN70);
                                stmtAddBooking.setInt(145, QTYIN70);
                            }
                            else
                            {
                                stmtAddBooking.setNull(144, NULL);
                                stmtAddBooking.setNull(145, NULL);
                            }

                            if(IN71 != 0) {
                                stmtAddBooking.setInt(146, IN71);
                                stmtAddBooking.setInt(147, QTYIN71);
                            }
                            else
                            {
                                stmtAddBooking.setNull(146, NULL);
                                stmtAddBooking.setNull(147, NULL);
                            }

                            if(IN72 != 0) {
                                stmtAddBooking.setInt(148, IN72);
                                stmtAddBooking.setInt(149, QTYIN72);
                            }
                            else
                            {
                                stmtAddBooking.setNull(148, NULL);
                                stmtAddBooking.setNull(149, NULL);
                            }

                            if(IN73 != 0) {
                                stmtAddBooking.setInt(150, IN73);
                                stmtAddBooking.setInt(151, QTYIN73);
                            }
                            else
                            {
                                stmtAddBooking.setNull(150, NULL);
                                stmtAddBooking.setNull(151, NULL);
                            }

                            if(IN74 != 0) {
                                stmtAddBooking.setInt(152, IN74);
                                stmtAddBooking.setInt(153, QTYIN74);
                            }
                            else
                            {
                                stmtAddBooking.setNull(152, NULL);
                                stmtAddBooking.setNull(153, NULL);
                            }

                            if(IN75 != 0) {
                                stmtAddBooking.setInt(154, IN75);
                                stmtAddBooking.setInt(155, QTYIN75);
                            }
                            else
                            {
                                stmtAddBooking.setNull(154, NULL);
                                stmtAddBooking.setNull(155, NULL);
                            }

                            if(IN76 != 0) {
                                stmtAddBooking.setInt(156, IN76);
                                stmtAddBooking.setInt(157, QTYIN76);
                            }
                            else
                            {
                                stmtAddBooking.setNull(156, NULL);
                                stmtAddBooking.setNull(157, NULL);
                            }

                            if(IN77 != 0) {
                                stmtAddBooking.setInt(158, IN77);
                                stmtAddBooking.setInt(159, QTYIN77);
                            }
                            else
                            {
                                stmtAddBooking.setNull(158, NULL);
                                stmtAddBooking.setNull(159, NULL);
                            }

                            if(IN78 != 0) {
                                stmtAddBooking.setInt(160, IN78);
                                stmtAddBooking.setInt(161, QTYIN78);
                            }
                            else
                            {
                                stmtAddBooking.setNull(160, NULL);
                                stmtAddBooking.setNull(161, NULL);
                            }

                            if(IN79 != 0) {
                                stmtAddBooking.setInt(162, IN79);
                                stmtAddBooking.setInt(163, QTYIN79);
                            }
                            else
                            {
                                stmtAddBooking.setNull(162, NULL);
                                stmtAddBooking.setNull(163, NULL);
                            }

                            if(IN80 != 0) {
                                stmtAddBooking.setInt(164, IN80);
                                stmtAddBooking.setInt(165, QTYIN80);
                            }
                            else
                            {
                                stmtAddBooking.setNull(164, NULL);
                                stmtAddBooking.setNull(165, NULL);
                            }

                            if(IN81 != 0) {
                                stmtAddBooking.setInt(166, IN81);
                                stmtAddBooking.setInt(167, QTYIN81);
                            }
                            else
                            {
                                stmtAddBooking.setNull(166, NULL);
                                stmtAddBooking.setNull(167, NULL);
                            }

                            if(IN82 != 0) {
                                stmtAddBooking.setInt(168, IN82);
                                stmtAddBooking.setInt(169, QTYIN82);
                            }
                            else
                            {
                                stmtAddBooking.setNull(168, NULL);
                                stmtAddBooking.setNull(169, NULL);
                            }

                            if(IN83 != 0) {
                                stmtAddBooking.setInt(170, IN83);
                                stmtAddBooking.setInt(171, QTYIN83);
                            }
                            else
                            {
                                stmtAddBooking.setNull(170, NULL);
                                stmtAddBooking.setNull(171, NULL);
                            }

                            if(IN84 != 0) {
                                stmtAddBooking.setInt(172, IN84);
                                stmtAddBooking.setInt(173, QTYIN84);
                            }
                            else
                            {
                                stmtAddBooking.setNull(172, NULL);
                                stmtAddBooking.setNull(173, NULL);
                            }

                            if(IN85 != 0) {
                                stmtAddBooking.setInt(174, IN85);
                                stmtAddBooking.setInt(175, QTYIN85);
                            }
                            else
                            {
                                stmtAddBooking.setNull(174, NULL);
                                stmtAddBooking.setNull(175, NULL);
                            }

                            if(IN86 != 0) {
                                stmtAddBooking.setInt(176, IN86);
                                stmtAddBooking.setInt(177, QTYIN86);
                            }
                            else
                            {
                                stmtAddBooking.setNull(176, NULL);
                                stmtAddBooking.setNull(177, NULL);
                            }

                            if(IN87 != 0) {
                                stmtAddBooking.setInt(178, IN87);
                                stmtAddBooking.setInt(179, QTYIN87);
                            }
                            else
                            {
                                stmtAddBooking.setNull(178, NULL);
                                stmtAddBooking.setNull(179, NULL);
                            }

                            if(IN88 != 0) {
                                stmtAddBooking.setInt(180, IN88);
                                stmtAddBooking.setInt(181, QTYIN88);
                            }
                            else
                            {
                                stmtAddBooking.setNull(180, NULL);
                                stmtAddBooking.setNull(181, NULL);
                            }

                            if(IN89 != 0) {
                                stmtAddBooking.setInt(182, IN89);
                                stmtAddBooking.setInt(183, QTYIN89);
                            }
                            else
                            {
                                stmtAddBooking.setNull(182, NULL);
                                stmtAddBooking.setNull(183, NULL);
                            }

                            if(IN90 != 0) {
                                stmtAddBooking.setInt(184, IN90);
                                stmtAddBooking.setInt(185, QTYIN90);
                            }
                            else
                            {
                                stmtAddBooking.setNull(184, NULL);
                                stmtAddBooking.setNull(185, NULL);
                            }

                            if(IN91 != 0) {
                                stmtAddBooking.setInt(186, IN91);
                                stmtAddBooking.setInt(187, QTYIN91);
                            }
                            else
                            {
                                stmtAddBooking.setNull(186, NULL);
                                stmtAddBooking.setNull(187, NULL);
                            }

                            if(IN92 != 0) {
                                stmtAddBooking.setInt(188, IN92);
                                stmtAddBooking.setInt(189, QTYIN92);
                            }
                            else
                            {
                                stmtAddBooking.setNull(188, NULL);
                                stmtAddBooking.setNull(189, NULL);
                            }

                            if(IN93 != 0) {
                                stmtAddBooking.setInt(190, IN93);
                                stmtAddBooking.setInt(191, QTYIN93);
                            }
                            else
                            {
                                stmtAddBooking.setNull(190, NULL);
                                stmtAddBooking.setNull(191, NULL);
                            }

                            if(IN94 != 0) {
                                stmtAddBooking.setInt(192, IN94);
                                stmtAddBooking.setInt(193, QTYIN94);
                            }
                            else
                            {
                                stmtAddBooking.setNull(192, NULL);
                                stmtAddBooking.setNull(193, NULL);
                            }

                            if(IN95 != 0) {
                                stmtAddBooking.setInt(194, IN95);
                                stmtAddBooking.setInt(195, QTYIN95);
                            }
                            else
                            {
                                stmtAddBooking.setNull(194, NULL);
                                stmtAddBooking.setNull(195, NULL);
                            }

                            if(IN96 != 0) {
                                stmtAddBooking.setInt(196, IN96);
                                stmtAddBooking.setInt(197, QTYIN96);
                            }
                            else
                            {
                                stmtAddBooking.setNull(196, NULL);
                                stmtAddBooking.setNull(197, NULL);
                            }

                            if(IN97 != 0) {
                                stmtAddBooking.setInt(198, IN97);
                                stmtAddBooking.setInt(199, QTYIN97);
                            }
                            else
                            {
                                stmtAddBooking.setNull(198, NULL);
                                stmtAddBooking.setNull(199, NULL);
                            }

                            if(IN98 != 0) {
                                stmtAddBooking.setInt(200, IN98);
                                stmtAddBooking.setInt(201, QTYIN98);
                            }
                            else
                            {
                                stmtAddBooking.setNull(200, NULL);
                                stmtAddBooking.setNull(201, NULL);
                            }

                            if(IN99 != 0) {
                                stmtAddBooking.setInt(202, IN99);
                                stmtAddBooking.setInt(203, QTYIN99);
                            }
                            else
                            {
                                stmtAddBooking.setNull(202, NULL);
                                stmtAddBooking.setNull(203, NULL);
                            }

                            if(IN100 != 0) {
                                stmtAddBooking.setInt(204, IN100);
                                stmtAddBooking.setInt(205, QTYIN100);
                            }
                            else
                            {
                                stmtAddBooking.setNull(204, NULL);
                                stmtAddBooking.setNull(205, NULL);
                            }

                        stmtAddBooking.setString(206, Subtotal);
                        stmtAddBooking.setString(207, DeliveryFee);
                        stmtAddBooking.setString(208, TotalAmount);
                        stmtAddBooking.setString(209, "Paid");
                        stmtAddBooking.setString(210, DeliveryType);

                        stmtAddBooking.executeUpdate();
                        isSuccess = true;

                      }
                      catch(SQLException ex)
                      {
                         isSuccess=false;
                         z = ex.getMessage();
                         return z;
                      }
                    }

                //}
            }catch(Exception ex)
            {
                isSuccess=false;
                z = ex.getMessage();
                return z;
            }

            return z;
        }

    }


}

