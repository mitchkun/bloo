package com.elegance.bloo;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by h-pc on 16-Oct-15.
 */
public class ConnectionClass {
    // Declaring Server ip, username, database name and password
    String ip = "173.212.233.244";
    String db = "Bloo";
    String un = "sa";
    String pass = "Infinitech2019";

    //******VPS******//

    //String ip = "10.0.2.2";
    //String pass = "Sethu2012";
    //ip = "173.212.233.244";
    //pass = "Infinitech2019";

    @SuppressLint("NewApi")
    public Connection CONN()
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String ConnectionURL = null;
        try
        {
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();

            connection = DriverManager.getConnection("jdbc:jtds:sqlserver://"+ip+":1433/"+db+"; user="+un+"; password="+pass, un,pass);
        }
        catch (SQLException se)
        {
            Log.e("error here 1 : ", se.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            Log.e("error here 2 : ", e.getMessage());
        }
        catch (Exception e)
        {
            Log.e("error here 3 : ", e.getMessage());
        }
        return connection;
    }
}
