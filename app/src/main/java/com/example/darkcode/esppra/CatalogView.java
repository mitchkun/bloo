package com.example.darkcode.esppra;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.res.Configuration;


import static com.example.darkcode.esppra.PersistentData.ArryCartTextViewIds;
import static com.example.darkcode.esppra.PersistentData.ArryShoppingCartItemBrand;
import static com.example.darkcode.esppra.PersistentData.ArryShoppingCartItemDescription;
import static com.example.darkcode.esppra.PersistentData.ArryShoppingCartItemNo;
import static com.example.darkcode.esppra.PersistentData.ArryShoppingCartItemPrice;

public class CatalogView extends AppCompatActivity {


    int fID;

    private static ConnectionClass connectionClass;

    public int findUnusedId() {
        while(findViewById(++fID) != null );
        return fID;
    } //for view id generation

    ProgressBar progS;

    String un, pass, db, ip, StoreType;
    Connection con;


    int currentStoreNo;


    int Dispwidth;
    int DispHeight;

    ResultSet rsItems = null;

    byte[] RItemPic = null;

    ImageView pbxBanner;

    LinearLayout CatLayout;
    LinearLayout CartLayout;

    ScrollView svCatalogue;
    ScrollView svShoppingCart;

    ImageView checkOut, removeItem;


    int MaxItemsNo;


    boolean executionComplete;
    boolean totalNoOfItemsReached;



    TextView txtSubTotal;
    public static ArrayList<Integer> ItemTemplateList = new ArrayList<>();

    public static ArrayList<Integer> ArryItemsAlreadyPainted = new ArrayList<>();
    Typeface tnrBold;
    Typeface tnrRegular;

    int lastGotItemNo;

