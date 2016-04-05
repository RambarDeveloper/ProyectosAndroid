package la.oja.senseware;

import android.content.Context;
import android.content.SharedPreferences;

import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import la.oja.senseware.models.User;

/**
 * Created by jcoaks on 10/3/2016.
 */
public class ApiCall
{
    HttpEntity<String> requestEntity;
    HttpHeaders requestHeaders;

    public ApiCall(Context context)
    {
        SharedPreferences settings;
        settings = context.getSharedPreferences("ActivitySharedPreferences_data", 0);
        String mail = settings.getString("email", "");
        String pass = settings.getString("password", "");
        HttpAuthentication authHeader = new HttpBasicAuthentication(mail, pass);
        requestHeaders = new HttpHeaders();
        requestHeaders.setAuthorization(authHeader);
        //requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.add("Content-Type", "application/json;charset=UTF-8");
        requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    }

    public ApiCall(String mail, String pass)
    {
        HttpAuthentication authHeader = new HttpBasicAuthentication(mail, pass);
        requestHeaders = new HttpHeaders();
        requestHeaders.setAuthorization(authHeader);
        requestHeaders.add("Content-Type", "application/json;charset=UTF-8");
        requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    }

    public String callGet(String url)
    {
        String resp = null;
        try
        {
            requestEntity = new HttpEntity<String>(requestHeaders);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            resp = responseEntity.getBody();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }

    public String callPost(String url, String data)
    {
        String resp = null;
        try
        {
            HttpEntity<String> entity = new HttpEntity<String>(data, requestHeaders);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            resp = response.getBody();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }


    public String callPost(String url, User data)
    {
        String resp = null;
        try
        {
            HttpEntity<User> entity = new HttpEntity<User>(data, requestHeaders);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            resp = response.getBody();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }

    public String callPut(String url, String data)
    {
        String resp = null;
        try
        {
            HttpEntity<String> entity = new HttpEntity<String>(data, requestHeaders);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            resp = response.getBody();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }
}
