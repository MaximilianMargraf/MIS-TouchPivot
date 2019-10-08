package com.example.mis_final_project_touchpivot;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //Table that will include all our data for KIS
    List<Movie> myData = new ArrayList<>();
    Context context = this;
    TextView cm1;
    TextView cm2;
    TableLayout table;
    Button compare;
    int last_row = 300;

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
        // the data comes from here_ https://www.tableau.com/sites/default/files/pages/movies.xlsx
        MovieReader movieReader = new MovieReader();
        movieReader.execute();
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
    private class MovieReader extends AsyncTask<SCpair, Integer, Context> {
        @Override
        protected Context doInBackground(SCpair... url_){
            try{
                // Get to the xls file in assets
                AssetManager am = MainActivity.this.getAssets();
                InputStream is = am.open("movies.xls");

                // create an HSSf workbook
                HSSFWorkbook workbook = new HSSFWorkbook(is);
                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                Log.i(TAG, "HSSF workbook created");

                HSSFSheet sheet = workbook.getSheetAt(0);
                StringBuilder sb = new StringBuilder();

                for(int i = 1; i < last_row; i++){
                    Row row = sheet.getRow(i);
                    for(int j = 0; j < 8; j++) {
                        String value = getCellAsString(row, j, formulaEvaluator);
                        sb.append(value + "§");
                    }
                }
                //Log.i(TAG, "String Builder done, on to parsing the string");
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
            String[][] strings = new String[last_row][8];
            strings[0][0] = "Title";
            strings[0][1] = "Ww. Gross";
            strings[0][2] = "Budget";
            strings[0][3] = "Released";
            strings[0][4] = "Genre";
            strings[0][5] = "Director";
            strings[0][6] = "Rotten Tomatoes";
            strings[0][7] = "IMDB";
            for(int i = 1; i <myData.size()+1;i++){
                for(int j = 0; j < 8; j++){
                    switch (j) {
                        case 0:
                            strings[i][j] = "" + myData.get(i-1).title_;
                            break;
                        case 1:
                            strings[i][j] = "" + myData.get(i-1).worldwide_gross_;
                            break;
                        case 2:
                            strings[i][j] = "" + myData.get(i-1).production_budget_;
                            break;
                        case 3:
                            strings[i][j] = "" + myData.get(i-1).year;
                            break;
                        case 4:
                            strings[i][j] = "" + myData.get(i-1).genre_;
                            break;
                        case 5:
                            strings[i][j] = "" + myData.get(i-1).directors_;
                            break;
                        case 6:
                            strings[i][j] = "" + myData.get(i-1).rotten_tomatoes_rating_;
                            break;
                        case 7:
                            strings[i][j] = "" + myData.get(i-1).imdb_rating_;
                            break;
                    }
                }
            }
            initTable(strings);
        }
    }

    // returns the value of the given columns
    private String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator){
        // this is the standard string if the cells are empty
        String value = "-";
        try{
            // in the given row access column c
            Cell cell = row.getCell(c);

            // check if cell is blank
            if(cell != null) {
                CellValue cellValue = formulaEvaluator.evaluate(cell);

                // boolean value in cell
                if (cellValue.getCellType() == CELL_TYPE_BOOLEAN) {
                    value = "" + cellValue.getBooleanValue();
                }

                // numeric value in cell
                else if (cellValue.getCellType() == CELL_TYPE_NUMERIC) {
                    Float numericValue = (float) cellValue.getNumberValue();
                    // date format recognized in cell
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
                        value = format.format(HSSFDateUtil.getJavaDate(numericValue));
                    } else {
                        value = "" + numericValue;
                    }
                }

                // string in cell
                else if (cellValue.getCellType() == CELL_TYPE_STRING) {
                    value = "" + cellValue.getStringValue();
                }
            }
        }
        catch (NullPointerException e){
            Log.e(TAG, "getCellAsString: NullPointerException: "+e.getMessage());
        }
        //Log.i(TAG, "Value: "+value);
        return value;
    }

    // split data and add it to the list
    public void parseStringBuilder(StringBuilder stringBuilder){
        String[] rows = stringBuilder.toString().split("§");
        try {
            if (rows.length > 1){
                for (int i = 0; i < rows.length; i += 8) {
                    String title_ = rows[i];

                    int worldwide_gross_ = 0;
                    if(rows[i+1]!="-"){
                        worldwide_gross_ = (int)Float.parseFloat(rows[i+1]);
                    }

                    int production_budget_ = 0;
                    if(rows[i+2]!="-"){
                        production_budget_ = (int)Float.parseFloat(rows[i+2]);
                    }

                    int release_date_ = 0;
                    if(rows[i+3]!="-"){
                        release_date_ = (int)Float.parseFloat(rows[i + 3]);
                    }

                    String genre_ = "Undefined";
                    if(rows[i+4]!="-"){
                        genre_ = rows[i+4];
                    }

                    String directors_ = "Unknown";
                    if(rows[i+5]!="-"){
                        directors_ = rows[i+5];
                    }

                    int rotten_tomatoes_rating_ = 0 ;
                    if(rows[i+6]!="-"){
                        rotten_tomatoes_rating_ = (int)Float.parseFloat(rows[i+6]);
                    }

                    float imdb_rating_ = 0;
                    if(rows[i+7]!="-"){
                        imdb_rating_ = Float.parseFloat(rows[i+7]);
                    }

                    //Log.i(TAG, "Title: "+title_+", Ww. Gross: "+worldwide_gross_+", Budget: "+production_budget_+", Released: "+release_date_+", Genre: "+genre_+", Directors: "+directors_);
                    Movie k = new Movie(title_, worldwide_gross_, production_budget_, release_date_, genre_, directors_, rotten_tomatoes_rating_, imdb_rating_);
                    myData.add(k);
                }
            }
            else{
                Log.i(TAG, "No entries in the Stringbuilder");
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
           /* String[][] strings = new String[selection1.size()][3];
            for(int i = 0; i < strings.length; i++){
                for(int j = 0; j <strings[0].length; j++){
                    if
                }
            }*/
        }
        // this functions like a reset tool, turning the table back to normal
        else{
            Toast.makeText(MainActivity.this, "Please select 2 columns to compare", Toast.LENGTH_SHORT).show();
        }
    }

    public void pivot(String s){

    }
}