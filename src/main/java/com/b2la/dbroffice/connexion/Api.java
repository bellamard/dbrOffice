package com.b2la.dbroffice.connexion;

import com.b2la.dbroffice.dao.User;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Api {

    public User[] login(){

        try {
            URL url= new URL("https://dbr.b2la.online/Users/connexion");
            HttpURLConnection con=(HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            BufferedReader in= new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer response= new StringBuffer();
            String inputLine;
            while ((inputLine= in.readLine())!= null){
                response.append(inputLine);
            }
            in.close();
            Gson gson= new
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    };
}
