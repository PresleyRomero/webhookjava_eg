
package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import org.json.*;

public class Modelwh {
    
    Connection conn = null;

    public Modelwh() throws Exception {
        try{
            Conexion cx = new Conexion();
            this.conn = cx.obtenerconexion();
        }catch(Exception $e){
            throw $e;	
        }
    }
    
    public boolean insertar(String tableparent, JSONObject geojsonparent, JSONArray repetibles) throws Exception{  
        try{            
            conn.setAutoCommit(false); 
            
            //# Insert Parent record  
            JSONObject properties = geojsonparent.getJSONArray("features").getJSONObject(0).getJSONObject("properties");
            properties.remove("marker-color");            
            insertRecord(tableparent, properties);
            
            //# Insert Children records (repetibles)
            for (int r = 0; r < repetibles.length(); r++) {
                JSONObject repetible = repetibles.getJSONObject(r);
                String tablechild = repetible.getString("table");
                JSONArray geojsons = repetible.getJSONArray("geojsons");
                for (int i = 0; i < geojsons.length(); i++) {
                    JSONObject propertieschild = geojsons.getJSONObject(i).getJSONArray("features").getJSONObject(0).getJSONObject("properties");
                    propertieschild.remove("marker-color");
                    insertRecord(tablechild, propertieschild);                    
                }
            } 
            conn.commit();
            return true;
        }catch (Exception e){
            if (conn!=null) conn.rollback();
            throw e;
        }finally{
            try{
                if(conn!=null) conn.close();
            }catch(Exception e){}
        }
    }
    
    public boolean modificar(String tableparent, JSONObject geojsonparent, String fulcrum_parent_id, JSONArray repetibles) throws Exception{ 
        try{
            conn.setAutoCommit(false);  
            
            //# Update Parent record 
            JSONObject properties = geojsonparent.getJSONArray("features").getJSONObject(0).getJSONObject("properties");
            properties.remove("marker-color");            
            updateRecord(tableparent, properties, fulcrum_parent_id);
            
            //# Delete old Children records (repetibles)
            for (int r = 0; r < repetibles.length(); r++) {
                String tablechild = repetibles.getJSONObject(r).getString("table");
                deleteChildren(tablechild, fulcrum_parent_id);
            } 
            
            //# Insert new Children records (repetibles)
            for (int r = 0; r < repetibles.length(); r++) {
                JSONObject repetible = repetibles.getJSONObject(r);
                String tablechild = repetible.getString("table");
                JSONArray geojsons = repetible.getJSONArray("geojsons");
                for (int i = 0; i < geojsons.length(); i++) {
                    JSONObject propertieschild = geojsons.getJSONObject(i).getJSONArray("features").getJSONObject(0).getJSONObject("properties");
                    propertieschild.remove("marker-color");
                    insertRecord(tablechild, propertieschild);                    
                }
            }             
            conn.commit();
            return true;
        }catch (Exception e){
            if (conn!=null) conn.rollback();
            throw e;
        }finally{
            try{
                if(conn!=null) conn.close();
            }catch(Exception e){}
        }
    }
    
    public boolean eliminar(String tableparent, String fulcrum_parent_id, String[] tablesrepet) throws Exception{ 
        try{
            conn.setAutoCommit(false); 
            //# Delete Children records (repetibles)
            for (int r = 0; r < tablesrepet.length; r++) {
                deleteChildren(tablesrepet[r], fulcrum_parent_id);
            }             
            //# Delete Parent record 
            deleteRecord(tableparent, fulcrum_parent_id);           
            conn.commit();
            return true;
        }catch (Exception e){
            if (conn!=null) conn.rollback();
            throw e;
        }finally{
            try{
                if(conn!=null) conn.close();
            }catch(Exception e){}
        }
    }
    
    ///**************
    
    public boolean insertRecord(String table, JSONObject properties) throws Exception{  
        PreparedStatement pstm=null;
        String[] fields = JSONObject.getNames(properties); 
        String params = new String(new char[fields.length-1]).replace("\0", "?,") + "?"; // forma cadena de parametros anonimos (?,?,?...)
        try{
            String sql = "INSERT INTO "+table+" (" + String.join(", ", fields) + ") VALUES (" + params  + ")";
            pstm = conn.prepareStatement(sql);
            int cont = 1;
            for (String fieldname : fields){
                pstm.setString(cont, properties.get(fieldname).toString());
                cont++;
            }
            pstm.execute();
            return true;
        }catch (Exception e){
            throw e;
        }finally{
            try{
                if(pstm!=null) pstm.close();
            }catch(Exception e){}
        }
    }
    
    public boolean updateRecord(String table, JSONObject properties, String fulcrum_id) throws Exception{ 
        PreparedStatement pstm=null;        
        properties.remove("fulcrum_id");
        String[] fields = JSONObject.getNames(properties); 
        try{
            String sql = "UPDATE "+table+" SET " + String.join("=?, ", fields) + "=? WHERE fulcrum_id = ?";
            pstm = conn.prepareStatement(sql);
            int cont = 1;
            for (String fieldname : fields){
                pstm.setString(cont, properties.get(fieldname).toString());
                cont++;
            }
            pstm.setString(fields.length+1, fulcrum_id);
            pstm.execute();
            return true;
        }catch (Exception e){
            throw e;
        }finally{
            try{
                if(pstm!=null) pstm.close();
            }catch(Exception e){}
        }
    }
    
    public boolean deleteRecord(String table, String fulcrum_id) throws Exception{ 
        PreparedStatement pstm=null;        
        try{            
            String sql = "DELETE FROM "+table+" WHERE fulcrum_id = ?";
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, fulcrum_id);
            pstm.execute();  
            return true;
        }catch (Exception e){
            throw e;
        }finally{
            try{
                if(pstm!=null) pstm.close();
            }catch(Exception e){}
        }
    }
    
    public boolean deleteChildren(String table, String fulcrum_parent_id) throws Exception{ 
        PreparedStatement pstm=null;        
        try{            
            String sql = "DELETE FROM "+table+" WHERE fulcrum_parent_id = ?";
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, fulcrum_parent_id);
            pstm.execute(); 
            return true;
        }catch (Exception e){
            throw e;
        }finally{
            try{
                if(pstm!=null) pstm.close();
            }catch(Exception e){}
        }
    }
    
}
