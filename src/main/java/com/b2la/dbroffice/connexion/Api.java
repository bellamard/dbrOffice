package com.b2la.dbroffice.connexion;

import com.b2la.dbroffice.dao.*;
import com.b2la.dbroffice.preference.Storage;
import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.scene.control.Alert;

import java.io.*;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;


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
                os.flush();
                os.flush();
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

    public static List<StreamUser> userPerson(LoginResponse bearer){
        String bearerUser= bearer.getBearer();
        try {
            URL url= new URL("https://dbr.b2la.online/Users");
            HttpURLConnection con=(HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization","Bearer "+bearerUser);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            int responseCode=con.getResponseCode();
            if(responseCode==HttpURLConnection.HTTP_OK){
                BufferedReader in= new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response= new StringBuilder();
                String inputLine;
                while ((inputLine= in.readLine())!= null){
                    response.append(inputLine);
                }
                in.close();
                Gson json= new GsonBuilder().create();
                Type listeType= new TypeToken<List<StreamUser>>(){}.getType();
                return json.fromJson(response.toString(), listeType);
            }
            con.disconnect();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return null;
    }

    public static List<Operation> operationList(LoginResponse bearer){
        String bearerUser= bearer.getBearer();
        try {
            URL url= new URL("https://dbr.b2la.online/Operations");
            HttpURLConnection con=(HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization","Bearer "+bearerUser);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            int responseCode=con.getResponseCode();
            if(responseCode==HttpURLConnection.HTTP_OK){
                BufferedReader in= new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response= new StringBuilder();
                String inputLine;
                while ((inputLine= in.readLine())!= null){
                    response.append(inputLine);
                }
                in.close();
                Gson json= new GsonBuilder().create();
                Type listeType= new TypeToken<List<Operation>>(){}.getType();
                return json.fromJson(response.toString(), listeType);
            }
            con.disconnect();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return null;
    }

    public static List<Cost> costList(LoginResponse bearer){
        String bearerUser= bearer.getBearer();
        try {
            URL url= new URL("https://dbr.b2la.online/costs");
            HttpURLConnection con=(HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization","Bearer "+bearerUser);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            int responseCode=con.getResponseCode();
            if(responseCode==HttpURLConnection.HTTP_OK){
                BufferedReader in= new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response= new StringBuilder();
                String inputLine;
                while ((inputLine= in.readLine())!= null){
                    response.append(inputLine);
                }
                in.close();
                Gson json= new GsonBuilder().create();
                Type listeType= new TypeToken<List<Cost>>(){}.getType();
                return json.fromJson(response.toString(), listeType);
            }
            con.disconnect();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return null;
    }

    public static Cost addCost(Cost request){
        LoginResponse bearer=Storage.loadLogin();
        assert bearer != null;
        String bearerUser= bearer.getBearer();
        try {
            URL url= new URL("https://dbr.b2la.online/costs");
            HttpURLConnection con=(HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization","Bearer "+bearerUser);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            Gson json= new Gson();
            String jsInput= json.toJson(request);
            try(OutputStream os= con.getOutputStream()){
                byte[] input=jsInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
                os.flush();
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
                return json.fromJson(response.toString(), Cost.class);
            }
            con.disconnect();
            return null;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Cost updateCost(Cost request){
        LoginResponse bearer=Storage.loadLogin();
        assert bearer != null;
        String bearerUser= bearer.getBearer();
        try {
            URL url= new URL("https://dbr.b2la.online/costs/"+request.getId());
            HttpURLConnection con=(HttpURLConnection) url.openConnection();
            con.setRequestMethod("PUT");
            con.setRequestProperty("Authorization","Bearer "+bearerUser);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            Gson json= new Gson();
            String jsInput= json.toJson(request);
            try(OutputStream os= con.getOutputStream()){
                byte[] input=jsInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
                os.flush();
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
                return json.fromJson(response.toString(), Cost.class);
            }
            con.disconnect();
            return null;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Cost deleteCost(Cost request){
        LoginResponse bearer=Storage.loadLogin();
        assert bearer != null;
        String bearerUser= bearer.getBearer();
        try {
            URL url= new URL("https://dbr.b2la.online/costs/"+request.getId());
            HttpURLConnection con=(HttpURLConnection) url.openConnection();
            con.setRequestMethod("DELETE");
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
                return json.fromJson(response.toString(), Cost.class);
            }
            con.disconnect();
            return null;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static User addUser(User request, String urlUser){
        LoginResponse bearer=Storage.loadLogin();
        assert bearer != null;
        String bearerUser= bearer.getBearer();
        try {
            URL url= new URL(urlUser);
            HttpURLConnection con=(HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization","Bearer "+bearerUser);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            Gson json= new Gson();
            String jsInput= json.toJson(request);
            try(OutputStream os= con.getOutputStream()){
                byte[] input=jsInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
                os.flush();
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
                return json.fromJson(response.toString(), User.class);
            }
            con.disconnect();
            return null;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void activation(){

    }

}
