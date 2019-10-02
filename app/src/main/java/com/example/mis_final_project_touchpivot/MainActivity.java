package com.example.mis_final_project_touchpivot;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

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
import java.util.List;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //Table that will include all our data for KIS
    List<KSI> myData = new ArrayList<>();
    TableView<String[]> table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //look device in landscape mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        checkPermission();

        // connect table to tableview
         table = (TableView<String[]>) findViewById(R.id.tableView);

        SCpair p = new SCpair("https://data.london.gov.uk/download/road-casualties-severity-borough/a883bd65-c504-43bd-9032-efd71349385e/road-casualties-severity-borough.xls", this);
        new RoadCasualties().execute(p);
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
    private class RoadCasualties extends AsyncTask<SCpair, Integer, Context> {
        @Override
        protected Context doInBackground(SCpair... url_){
            try{
                Log.i(TAG, "Initiate URL to connect to data");
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                URL url = new URL (url_[0].getString_());
                URLConnection uc = url.openConnection();

                Log.i(TAG, "Create an HSSF workbook to work on the excel file");
                HSSFWorkbook workbook = new HSSFWorkbook(uc.getInputStream());
                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

                // page 1
                HSSFSheet sheet1 = workbook.getSheetAt(1);
                HSSFSheet sheet2 = workbook.getSheetAt(2);
                StringBuilder sb = new StringBuilder();

                Log.i(TAG, "Fill String Builder with Data from page 1");
                for(int i = 2; i < 12; i++){
                    Row row = sheet1.getRow(i+10);
                    for(int j = 0; j < 2; j++){
                        String value = getCellAsString(row, j, formulaEvaluator);
                        sb.append(value + ",");
                    }
                    row = sheet2.getRow(i);
                    for(int j = 1; j < 7; j++){
                        String value = getCellAsString(row, j, formulaEvaluator);
                        sb.append(value + ",");
                    }

                }

                Log.i(TAG, "String Builder done, on to parsing the string");
                parseStringBuilder(sb);
            }
            catch (FileNotFoundException e) {
                Log.e(TAG, "readExcelData: FileNotFoundException " + e.getMessage());
            }
            catch (IOException e){
                Log.e(TAG, "readExcelData: Error reading inputstream " + e.getMessage());
            }
            return url_[0].getContext();
        }

        // called on main thread after async task is done
        @Override
        protected void onPostExecute(Context context){
            String[][] strings = new String[10][8];
            strings[0][0] = "Year";
            strings[0][1] = "Long term";
            strings[0][2] = "KSI pedestrian";
            strings[0][3] = "KSI pedal/cycle";
            strings[0][4] = "KSI 2 wheels";
            strings[0][5] = "KSI car/taxi";
            strings[0][6] = "KSI bus/coach";
            strings[0][7] = "KSI other";
            for(int i = 1; i <myData.size();i++){
                for(int j = 0; j < 8; j++){
                    switch (j) {
                        case 0:
                            strings[i][j] = "" + myData.get(i).year_;
                            break;
                        case 1:
                            strings[i][j] = "" + myData.get(i).long_term_trend_;
                            break;
                        case 2:
                            strings[i][j] = "" + myData.get(i).pedestrian_;
                            break;
                        case 3:
                            strings[i][j] = "" + myData.get(i).pedal_cycle_;
                            break;
                        case 4:
                            strings[i][j] = "" + myData.get(i).powered_two_wheeler_;
                            break;
                        case 5:
                            strings[i][j] = "" + myData.get(i).car_taxi_;
                            break;
                        case 6:
                            strings[i][j] = "" + myData.get(i).bus_coach_;
                            break;
                        case 7:
                            strings[i][j] = "" + myData.get(i).goods_other_;
                            break;
                    }
                }
            }
            table.setDataAdapter(new SimpleTableDataAdapter(context, strings));
        }
    }


    // returns the value of the given columns
    private String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator){
        String value = "";
        try{
            // in the given row access column c
            Cell cell = row.getCell(c);
            CellValue cellValue = formulaEvaluator.evaluate(cell);

            // boolean value in cell
            if(cellValue.getCellType() == CELL_TYPE_BOOLEAN){
                value = ""+cellValue.getBooleanValue();
            }
            // numeric value in cell
            else if(cellValue.getCellType() == CELL_TYPE_NUMERIC){
                int numericValue = (int) cellValue.getNumberValue();
                // date format recognized in cell
                if(HSSFDateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
                    value = format.format(HSSFDateUtil.getJavaDate(numericValue));
                }
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
        String[] rows = stringBuilder.toString().split(",");

        // page 1, row 12 to 21, column 0 and 1
        String year_;
        int long_term_trend_;

        // page 2, row 2 to 11, column 1 2 3 4 5 6
        int pedestrian_;
        int pedal_cycle_;
        int powered_two_wheeler_;
        int car_taxi_;
        int bus_coach_;
        int goods_other_;
        try{
            for(int i = 0; i < rows.length; i+=8){
                year_ = rows[i];
                long_term_trend_ = Integer.parseInt(rows[i+1]);
                pedestrian_ = Integer.parseInt(rows[i+2]);
                pedal_cycle_ = Integer.parseInt(rows[i+3]);
                powered_two_wheeler_ = Integer.parseInt(rows[i+4]);
                car_taxi_ = Integer.parseInt(rows[i+5]);
                bus_coach_ = Integer.parseInt(rows[i+6]);
                goods_other_ = Integer.parseInt(rows[i+7]);
                KSI k = new KSI(year_,long_term_trend_,pedestrian_,pedal_cycle_,powered_two_wheeler_,car_taxi_,bus_coach_,goods_other_);
                myData.add(k);
            }
        }
        catch (NumberFormatException e){
            Log.e(TAG, "parseStringBuilder: NumberFormatException: "+e.getMessage());
        }
        Log.d(TAG,"Reading data finished");
    }
}