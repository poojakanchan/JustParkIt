package com.parkingapp.parser;

import android.content.ContextWrapper;
import com.parkingapp.exception.ParkingAppException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by pooja, nayanakamath on 4/25/2015.

 */
public class CsvParser {

    public static final String INSERT_QUERY ="INSERT INTO Sfsu_StreetCleaning(WeekDay,RightLeft, Corridor, FromHour," +
            "ToHour, Holidays, Week1OfMonth, Week2OfMonth, Week3OfMonth, Week4OfMonth, Week5OfMonth," +
            "LF_FADD, LF_TOADD, RT_TOADD, RT_FADD, STREETNAME, ZIP_CODE, NHOOD)  VALUES (";
    public void parseCSV() throws ParkingAppException{
        try{
            FileReader fileReader =
                    new FileReader("sf_street_cleaning.csv");

            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            FileWriter fileWriter =
                    new FileWriter("sf_street_cleaning.sql");

            BufferedWriter bufferedWriter =
                    new BufferedWriter(fileWriter);
            String line = null;
            bufferedReader.readLine();
            while((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                String[] columns = line.split(",");
                StringBuilder query = new StringBuilder();
                query.append(INSERT_QUERY);

                appendString(query,columns[0]);
                appendString(query,columns[1]);
                appendString(query,columns[2]);
                appendString(query,columns[3]);
                appendString(query,columns[4]);
                appendChar(query, columns[5]);
                appendChar(query, columns[6]);
                appendChar(query, columns[7]);
                appendChar(query, columns[8]);
                appendChar(query, columns[9]);
                appendChar(query, columns[10]);
                appendNumber(query, columns[11]);
                appendNumber(query, columns[12]);
                appendNumber(query, columns[13]);
                appendNumber(query, columns[14]);
                appendString(query,columns[15]);
                appendNumber(query, columns[16]);
                if(columns[17] == null)
                {
                    query.append("null");
                }else {
                    appendString(query,columns[17]);
                }
                query.deleteCharAt(query.length()-1);
                query.append("); \n");
                bufferedWriter.write(query.toString());
            }

            bufferedReader.close();
            bufferedWriter.close();

        }
        catch(FileNotFoundException ex) {
            throw new ParkingAppException(ex,ex.getMessage());
        }
        catch(IOException ex) {
            throw new ParkingAppException(ex,ex.getMessage());
        }
    }
    private void appendString(StringBuilder query,String value) {
        if(value == null)
        {
            query.append("null");
        } else {
            query.append("\"");
            query.append(value.trim());
            query.append("\"");
            query.append(",");
        }
           }
    private void appendNumber(StringBuilder query,String value) {
        if(value == null)
        {
            query.append("null");
        } else {
            query.append(Integer.parseInt(value.trim()));
            query.append(",");
        }
       }
    private void appendChar(StringBuilder query,String value) {
        if(value == null)
        {
            query.append("null");
        } else {
            query.append("'");
            query.append(value.trim().charAt(0));
            query.append("'");
            query.append(",");
        }
    }

    }