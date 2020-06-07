package model;

import java.sql.Connection;
import java.sql.DriverManager;

public class Conexion {
    
    public Connection obtenerconexion() throws Exception{      
        Connection conn=null;    
        //# MySQL database connection info
        String host = "zzzzzzzzzz";
        String user = "zzzzz";
        String pass = "zzzzz";
        String db = "zzzzz";  
        
        try{
            String url="jdbc:mysql://"+host+"/"+db;
            Class.forName("com.mysql.jdbc.Driver");
            conn=DriverManager.getConnection(url,user,pass);
        }catch(Exception e){            
            throw e;
        }
        return conn;
    } 
    
}
