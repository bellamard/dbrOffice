package com.b2la.dbroffice.connexion;

import com.b2la.dbroffice.dao.LoginRequest;
import com.b2la.dbroffice.dao.LoginResponse;
import com.b2la.dbroffice.preference.Storage;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
                byte[] input=jsInput.getBytes("utf-8");
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

            return null;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    };
}
