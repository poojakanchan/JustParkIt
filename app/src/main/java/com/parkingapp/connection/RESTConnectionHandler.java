package com.parkingapp.connection;

import android.os.StrictMode;

import com.parkingapp.exception.ParkingAppException;
import com.parkingapp.parser.SFParkBean;
import com.parkingapp.parser.SfXmlParser;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by pooja on 4/13/2015.
 * abstract class for REST connection handling
 */
public abstract class RESTConnectionHandler {

//  Same class should be used for calling both google maps and SF park APIs.

    /**
     * generates REST URL for passed URI parameters.
     * @param uri URI of the API to be accessed
     * @param parameters the parameters of the API
     * @return generated URL
     *         If URI is empty, exception is thrown
     */

    public String generateURL(String uri, List<String> parameters) throws  ParkingAppException{
        char uroConnector = '?';
        char paramConnector = '&';
        StringBuilder url = new StringBuilder();

        if(uri == null) {
            throw new ParkingAppException(" URI can not be null ");
        }
        url.append(uri);
        url.append(uroConnector);

        for(String param : parameters) {
           url.append(param);
           url.append(paramConnector);
        }
        url.deleteCharAt(url.length()-1);
        return url.toString();
    }

    /**
     * opens a HTTP call and buffers the response into String.
     * @param urlStr
     * @return API response string
     */
  public List<SFParkBean> connect(String urlStr) throws ParkingAppException{

      //  String urlStr = "http://api.sfpark.org/sfpark/rest/availabilityservice?lat=37.792275&long=-122.397089&radius=0.25&uom=mile&method=availability&response=xml";
      // String uri = urlStr;
      HttpURLConnection conn =null;
      try {

          int SDK_INT = android.os.Build.VERSION.SDK_INT;

          if (SDK_INT > 8) {
              StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                      .permitAll().build();
              StrictMode.setThreadPolicy(policy);
              //your codes here

          }
          URL url = new URL(urlStr);

          // Open a new connection for the passed URL
          conn = (HttpURLConnection) url.openConnection();

          /* When the connection is successful, the response code is 200 OK,
            If not, the connection is unsuccessful. In case of unsuccessful connection,
            exception is thrown.
          */
          if (conn.getResponseCode() != 200) {
              throw new IOException(conn.getResponseMessage());
          }

          //Setting timeout to 5 sec
          conn.setConnectTimeout(5000);
          conn.setReadTimeout(5000);

          SfXmlParser parser = new SfXmlParser();
          parser.createParser();
          Document document = parser.getParser().parse(conn.getInputStream());
          parser.setDocument(document);

          List<SFParkBean> list = parser.parseXML();
         return list;
          /*BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

          StringBuilder sb = new StringBuilder();
          String line;
          while ((line= rd.readLine())!= null) {
              sb.append(line);
          }
          return sb.toString();

      */
      }catch(SAXException se) {
          se.printStackTrace();
          throw new ParkingAppException(se,se.getMessage());
      }catch(IOException ioe) {
          ioe.printStackTrace();
          throw new ParkingAppException(ioe,ioe.getMessage());
      } catch (Exception e) {
          throw new ParkingAppException(e,e.getMessage());
      }finally {
          if(conn != null)
            conn.disconnect();
      }
  }
}
