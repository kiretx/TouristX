package TouristX;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;

public class TouristX {

	private static final String EncodingUtil = null;

	public static void main(String[] args) throws IOException {
		
		
		System.out.print("Enter Country to find: ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	 
	    String readStr = reader.readLine();
	    String unformatedName = readStr;
	    String country = URLEncoder.encode(readStr, "UTF-8").replaceAll("\\+", "%20");
	    
		try {
			fetchCountry(country,unformatedName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void fetchCountry(String country, String unformatedName) throws IOException{
		
		HttpURLConnection connection = (HttpURLConnection) new URL("https://restcountries.com/v3.1/name/"+country).openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/json");
		int responseCode = connection.getResponseCode();
		if(responseCode == 200){
		  Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
		  mongoLogger.setLevel(Level.SEVERE); 
		  System.out.println("GET was successful.");
	      MongoClient client = MongoClients.create("mongodb+srv://eriktx:1234@cluster0.0slax.mongodb.net/TouristX?retryWrites=true&w=majority");
			
          String inline = "";
          Scanner scanner = new Scanner(connection.getInputStream());
          inline += scanner.nextLine();
          scanner.close();
          
          //reading api response
          JSONArray jarr = new JSONArray(inline);
          String str = jarr.get(0).toString();
          JSONObject jobj = new JSONObject(str);

          String name = unformatedName;
          
          //extracting from api response
          String capital = jobj.getJSONArray("capital").get(0).toString();
          JSONObject lanObj = jobj.getJSONObject("languages");
          Iterator<String> keys = lanObj.keys();
          String language=null;
          while(keys.hasNext()) {
              language = lanObj.get(keys.next()).toString();
              break;
          }
          String subregion = jobj.getString("subregion");
          String region = jobj.getString("region");
          Long population = jobj.getLong("population");
          Long area = jobj.getLong("area");
          String altSpellings = jobj.getJSONArray("altSpellings").get(0).toString();
          
            
          try {
            	 
            	MongoDatabase db = client.getDatabase("TouristX");
    			MongoCollection<Document> col = db.getCollection("country");
    			
    			Document countryApiDoc = new Document();
    			
    			Document filterDoc = new Document();
    			

    			filterDoc.put("Name", name);

    			Iterator<Document> iter = col.find(filterDoc).iterator(); 
    			
    			//inserted only new country
    			if(!iter.hasNext()) {	
    					countryApiDoc.put("Name", name);      
    			        countryApiDoc.put("Capital", capital);      
    			        countryApiDoc.put("Region", region);
    			        countryApiDoc.put("Subregion", subregion);
    			        countryApiDoc.put("Population", population);
    			        countryApiDoc.put("Area", area);
    			        countryApiDoc.put("Alternate Spellings", altSpellings);
    			        countryApiDoc.put("Main Language", language);
    	    		        
    		            col.insertOne(countryApiDoc);
    				
    			}
    			
    			//finding top 10
    			FindIterable<Document> cursor = col.find().sort(new BasicDBObject("Population",-1)).limit(10);
    			
    			System.out.println("");
                System.out.println("");
    			MongoCursor<Document> iterate = cursor.iterator();
    			while(iterate.hasNext()) {
    				Document document = iterate.next();
    				
    				System.out.println("===============================================================");
    				System.out.println(document.get("Name")+" - "+document.get("Capital")+
    						", Population: "+ document.get("Population").toString());
    				System.out.println("");
    				System.out.println("");

    			}
    		   
    		
	            client.close();
        	
            }
         catch (Exception e) {
            	
            }
          
            System.out.println("");
            System.out.println("");
            System.out.println("");
            System.out.println("Successful MongoDB Transaction");  
            connection.disconnect();
            
		}
		else if(responseCode == 500){
			System.out.println("GET Failed.");
		}
	}

}
