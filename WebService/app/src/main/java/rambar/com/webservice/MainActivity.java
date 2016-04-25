package rambar.com.webservice;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    EditText latitud;
    EditText longitud;
    Button obtenerDatos;
    TextView resultado;
    Button conectar;
    String urlString = "http://oja.la";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitud = (EditText) findViewById(R.id.latitud);
        longitud = (EditText) findViewById(R.id.longitud);
        obtenerDatos = (Button) findViewById(R.id.obtenerDatos);
        resultado = (TextView) findViewById(R.id.resultado);
        conectar = (Button) findViewById(R.id.conectar);
    }

    public void obtenerDatos(View view) {

        switch (view.getId()){
            case R.id.obtenerDatos:

                break;

            default:

                break;
        }

    }

    public void conectar(View view) {

    }

    //AsyncTask para conectar http
    //Android no permite hacer conexiones a internet en el hilo principal
    private class Conexion extends AsyncTask<Void, Integer, Void>{

        //en este metodo se ejecutan los procesos en segundo plano
        @Override
        protected Void doInBackground(Void... params) {

            try {
                //URL es una clase de java no android usada para almacenar direcciones URL - necesita try/catch
                URL url = new URL(urlString);
                //HttpURLConnection es una clase para realizar la conexion
                //Debemos crear una instancia de esta clase usando url.openConnection() y un cast
                //esto genera un error y debemos poner otro catch
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //Añadimos una cabecera HTTP para identificarnos y evitar obtener errores en algunos servidores que exigen identificacion
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux: Android 1.5; es-ES) Ejemplo HTTP");

                //Creamos un entero para almacenar la respuesta
                int respuesta = connection.getResponseCode();


                //comparamos la respuesta con una de las variables de estado ejemp HTTP_OK
                if (respuesta==HttpURLConnection.HTTP_OK){
                    //Leyendo la cadena que devuelve la pagina
                    InputStream in = new BufferedInputStream(connection.getInputStream());//preparo la cadena de entrada
                    BufferedReader lector = new BufferedReader(new InputStreamReader(connection.getInputStream()));//guardo la cadena en un BufferedReader
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

    }

    //Uso String en el primer parametro para poder pasar la cadena desde ...
    //AsyncTask para conectar con la API
    private class ObtenerDatos extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... params) {

            String stringUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng=";
            stringUrl = stringUrl + params[0];
            stringUrl = stringUrl + ",";
            stringUrl = stringUrl + params[1];
            stringUrl = stringUrl + "&sensor=false";
            String devuelve = "";

            //Abrir conexion
            URL url = null;

            try {
                url = new URL(stringUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int respuesta = connection.getResponseCode();

                StringBuilder result = new StringBuilder();

                if (respuesta == HttpURLConnection.HTTP_OK){

                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    //Para pasar un BufferedReader a un String hay que usar un StringBuilder

                    //En el siguiente codigo convierto el contenido del BufferReader en una unica linea de texto
                    String line;

                    while((line=reader.readLine())!=null){
                        result.append(line);//Trabaja linea por linea
                    }

                    //Ahora creamos un objeto JSON para poder acceder a los atributos y metodos del objeto
                    JSONObject respuestaJSON = new JSONObject(result.toString());//Crea un objeto JSON a partir de un StringBuilder;

                    //Accedemos al vector o array de resultados
                    //Hay que añadir un catch a la excepcion
                    JSONArray resultJSON = respuestaJSON.getJSONArray("results");//results es el nombre del campo en JSON

                    //Vamos obteniendo todos los campos que nos interesen
                    //En este caso obtenemos la primera direccion de los resultados
                    String direccion = "SIN DATOS PARA ESAS COORDENADAS";

                    if(resultJSON.length()>0){
                        direccion = resultJSON.getJSONObject(0).getString("formatted_address"); //Escojo el objeto "0" que es el primero y lo convierto en string
                    }

                    devuelve = "Direccion: " + direccion;


                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return devuelve;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            resultado.setText(s);
        }
    }
}
