package com.elegance.bloo;

import android.graphics.drawable.AnimationDrawable;

import java.util.ArrayList;

public class PersistentData{
    // public id generator int
    public static int lastfID;

    public static boolean callpermissiongranted = false;
    public static boolean smspermissiongranted = false;
    public static final int CALL_PERMISSION_REQUEST_CODE = 123;
    public static final int SMS_PERMISSION_REQUEST_CODE = 124;
    public static boolean paymentConfirmed = false;

    //Animation
    public static AnimationDrawable animation = new AnimationDrawable();

    public static ArrayList<Integer> ArryCartTextViewIds = new ArrayList<>();

    public static ArrayList<Integer> ArryStoreNo = new ArrayList<>();
    public static ArrayList<String> ArryStoreName = new ArrayList<>();
    public static ArrayList<String> ArryCity =  new ArrayList<>();
    public static ArrayList<String> ArryLogo = new ArrayList<>();
    public static ArrayList<String> ArryType = new ArrayList<>();

    public static ArrayList<Integer> ArryPNo = new ArrayList<>();
    public static ArrayList<String> ArryArea = new ArrayList<>();
    public static ArrayList<String> ArryDeliveryPrice = new ArrayList<>();
    public static ArrayList<String> ArryDeliveryType = new ArrayList<>();


    public static ArrayList<Integer> ArryItemNo = new ArrayList<>();
    public static ArrayList<String> ArryCategory = new ArrayList<>();
    public static ArrayList<String> ArryBrand = new ArrayList<>();
    public static ArrayList<String> ArryDescription = new ArrayList<>();
    public static ArrayList<String> ArryPrice = new ArrayList<>();
    public static ArrayList<Integer> ArryItemStoreNo = new ArrayList<>();
    public static ArrayList<String> ArryItemImage = new ArrayList<>();
    public static ArrayList<String> ArryItemThumbnail = new ArrayList<>();

    public static ArrayList<Integer> ArryShoppingCartItemNo = new ArrayList<>();
    public static ArrayList<String> ArryShoppingCartItemPrice = new ArrayList<>();
    public static ArrayList<String> ArryShoppingCartItemBrand = new ArrayList<>();
    public static ArrayList<String> ArryShoppingCartItemDescription  = new ArrayList<>();

    public static double currentSubTotal;
    public static double totalPrice;

}
