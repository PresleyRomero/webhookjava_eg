
package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class Mauxiliar {
    
    Connection conn = null;

    public Mauxiliar() throws Exception {
        try{
            Conexion cx = new Conexion();
            this.conn = cx.obtenerconexion();
        }catch(Exception $e){
            throw $e;	
        }
    }
    
    public boolean insertDataFromWH(String data) throws SQLException {
        PreparedStatement pstm = null;
        try {
            conn.setAutoCommit(false);            
            String sql = "INSERT INTO datawebhook (code, name, description) VALUES ('', 'data from evento', ?) ";
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, data);
            pstm.execute();         
            conn.commit();
            return true;
        }catch(Exception e) {
            if (conn!=null) conn.rollback();
            throw e;
        }finally{
            try{
                if(pstm!=null) pstm.close();
                if(conn!=null) conn.close();
            }catch(Exception e){}
        }       
    }
    
    public boolean insertExceptions(String message) throws SQLException {
        PreparedStatement pstm = null;
        try {
            conn.setAutoCommit(false);
            String sql = "INSERT INTO catch (code, name, description) VALUES ('', 'catched', ?) ";
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, message);
            pstm.execute();        
            conn.commit();
            return true;
        }catch(Exception e) {
            if (conn!=null) conn.rollback();
            System.out.println("Falló en método insertExceptions: "+e.getMessage());
            throw e;
        }finally{
            try{
                if(pstm!=null) pstm.close();
                if(conn!=null) conn.close();
            }catch(Exception e){}
        }          
    }
    
}
