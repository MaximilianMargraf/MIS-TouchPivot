package com.example.mis_final_project_touchpivot;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //Table that will include all our data for KIS
    List<KSI> myData = new ArrayList<>();
    Context context = this;
    TextView cm1;
    TextView cm2;
    TableLayout table;
    Button compare;

    // lists with values from selected columns, the 0st entry is the name of the column
    List<String> selection1 = new ArrayList<>();
    List<String> selection2 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //look device in landscape mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        // link design elements
        table = (TableLayout)findViewById(R.id.table_main);
        cm1 = (TextView)findViewById(R.id.textView);
        cm2 = (TextView)findViewById(R.id.textView2);
        compare = (Button)findViewById(R.id.button);

        // check for storage permissions (I think this became obsolete)
        checkPermission();

        // call compare function when compare button is clicked
        compare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compare();
            }
        });

        // read in table from URL with Async Task
        SCpair p = new SCpair("https://data.london.gov.uk/download/road-casualties-severity-borough/a883bd65-c504-43bd-9032-efd71349385e/road-casualties-severity-borough.xls", this);
        new RoadCasualties().execute(p);
    }

    public Context getContext(){
        return context;
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
    private class RoadCasualties extends AsyncTask<SCpair, Integer, Context> {
        @Override
        protected Context doInBackground(SCpair... url_){
            try{
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                URL url = new URL (url_[0].getString_());
                URLConnection uc = url.openConnection();

                HSSFWorkbook workbook = new HSSFWorkbook(uc.getInputStream());
                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

                // page 1
                HSSFSheet sheet1 = workbook.getSheetAt(1);
                HSSFSheet sheet2 = workbook.getSheetAt(2);
                StringBuilder sb = new StringBuilder();

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
            return context;
        }

        // called on main thread after async task is done
        @Override
        protected void onPostExecute(Context context){
            String[][] strings = new String[11][8];
            strings[0][0] = "Year";
            strings[0][1] = "L. term";
            strings[0][2] = "KSI pedestrian";
            strings[0][3] = "KSI pedal/cycle";
            strings[0][4] = "KSI 2 wheels";
            strings[0][5] = "KSI car/taxi";
            strings[0][6] = "KSI bus/coach";
            strings[0][7] = "KSI other";
            for(int i = 1; i <myData.size()+1;i++){
                for(int j = 0; j < 8; j++){
                    switch (j) {
                        case 0:
                            strings[i][j] = "" + myData.get(i-1).year_;
                            break;
                        case 1:
                            strings[i][j] = "" + myData.get(i-1).long_term_trend_;
                            break;
                        case 2:
                            strings[i][j] = "" + myData.get(i-1).pedestrian_;
                            break;
                        case 3:
                            strings[i][j] = "" + myData.get(i-1).pedal_cycle_;
                            break;
                        case 4:
                            strings[i][j] = "" + myData.get(i-1).powered_two_wheeler_;
                            break;
                        case 5:
                            strings[i][j] = "" + myData.get(i-1).car_taxi_;
                            break;
                        case 6:
                            strings[i][j] = "" + myData.get(i-1).bus_coach_;
                            break;
                        case 7:
                            strings[i][j] = "" + myData.get(i-1).goods_other_;
                            break;
                    }
                }
            }
            initTable(strings);
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

    // read data into TableLayout via a generalized function for all 2D string arrays
    public void initTable(final String[][] strings) {
        table.setStretchAllColumns(true);
        table.bringToFront();
        for(int i = 0; i < strings.length; i++){
            TableRow tr =  new TableRow(this);
            // fill the Textviews of the different
            for(int j = 0; j < strings[0].length; j++){
                final TextView txt = new TextView(this);
                txt.setTextSize(18);
                txt.setText(strings[i][j]);
                txt.setId(j);
                // On click listener for columns
                txt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int id = txt.getId();
                        // case: no selection yet
                        if(cm1.getText()=="" && cm2.getText()==""){
                            cm1.setText(strings[0][id]);
                            for(int k = 0; k < strings.length; k++){
                                selection1.add(strings[k][id]);
                            }
                        }
                        // case: first selection happened already
                        else if(cm1.getText()!="" && cm2.getText()==""){
                            cm2.setText(strings[0][id]);
                            for(int k = 0; k < strings.length; k++){
                                selection2.add(strings[k][id]);
                            }
                        }
                        // case: both selections already done, reset and fill first selection list
                        else if(cm1.getText()!="" && cm2.getText()!=""){
                            cm1.setText(strings[0][id]);
                            cm2.setText("");
                            selection1.clear();
                            selection2.clear();
                            for(int k = 0; k < strings.length; k++){
                                selection1.add(strings[k][id]);
                            }
                        }
                    }
                });
                tr.addView(txt);
            }
            table.addView(tr);
        }
        Log.i(TAG, "Initialised table");
    }

    // when 2 columns are selected, compare them with button click
    public void compare(){
        if(!selection1.isEmpty()&&!selection2.isEmpty()){
            // make data visually comparable
            Toast.makeText(MainActivity.this, "Comparison started", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(MainActivity.this, "Please select 2 columns to compare", Toast.LENGTH_SHORT).show();
        }
    }
}