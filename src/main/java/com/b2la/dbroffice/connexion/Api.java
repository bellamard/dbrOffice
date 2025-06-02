package com.b2la.dbroffice.connexion;

import com.b2la.dbroffice.dao.CountUsers;
import com.b2la.dbroffice.dao.LoginRequest;
import com.b2la.dbroffice.dao.LoginResponse;
import com.b2la.dbroffice.dao.User;
import com.b2la.dbroffice.preference.Storage;
import com.google.gson.Gson;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Api {

    public static LoginResponse login(LoginRequest request){

        try {
            URL url= new URL("https://dbr.b2la.online/Users/connexion");
            HttpURLConnection con=(HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            Gson json= new Gson();
            String jsInput= json.toJson(request);
            try(OutputStream os= con.getOutputStream()){
                byte[] input=jsInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode=con.getResponseCode();
            if(responseCode==HttpURLConnection.HTTP_OK){
                BufferedReader in= new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response= new StringBuilder();
                String inputLine;
                while ((inputLine= in.readLine())!= null){
                    response.append(inputLine);
                }
                in.close();
                return json.fromJson(response.toString(), LoginResponse.class);
            }
            con.disconnect();
            return null;

        } catch (IOException e) {
            Alert alert= new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur!!!");
            alert.setHeaderText("Avertissement Erreur!!!");
            alert.setContentText("Veuillez ressayer: \n"+e);
            alert.showAndWait();
            System.out.println(e.toString());
            System.exit(0);
            throw new RuntimeException(e);

        }

    };

    public static CountUsers solde(LoginResponse bearer){
        String bearerUser= bearer.getBearer();
        try {
            URL url= new URL("https://dbr.b2la.online/Accounts/solde");
            HttpURLConnection con=(HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization","Bearer "+bearerUser);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            Gson json= new Gson();

            int responseCode=con.getResponseCode();
            if(responseCode==HttpURLConnection.HTTP_OK){
                BufferedReader in= new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response= new StringBuilder();
                String inputLine;
                while ((inputLine= in.readLine())!= null){
                    response.append(inputLine);
                }
                in.close();
                return json.fromJson(response.toString(), CountUsers.class);
            }
            con.disconnect();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return null;
    }


}
