package com.example.mis_final_project_touchpivot;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.nfc.Tag;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // declare variables
    File file;

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

        readExcelData("app/datasets/tfl-daily-cycle-hires.xls");
    }

    // android version of level <= 23
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

    private void readExcelData(String filepath){
        Log.d(TAG, "readExcelData: Reading Excel file");
        File inputFile = new File(filepath);

        try{
            // access cycle hire data, data stored on second sheet
            InputStream inputStream = new FileInputStream(inputFile);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(1);
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
                    String cellInfo = "i:"+i+"; j:"+j+"; v:" +value;
                    Log.d(TAG, "readExcelData: Data from row: "+cellInfo);
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

    //
    public void parseStringBuilder(StringBuilder stringBuilder){
        Log.d(TAG, "parseStringBuilder: Started parsing");

        // split stringbuilder into rows
        String[] rows = stringBuilder.toString().split(":");

        // split columns at ,
        for(int i = 0; i<rows.length; i++){
            String[] columns = rows[i].split(",");

            try{
                String date = columns[0];
                int hires = Integer.parseInt(columns[1]);
                Log.d(TAG, "Date: "+date+", Hires: "+hires);
                cycleHires.add(new CycleHires(date, hires));
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
}