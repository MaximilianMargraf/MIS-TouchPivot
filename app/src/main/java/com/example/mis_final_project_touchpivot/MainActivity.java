package com.example.mis_final_project_touchpivot;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // declare variables
    ArrayList<StringIntegerPair> stringIntegerPairs;
    ListView listView;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //look device in landscape mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        checkPermission();

        listView = (ListView) findViewById(R.id.data_view);
        stringIntegerPairs = new ArrayList<>();

        String url1 = "https://data.london.gov.uk/download/number-bicycle-hires/ac29363e-e0cb-47cc-a97a-e216d900a6b0/tfl-daily-cycle-hires.xls";
        new XLSAsync().execute(url1);
    }

    // android version of level <= 26
    public void checkPermission(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            permissionCheck += this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");

            if(permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
            }
        }
        else{
            Log.d(TAG, "No need to check permissions");
        }
    }

    // https://developer.android.com/reference/android/os/AsyncTask
    // tasks like this need to be done in a separate thread!
    // for now, this only works for the daily bike hires
    private class XLSAsync extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... urls){
            try{
                Log.i(TAG, "XLSAsync: Started");
                // set URL for download.
                URL url = new URL (urls[0]);
                URLConnection uc = url.openConnection();

                HSSFWorkbook workbook = new HSSFWorkbook(uc.getInputStream());
                HSSFSheet sheet = workbook.getSheetAt(1);
                int rowCount = sheet.getPhysicalNumberOfRows();
                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                StringBuilder sb = new StringBuilder();

                // get rows
                for(int i = 0; i < rowCount; i++){
                    Row row = sheet.getRow(i);
                    // get only the first 2 columns for daily bike hires
                    for(int j = 0; j < 2; j++){
                        String value = getCellAsString(row, j,formulaEvaluator);

                        // attach data to stringbuilder
                        sb.append(value + ",");
                    }
                }
                parseStringBuilder(sb);
            }
            catch (FileNotFoundException e) {
                Log.e(TAG, "readExcelData: FileNotFoundException " + e.getMessage());
            }
            catch (IOException e){
                Log.e(TAG, "readExcelData: Error reading inputstream " + e.getMessage());
            }

            return true;
        }

        // called on main thread after async task is done
        @Override
        protected void onPostExecute(Boolean result){
            ArrayList<String> listPrint = new ArrayList<>();
            listPrint.add("Date : Number of cycle hires");
            for(StringIntegerPair sp: stringIntegerPairs){
                listPrint.add(sp.getDate() +" : "+ sp.getHires());
            }

            for(String sp: listPrint){
                Log.d(TAG, sp);
            }

            adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_listview, listPrint);
            listView.setAdapter(adapter);
        }
    }


    // returns the value of the first 2 lists columns
    private String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator){
        String value = "";
        try{
            Cell cell = row.getCell(c);
            CellValue cellValue = formulaEvaluator.evaluate(cell);

            // boolean value in cell
            if(cellValue.getCellType() == CELL_TYPE_BOOLEAN){
                value = ""+cellValue.getBooleanValue();
            }
            // numeric value in cell
            else if(cellValue.getCellType() == CELL_TYPE_NUMERIC){
                double numericValue = cellValue.getNumberValue();
                // date format recognized in cell
                if(HSSFDateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
                    value = format.format(HSSFDateUtil.getJavaDate(numericValue));
                }
                // normal numerical value in cell
                else{
                    value = ""+numericValue;
                }
            }
            // string in cell
            else if(cellValue.getCellType() == CELL_TYPE_STRING){
                value = ""+cellValue.getStringValue();
            }
        }
        catch (NullPointerException e){
            Log.e(TAG, "getCellAsString: NullPointerException: "+e.getMessage());
        }
        return value;
    }

    // split data and add it to the list
    public void parseStringBuilder(StringBuilder stringBuilder){
        Log.d(TAG, "parseStringBuilder: Started parsing");

        // split stringbuilder into rows
        String[] rows = stringBuilder.toString().split(",");

        String date = "";
        int hires = 0;
        try{
            for(int i = 2; i<rows.length; i++){

                    // check if first (date) or second entry (value)
                    if(i%2==0) {
                        Log.d(TAG, rows[i]);
                        date = rows[i];
                    }
                    // if a second entry
                    else{
                        Log.d(TAG, rows[i]);
                        hires = Integer.parseInt(rows[i].substring(0, rows[i].length()-2));
                        StringIntegerPair k = new StringIntegerPair(date, hires);
                        stringIntegerPairs.add(k);
                    }
                }
            }
        catch (NumberFormatException e){
            Log.e(TAG, "parseStringBuilder: NumberFormatException: "+e.getMessage());
        }
        // we do not reach this line of code
        Log.d(TAG,"Reading from xls into StringIntegerPair completed");
        printDataLog();
    }

    public void printDataLog(){
        for(int i = 0; i < stringIntegerPairs.size(); i++){
            Log.i(TAG, "Date: "+ stringIntegerPairs.get(i).getDate() + ", Hires: "+ stringIntegerPairs.get(i).getHires());
        }
    }
}