    String storeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_view);

        String languageToLoad  = "en"; // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        connectionClass = new ConnectionClass();

        removeItem = findViewById(R.id.pbxRemoveItem);

        int indexOfCurrentStore = PersistentData.ArryStoreNo.get(currentStoreNo);
        storeName = PersistentData.ArryStoreName.get(indexOfCurrentStore);

        lastGotItemNo = 0;
        fID = PersistentData.lastfID;
        //Bold
        tnrBold = Typeface.createFromAsset(getAssets(), "timesbd.ttf");
        //Regular
        tnrRegular = Typeface.createFromAsset(getAssets(), "times.ttf");

         checkOut = findViewById(R.id.pbxCheckout);
        ArryItemsAlreadyPainted.clear();

        svCatalogue = findViewById(R.id.svCatalogue);
        svShoppingCart = findViewById(R.id.svShoppingCart);

        currentStoreNo =  getIntent().getIntExtra("currentStoreNo",1);
        StoreType = getIntent().getStringExtra("storeType");
        storeName = getIntent().getStringExtra("storeName");

        Display display = getWindowManager().getDefaultDisplay();
        Dispwidth = (display.getWidth() / 2) - 10; // ((display.getWidth()*20)/100)
        DispHeight = Dispwidth;
        int bannerHeight = display.getWidth() / 3;

        svCatalogue.getLayoutParams().width = (display.getWidth()/2);
        svShoppingCart.getLayoutParams().width = (display.getWidth()/2);

        svCatalogue.getLayoutParams().width = Dispwidth;
        svCatalogue.setPadding(10, 0, 20, 10);
        svCatalogue.requestLayout();

        svCatalogue.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged()
            {
                int rootHeight = svCatalogue.getHeight();
                int childHeight = svCatalogue.getChildAt(0).getHeight();
                int scrollY = svCatalogue.getScrollY();
                if (((childHeight + 10) - rootHeight) == scrollY)
                { //- 10 for bottom padding
                    {
                        if (executionComplete = true) {
                            LoadCatalogsProgressively LoadData = new LoadCatalogsProgressively();
                            LoadData.execute("");
                        }
                    }
                }
            }
        });


        progS = findViewById(R.id.progS);
        CatLayout = findViewById(R.id.CatLayout);
        CartLayout = findViewById(R.id.CartLayout);

        pbxBanner = findViewById(R.id.pbxBanner);
        pbxBanner.getLayoutParams().height =  bannerHeight;


        AnimationDrawable animation = PersistentData.animation;
        pbxBanner.setBackgroundDrawable(animation);

        //Mobile Money Checkout
        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PersistentData.ArryShoppingCartItemNo.size() > 0) {
                    Intent intentCheckOut = new Intent(CatalogView.this, CheckOutActivity.class);
                    intentCheckOut.putExtra("subTotal", txtSubTotal.getText());
                    startActivity(intentCheckOut);
                    finish();
                }
                else
                {
                    Toast.makeText(getBaseContext(), "No items in shopping cart.", Toast.LENGTH_LONG).show();
                }
            }
        });

        removeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Remove from cart
                int currentNoOfChildren = CartLayout.getChildCount();
                if (currentNoOfChildren > 0)
                {
                    CartLayout.removeView(CartLayout.getChildAt(currentNoOfChildren - 1));

                    //Deduct price from Total price
                    String strcurrentSubTotal = String.valueOf(txtSubTotal.getText());
                    String strItemPrice = PersistentData.ArryShoppingCartItemPrice.get(ArryShoppingCartItemPrice.size() - 1);
                    double currentSubTotal = Double.parseDouble(strcurrentSubTotal);
                    double itemPrice = Double.parseDouble(strItemPrice);

                    double newSubTotal = currentSubTotal - itemPrice;
                    PersistentData.totalPrice = newSubTotal;
                    PersistentData.currentSubTotal = newSubTotal;

                    txtSubTotal.setText(String.format("%.2f", new BigDecimal(newSubTotal)));
                    CartLayout.requestLayout();

                    //Remove from Shopping cart array
                    PersistentData.ArryShoppingCartItemNo.remove(ArryShoppingCartItemNo.size() - 1);
                    PersistentData.ArryShoppingCartItemBrand.remove(ArryShoppingCartItemBrand.size() - 1);
                    PersistentData.ArryShoppingCartItemDescription.remove(ArryShoppingCartItemDescription.size() - 1);
                    PersistentData.ArryShoppingCartItemPrice.remove(ArryShoppingCartItemPrice.size() - 1);
                }
            }
        });

        txtSubTotal = findViewById(R.id.txtSubTotal);

        double st = PersistentData.currentSubTotal;
        String.format("%.2f", new BigDecimal(st));

        txtSubTotal.setText(String.format("%.2f", new BigDecimal(st)));

        loadCurrentCartItems();

        LoadMaxItemsNo LMIN = new LoadMaxItemsNo();
        LMIN.execute("");

        LoadCatalogsProgressively LCP = new LoadCatalogsProgressively();
        LCP.execute("");
    }

    private void loadCurrentCartItems()
    {
        if(PersistentData.ArryShoppingCartItemNo.size() > 0)
        {
            for(int r = 0; r < PersistentData.ArryShoppingCartItemNo.size(); r++)
            {

                TextView txtNewCartItemInfo = new TextView(CatalogView.this);
                txtNewCartItemInfo.setTextColor(Color.BLACK);
                txtNewCartItemInfo.setText("\n"+ String.valueOf(PersistentData.ArryShoppingCartItemBrand.get(r))+" "+String.valueOf(PersistentData.ArryShoppingCartItemDescription.get(r)) +"@"+String.valueOf(PersistentData.ArryShoppingCartItemPrice.get(r)));
                txtNewCartItemInfo.setTypeface(tnrRegular);

                CartLayout.addView(txtNewCartItemInfo);

                String strcurrentSubTotal = String.valueOf(txtSubTotal.getText());
                String strItemPrice = PersistentData.ArryPrice.get(r);
                PersistentData.currentSubTotal = Double.parseDouble(strcurrentSubTotal);
                double itemPrice = Double.parseDouble(strItemPrice);

                /*double newSubTotal = PersistentData.currentSubTotal + itemPrice;

                txtSubTotal.setText(String.format("%.2f", new BigDecimal(newSubTotal)));*/
            }
        }
    }

    public class LoadMaxItemsNo extends AsyncTask<String, Integer, String> {
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
                    String cmdGetMaxItemsNo = "Select Count([ItemNo.]) from [Items] where [StoreNo.] = "+ currentStoreNo +"and [Live] = 'Y'";
                    Statement stmtMaxItemsNo = con.createStatement();
                    ResultSet rsMaxItemsNo = stmtMaxItemsNo.executeQuery(cmdGetMaxItemsNo);

                    while (rsMaxItemsNo.next()) {
                        //Store No
                        try
                        {
                            MaxItemsNo = rsMaxItemsNo.getInt(1);
                        } catch (Exception ex)
                        {
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

    public class LoadCatalogsProgressively extends AsyncTask<String, String, String> {
        String z = "";
        Boolean isSuccess = false;
       int itemCount;

        @Override
        protected void onPreExecute()
        {
            progS.setVisibility(View.VISIBLE);
            executionComplete = false;
        }

        @Override
        protected void onPostExecute(String r)
        {

            if (totalNoOfItemsReached == false)
            {
                if (PersistentData.ArryItemNo.size() == 0) {
                    Toast.makeText(CatalogView.this, "No items(s) currently available.", Toast.LENGTH_LONG).show();
                    executionComplete = true;
                }
                else
                    {
                    if (PersistentData.ArryItemNo.size() > 0) {
                        int totalNewItemsTobePainted = PersistentData.ArryItemNo.size() - (ItemTemplateList.size() / 2);

                        for (int t = PersistentData.ArryItemNo.size() - totalNewItemsTobePainted; t < PersistentData.ArryItemNo.size(); t++) {
                            if (PersistentData.ArryItemStoreNo.get(t) == currentStoreNo) {
                                ImageView image = null;
                                //Populate views from arrays
                                image = new ImageView(CatalogView.this);

                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Dispwidth, DispHeight);
                                image.setLayoutParams(layoutParams);

                                try {
                                    RItemPic = android.util.Base64.decode(PersistentData.ArryItemThumbnail.get(t), android.util.Base64.DEFAULT);

                                    Bitmap ArtImage = BitmapFactory.decodeByteArray(RItemPic, 0, RItemPic.length);
                                    image.setImageBitmap(ArtImage);
                                    image.setBackgroundColor(Color.parseColor("#ffffff"));
                                    image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                } catch (Exception ex) {

                                    //SupersparLogo
                                    if(storeName.compareTo("Superspar") == 0)
                                    {
                                        image.setImageResource(R.drawable.noimagespar);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //PicknPayLogo
                                    if(storeName.compareTo("PicknPay") == 0)
                                    {
                                        image.setImageResource(R.drawable.noimagepicknpay);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //BuynSaveSparLogo
                                    if(storeName.compareTo("BuynSave Spar") == 0)
                                    {
                                        image.setImageResource(R.drawable.no_image_buynsave);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //ShopriteLogo
                                    if(storeName.compareTo("Shoprite") == 0)
                                    {
                                        image.setImageResource(R.drawable.noimageshoprite);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //USaveLogo
                                    if(storeName.compareTo("USave") == 0)
                                    {
                                        image.setImageResource(R.drawable.noimageusave);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //SaveRiteLogo
                                    if(storeName.compareTo("SaveRite") == 0)
                                    {
                                        image.setImageResource(R.drawable.noimagesaverite);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //SpurLogo
                                    if(storeName.compareTo("Spur") == 0)
                                    {
                                        image.setImageResource(R.drawable.noimagespur);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //OceanBasketLogo
                                    if(storeName.compareTo("Ocean Basket") == 0)
                                    {
                                        image.setImageResource(R.drawable.noimageoceanbasket);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //GalitosLogo
                                    if(storeName.compareTo("Pizza Inn") == 0)
                                    {
                                        image.setImageResource(R.drawable.noimagegalitos);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //PizzaInnLogo
                                    if(storeName.compareTo("Debonairs") == 0)
                                    {
                                        image.setImageResource(R.drawable.no_image_debonairs);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //DebonairsLogo
                                    if(storeName.compareTo("Steers") == 0)
                                    {
                                        image.setImageResource(R.drawable.noimagesteers);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //SteersLogo
                                    if(storeName.compareTo("KFC") == 0)
                                    {
                                        image.setImageResource(R.drawable.noimagekfc);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //KFCLogo
                                    if(storeName.compareTo("Nandos") == 0)
                                    {
                                        image.setImageResource(R.drawable.noimagenandos);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //NandosLogo
                                    if(storeName.compareTo("Hungry Lion") == 0)
                                    {
                                        image.setImageResource(R.drawable.noimagenandos);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //KingPieLogo
                                    if(storeName.compareTo("King Pie") == 0)
                                    {
                                        image.setImageResource(R.drawable.noimagekingpie);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //OKFoodsLogo
                                    if(storeName.compareTo("OK Food") == 0)
                                    {
                                        image.setImageResource(R.drawable.noimageokfoods);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //ClicksLogo
                                    if(storeName.compareTo("Clicks") == 0)
                                    {
                                        image.setImageResource(R.drawable.noimageclicks);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //HungryLionLogo
                                    if(storeName.compareTo("Hungry") == 0)
                                    {
                                        image.setImageResource(R.drawable.noimagehungrylion);
                                        image.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    //
                                }

                                int imageGeneratedId = findUnusedId();
                                int addToCatGeneratedId = findUnusedId();

                                ItemTemplateList.add(imageGeneratedId);
                                ItemTemplateList.add(addToCatGeneratedId);


                                //Price
                                TextView txtPrice = null;
                                txtPrice = new TextView(CatalogView.this);
                                txtPrice.setTextColor(Color.BLACK);
                                txtPrice.setText("E" + String.valueOf(PersistentData.ArryPrice.get(t)));
                                //Brand
                                TextView txtBrand = null;
                                txtBrand = new TextView(CatalogView.this);
                                txtPrice.setTextColor(Color.BLACK);
                                txtBrand.setText(String.valueOf(PersistentData.ArryBrand.get(t)));

                                //Description
                                TextView txtDescription = null;
                                txtDescription = new TextView(CatalogView.this);
                                txtPrice.setTextColor(Color.BLACK);
                                txtDescription.setText(String.valueOf(PersistentData.ArryDescription.get(t)));

                                //addToCart
                                ImageView imageAddToCart = null;
                                imageAddToCart = new ImageView(CatalogView.this);


                                Bitmap addToCartBitmap = BitmapFactory.decodeResource(getBaseContext().getResources(),
                                        R.drawable.add_to_cart);
                                imageAddToCart.setImageBitmap(addToCartBitmap);
                                imageAddToCart.setScaleType(ImageView.ScaleType.CENTER_INSIDE);


                                image.setId(imageGeneratedId);
                                imageAddToCart.setId(addToCatGeneratedId);


                                //Image
                                if (t == 0) {
                                    CatLayout.addView(image);
                                    LinearLayout.LayoutParams paramsImage = (LinearLayout.LayoutParams) image.getLayoutParams();
                                    paramsImage.setMargins(5, 5, 5, 5);
                                    image.setLayoutParams(paramsImage);

                                }
                                if (t > 0) {
                                    CatLayout.addView(image);
                                    LinearLayout.LayoutParams paramsImage = (LinearLayout.LayoutParams) image.getLayoutParams();
                                    paramsImage.setMargins(5, 15, 5, 5);
                                    image.setLayoutParams(paramsImage);

                                }

                                //Price
                                CatLayout.addView(txtPrice);
                                LinearLayout.LayoutParams paramsPrice = (LinearLayout.LayoutParams) txtPrice.getLayoutParams();
                                paramsPrice.setMargins(10, 5, 5, 5);
                                txtPrice.setLayoutParams(paramsPrice);
                                txtPrice.setTypeface(tnrBold);
                                txtPrice.setTextSize(22);

                                //Brand
                                CatLayout.addView(txtBrand);
                                LinearLayout.LayoutParams paramsBrand = (LinearLayout.LayoutParams) txtPrice.getLayoutParams();
                                paramsBrand.setMargins(5, 5, 5, 5);
                                txtBrand.setLayoutParams(paramsBrand);
                                txtBrand.setTypeface(tnrBold);
                                txtBrand.setTextSize(16);

                                //Description
                                CatLayout.addView(txtDescription);
                                LinearLayout.LayoutParams paramsDescription = (LinearLayout.LayoutParams) txtDescription.getLayoutParams();
                                paramsDescription.setMargins(5, 5, 5, 5);
                                txtDescription.setLayoutParams(paramsDescription);
                                txtDescription.setTypeface(tnrRegular);

                                //AddToCart
                                CatLayout.addView(imageAddToCart);
                                LinearLayout.LayoutParams paramsAddToCart = (LinearLayout.LayoutParams) image.getLayoutParams();
                                paramsAddToCart.setMargins(5, 5, 5, 5);
                                imageAddToCart.setLayoutParams(paramsAddToCart);

                                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(150, 100);
                                imageAddToCart.setLayoutParams(layoutParams2);

                                final int finalT = t;
                                imageAddToCart.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        PersistentData.ArryShoppingCartItemNo.add(PersistentData.ArryItemNo.get(finalT));
                                        PersistentData.ArryShoppingCartItemBrand.add(PersistentData.ArryBrand.get(finalT));
                                        PersistentData.ArryShoppingCartItemDescription.add(PersistentData.ArryDescription.get(finalT));
                                        PersistentData.ArryShoppingCartItemPrice.add(PersistentData.ArryPrice.get(finalT));

                                        TextView txtNewCartItemInfo = new TextView(CatalogView.this);
                                        txtNewCartItemInfo.setTextColor(Color.BLACK);
                                        txtNewCartItemInfo.setText("\n" + String.valueOf(PersistentData.ArryBrand.get(finalT)) + " " + String.valueOf(PersistentData.ArryDescription.get(finalT)) + "@" + String.valueOf(PersistentData.ArryPrice.get(finalT)));
                                        txtNewCartItemInfo.setTypeface(tnrRegular);

                                        CartLayout.addView(txtNewCartItemInfo);
                                        ArryCartTextViewIds.add(txtNewCartItemInfo.getId());

                                        String strcurrentSubTotal = String.valueOf(txtSubTotal.getText());
                                        //if (strcurrentSubTotal.compareTo())
                                        //{

                                        //}
                                        String strItemPrice = PersistentData.ArryPrice.get(finalT);
                                        double currentSubTotal = Double.parseDouble(strcurrentSubTotal);
                                        double itemPrice = Double.parseDouble(strItemPrice);

                                        double newSubTotal = currentSubTotal + itemPrice;
                                        PersistentData.totalPrice = newSubTotal;
                                        PersistentData.currentSubTotal = newSubTotal;

                                        txtSubTotal.setText(String.format("%.2f", new BigDecimal(newSubTotal)));

                                    }
                                });

                            }

                            if (MaxItemsNo == PersistentData.ArryStoreNo.size()) {
                                totalNoOfItemsReached = true;
                            }
                        }

                    }
                    executionComplete = true;
                }
                progS.setVisibility(View.INVISIBLE);

            }
        }

        @Override
        protected String doInBackground(String... strings)
        {

            try {
                Connection con = connectionClass.CONN();

                String cmdGetItems = "Select Top(10) [ItemNo.],[Category],[Brand],[Description],[Price],[StoreNo.],[Thumbnail] from [Items] where [StoreNo.] = " + currentStoreNo + " And [ItemNo.] > " + lastGotItemNo;
                if (totalNoOfItemsReached == false) {

                    try {
                        Statement stmtLoadItems = con.createStatement();
                        rsItems = null;
                        rsItems = stmtLoadItems.executeQuery(cmdGetItems);
                    } catch (Exception x2) {
                        isSuccess = false;
                        z = x2.getMessage();
                        return z;
                    }

                    while (rsItems.next()) {
                        //Store No
                        Integer ItemNo = 0;
                        try {
                            ItemNo = rsItems.getInt(1);
                        } catch (Exception ex) {
                            //Do Nothing
                        }
                        //Category;
                        String Category = null;
                        try {
                            Category = rsItems.getString(2);
                        } catch (Exception ex) {
                            //Do Nothing
                        }
                        //Brand
                        String Brand = null;
                        Brand = rsItems.getString(3);

                        //Description;
                        String Description = "";
                        Description = rsItems.getString(4);

                        //Price;
                        String Price = "";
                        Price = rsItems.getString(5);

                        //StoreNo;
                        Integer ItemStoreNo = 0;
                        ItemStoreNo = rsItems.getInt(6);

                        //Thumbnail;
                        String Thumbnail = "";
                        Thumbnail = rsItems.getString(7);

                        Boolean itemAlreadyAdded = false;
                        for (int s = 0; s < PersistentData.ArryItemNo.size(); s++) {
                            if (ItemNo == PersistentData.ArryItemNo.get(s)) {
                                itemAlreadyAdded = true;
                            }
                        }
                        if (itemAlreadyAdded == false) {
                            PersistentData.ArryItemNo.add(ItemNo);
                            PersistentData.ArryCategory.add(Category);
                            PersistentData.ArryBrand.add(Brand);
                            PersistentData.ArryDescription.add(Description);
                            PersistentData.ArryPrice.add(Price);
                            PersistentData.ArryItemStoreNo.add(ItemStoreNo);
                            PersistentData.ArryItemThumbnail.add(Thumbnail);
                        }

                    }

                    if (PersistentData.ArryItemNo.size() > 0) {
                        lastGotItemNo = PersistentData.ArryItemNo.get(PersistentData.ArryItemNo.size() - 1);
                    } else {
                        lastGotItemNo = 0;
                    }
                }

                }catch(Exception ex)
                {
                    isSuccess = false;
                    z = ex.getMessage();
                }

            return z;
        }
    }
}
