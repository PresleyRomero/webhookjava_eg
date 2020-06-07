
// *** E.G WEBHOOK FOR "FORM EMPRESAS" WITH 2 REPETIBLES ("SUCURSALES" AND "TRABAJADORES" ) *** //

package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Mauxiliar;
import model.Modelwh;
import org.json.*;//JSONObject;

@WebServlet(name = "Controllerwh", urlPatterns = {"/Controllerwh"})

public class Controllerwh extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {            
            //# Webhook JSON payload 
            String data = request.getReader().lines().collect(Collectors.joining(System.lineSeparator())); // POST data from webhook
            JSONObject payload = new JSONObject(data);            
            //-- insert in bbdd POST data from webhook (option to see repetibles code)
            Mauxiliar mauxliar = new Mauxiliar(); 
            mauxliar.insertDataFromWH(data);
            
            String formID  = payload.getJSONObject("data").getString("form_id");
            
            switch(formID){
                //# Form Empresas 
                case "zzzzzzzz-cb75-4f73-93e9-zzzzzzzzzzzz": // FORM_ID: info fulcrum app                              
                    //String shareToken = "zzzzd615de63zzzz";// info fulcrum app
                    String datashareurl = "https://web.fulcrumapp.com/shares/zzzzd615de63zzzz.geojson";// Data Share (.geojson): info fulcrum app
                    String table = "empresa"; // info db
                    String[] tablesrepet = {"trabajador", "sucursal"}; // info db

                    //# Repetibles info
                    JSONObject repet0 = new JSONObject();
                    repet0  .put("table", tablesrepet[0])
                            .put("payloadcode", "4f8f") // info payload
                            .put("urlshare", "https://web.fulcrumapp.com/shares/zzzzd615de63zzzz.geojson?child=trabajadores"); // Data Share for repetible 1 (.geojson): info fulcrum app

                    JSONObject repet1 = new JSONObject();
                    repet1  .put("table", tablesrepet[1])
                            .put("payloadcode", "3d87") // info payload
                            .put("urlshare", "https://web.fulcrumapp.com/shares/zzzzd615de63zzzz.geojson?child=sucursales"); // Data Share for repetible 2 (.geojson): info fulcrum app 

                    JSONArray repetsinfo = new JSONArray();
                    repetsinfo.put(repet0);
                    repetsinfo.put(repet1);
                    
                    updateBbdd(payload, datashareurl, table, tablesrepet, repetsinfo);
                    break;
                
                default: 
                    System.out.println("No se programó sincronización para este formulario ");
            }
            
        }catch(Exception e){   
            out.write("Falló método processRequest: "+e.getMessage());
            try{                
                Mauxiliar mauxiliar2 = new Mauxiliar();
                mauxiliar2.insertExceptions(e.getMessage());
            }catch(Exception ex){ 
                out.write("Falló método insertExceptions: "+e.getMessage()); 
            }
        }        
    }
    
    public static void updateBbdd(JSONObject payload, String datashareurl, String tableparent, String[] tablesrepet, JSONArray repetsinfo) throws Exception{
        try{
            String fulcrum_id = payload.getJSONObject("data").getString("id");              

            //# Action based on record event type
            if( ! payload.getString("type").equals("record.delete")){  
                //# Fetch record info from parent data share 
                JSONObject geojsonparent = obtenerJson(datashareurl + "?fulcrum_id=" + fulcrum_id );
                
                //# Fetch records info from children data share (repetibles)
                JSONArray repetibles = new JSONArray();
                for (int p = 0; p < repetsinfo.length(); p++) {
                    String tablerep = repetsinfo.getJSONObject(p).getString("table");
                    String payloadcode = repetsinfo.getJSONObject(p).getString("payloadcode");
                    String urlshare = repetsinfo.getJSONObject(p).getString("urlshare");

                    //Get all geojsons children
                    JSONArray geojsonschilds = new JSONArray();
                    JSONArray childs = new JSONArray();
                    try{ // exception if there are no records on repetibles
                        childs = payload.getJSONObject("data").getJSONObject("form_values").getJSONArray(payloadcode);
                    }catch(Exception e){  }
                    for (int i = 0; i < childs.length(); i++) {
                        String childid = childs.getJSONObject(i).getString("id");
                        JSONObject gjchild = obtenerJson(urlshare + "&fulcrum_id=" + childid);
                        geojsonschilds.put(gjchild);
                    }
                    JSONObject repetible = new JSONObject();
                    repetible.put("table", tablerep)
                            .put("geojsons", geojsonschilds);

                    repetibles.put(repetible);                        
                }

                if(payload.getString("type").equals("record.create")) {
                    Modelwh modelo = new Modelwh();
                    modelo.insertar(tableparent, geojsonparent, repetibles);
                    
                }else if(payload.getString("type").equals("record.update")) {
                    Modelwh modelo = new Modelwh();
                    modelo.modificar(tableparent, geojsonparent, fulcrum_id, repetibles);   
                }
            }else{
                Modelwh modelo = new Modelwh();
                modelo.eliminar(tableparent, fulcrum_id, tablesrepet); 
            }
        }catch(Exception e) {
            System.out.println("Falló método updateBbdd: "+e.getMessage());
            throw e;
        }
        
    }
    
    
    public static JSONObject obtenerJson(String urlservicio) {
        JSONObject json = null;
        try {
            //creamos una URL donde esta nuestro webservice
            URL url = new URL(urlservicio);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //indicamos por que verbo HTML ejecutaremos la solicitud
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                //si la respuesta del servidor es distinta al codigo 200 lanzaremos una Exception
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            //creamos un StringBuilder para almacenar la respuesta del web service
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = br.read()) != -1) {
              sb.append((char) cp);
            }
            //en la cadena output almacenamos toda la respuesta del servidor
            String output = sb.toString();
            //convertimos la cadena a JSON a traves de la libreria 
            json = new JSONObject(output);
            conn.disconnect();            
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return json;
    }
    
    

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
