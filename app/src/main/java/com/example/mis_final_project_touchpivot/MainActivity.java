package com.example.mis_final_project_touchpivot;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // declare variables
    ArrayList<CycleHires> cycleHires;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //look device in landscape mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);
        cycleHires = new ArrayList<>();

        checkPermission();

        String url1 = "https://data.london.gov.uk/download/number-bicycle-hires/ac29363e-e0cb-47cc-a97a-e216d900a6b0/tfl-daily-cycle-hires.xls";
        new XLSAsync().execute(url1);

    }

    private void readExcelData(String filepath){

    }

    // return the value of the cell as a String
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
        String[] rows = stringBuilder.toString().split(":");

        // split columns at ,
        for(int i = 0; i<rows.length; i++){
            String[] columns = rows[i].split(",");

            try{
                if(i != 0) {
                    String date = columns[0];
                    int hires = Integer.parseInt(columns[1]);
                    Log.d(TAG, "Date: " + date + ", Hires: " + hires);
                    cycleHires.add(new CycleHires(date, hires));
                }
            }
            catch (NumberFormatException e){
                Log.e(TAG, "parseStringBuilder: NumberFormatException: "+e.getMessage());
            }
        }
        printDataLog();
    }

    public void printDataLog(){
        for(int i = 0; i < cycleHires.size(); i++){
            Log.i(TAG, "Date: "+cycleHires.get(i).getDate() + ", Hires: "+cycleHires.get(i).getHires());
        }
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
    private class XLSAsync extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... urls){
            try{
                Log.i(TAG, "Set up URL");
                // set URL for download.
                URL url = new URL (urls[0]);
                Log.i(TAG, "Establishing URL connection");
                URLConnection uc = url.openConnection();

                System.out.println("Trying to print that Input Stream: "+uc.getInputStream());

                HSSFWorkbook workbook = new HSSFWorkbook(uc.getInputStream());
                HSSFSheet sheet = workbook.getSheetAt(1);
                int rowCount = sheet.getPhysicalNumberOfRows();
                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                StringBuilder sb = new StringBuilder();

                // get rows
                for(int i = 0; i < rowCount; i++){
                    Row row = sheet.getRow(i);
                    int cellCount = row.getPhysicalNumberOfCells();
                    // get only the first 2 columns
                    for(int j = 0; j < 2; j++){
                        String value = getCellAsString(row, j,formulaEvaluator);

                        // print data from cells for "visual debugging"
                        String cellInfo = "i:"+i+"; j:"+j+"; v:" +value;
                        Log.d(TAG, "readExcelData: Data from row: "+cellInfo);

                        // attach data to stringbuilder
                        sb.append(value + ", ");
                    }
                }
                sb.append(":");

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
    }
}