package guilhermeSchults.generaisBizantinos.No_General;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class NetClientGet {

	
	private static final String CAMINHO_API = "https://api.hgbrasil.com/weather?format=json&array_limit=1&fields=only_results,temp";

	private static final String KEY="cc015f08";
	
	private static WeatherResponse resp= new WeatherResponse();
	
	public static double GetTemperatura(String cidade, String estado) {
		try 
		{

			URL url = new URL(CAMINHO_API + "&key=" + KEY + "&city_name=" + cidade + "," + estado);
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
 			if (conn.getResponseCode() != 200) 
 			{
				throw new RuntimeException("Erro: " + conn.getResponseCode());
			}
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			BufferedReader br = new BufferedReader(in);
			String output;
			while ((output = br.readLine()) != null) 
			{
				Gson gson = new GsonBuilder().create();
				resp=gson.fromJson(output, WeatherResponse.class);
				return resp.temp;
			}
			conn.disconnect();

		} catch (Exception e) 
		{
			System.out.println("Excecao retornada:- " + e);
		}
		return 0;
	}
	
	public static class WeatherResponse 
	{
		double temp;
	}
	
}
