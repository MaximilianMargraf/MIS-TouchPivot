package com.example.mis_final_project_touchpivot;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //Table that will include all our data for KIS
    List<Movie> myData = new ArrayList<>();
    List<PerYear> myData2 = new ArrayList<>();
    List<PerGenre> myData3 = new ArrayList<>();
    List<PerDirector> myData4 = new ArrayList<>();
    Context context = this;
    TextView cm1;
    Button back;
    TableLayout table;
    int last_row = 101;
    Utility util = new Utility();
    RelativeLayout r1, r2;
    LineChart lineChart1, lineChart2;
    BarChart barChart1, barChart2;
    PieChart pieChart1, pieChart2;
    protected Typeface tfLight;

    final int[] MY_COLORS = { // Add Custom Color for chart
            Color.rgb(255,0,68), // red 0
            Color.rgb(255,145,0), // orange 1
            Color.rgb(255,239,0), // yellow 2
            Color.rgb(46,232,32), // green 3
            Color.rgb(25,202,238), // blue 4
            Color.rgb(184,37,213), // purple 5
            Color.rgb(188,143,143), // rosy brown 6
            Color.rgb(171,39,79), // 7
            Color.rgb(0,0,204), // dark blue 8
            Color.rgb(1,186,118), // 9
            Color.rgb(0,206,209), // dark turquoise
            Color.rgb(255,102,255)}; // highlight (pastel red) 10

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //look device in landscape mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        // link design elements
        table = (TableLayout)findViewById(R.id.table_main);
        cm1 = (TextView)findViewById(R.id.textView);
        back = (Button)findViewById(R.id.button);
        r1 = (RelativeLayout)findViewById(R.id.relativeLayout_top);
        r2 = (RelativeLayout)findViewById(R.id.relativeLayout_bottom);

        lineChart1 = (LineChart)findViewById(R.id.lineChart1);
        lineChart2 = (LineChart)findViewById(R.id.lineChart2);

        barChart1 = (BarChart)findViewById(R.id.barChart1);
        barChart2 = (BarChart)findViewById(R.id.barChart2);

        pieChart1 = (PieChart)findViewById(R.id.pieChart1);
        pieChart2 = (PieChart)findViewById(R.id.pieChart2);
        initChart();

        // check for storage permissions (I think this became obsolete)
        //checkPermission();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MovieReader().execute();
                cm1.setText("Title");
            }
        });

        // read in table from URL with Async Task
        // the data comes from here https://www.tableau.com/sites/default/files/pages/movies.xlsx
        MovieReader movieReader = new MovieReader();
        movieReader.execute();
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
    private class MovieReader extends AsyncTask<Void, Integer, Context> {
        @Override
        protected Context doInBackground(Void... voids){
            try{
                // Get to the xls file in assets
                if(myData.size()==0) {
                    AssetManager am = MainActivity.this.getAssets();
                    InputStream is = am.open("movies.xls");

                    // create an HSSf workbook
                    HSSFWorkbook workbook = new HSSFWorkbook(is);
                    FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    Log.i(TAG, "HSSF workbook created");

                    HSSFSheet sheet = workbook.getSheetAt(0);
                    StringBuilder sb = new StringBuilder();

                    for (int i = 1; i < last_row; i++) {
                        Row row = sheet.getRow(i);
                        for (int j = 0; j < 8; j++) {
                            String value = getCellAsString(row, j, formulaEvaluator);
                            sb.append(value + "§");
                        }
                    }
                    //Log.i(TAG, "String Builder done, on to parsing the string");
                    parseStringBuilder(sb);
                }
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
            table.removeAllViews();
            String[][] strings = util.initMovieArray(myData);
            initTable(strings);

            // prepare the other data for pivoting, only do this once though, not more often
            if(myData2.size()==0) {
                for (int i = 1905; i < 2024; i++) {
                    // check if the year exists in the Dataset
                    for (int j = 0; j < myData.size(); j++) {
                        if (myData.get(j).year == i) {
                            myData2.add(new PerYear(i, myData));
                            break;
                        }
                    }
                }
            }

            if(myData3.size()==0) {
                List<String> genres = util.initGenres();
                for (String s : genres) {
                    myData3.add(new PerGenre(s, myData));
                }
            }

            if(myData4.size()==0){
                List<String> directors = util.initDirectors(myData);
                Log.i(TAG, "Directors size: " + directors.size());
                for (String s : directors) {
                    myData4.add(new PerDirector(s, myData));
                }
            }
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
                    int worldwide_gross_  = (int)Float.parseFloat(rows[i+1]);
                    int production_budget_  = (int)Float.parseFloat(rows[i+2]);
                    int release_date_ = (int)Float.parseFloat(rows[i + 3]);
                    String genre_ = rows[i+4];
                    String directors_ = rows[i+5];
                    int rotten_tomatoes_rating_ = (int)Float.parseFloat(rows[i+6]);
                    float imdb_rating_ = Float.parseFloat(rows[i+7]);

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
                txt.setPadding(5,2,5,2);
                // On click listener for columns
                txt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int id = txt.getId();
                        cm1.setText(strings[0][id]);

                        // show movies per year in line chart
                        if(strings[0][id].equals("Year")) {
                            // if both views are not filled yet
                            if ((lineChart1.getVisibility() == View.GONE && barChart1.getVisibility() == View.GONE
                                    && pieChart1.getVisibility() == View.GONE) && (lineChart2.getVisibility() == View.GONE
                                    && barChart2.getVisibility() == View.GONE && pieChart2.getVisibility() == View.GONE)) {

                                moviesPerYear(lineChart1);
                            }
                            else if ((lineChart1.getVisibility() == View.VISIBLE || barChart1.getVisibility() == View.VISIBLE
                                    || pieChart1.getVisibility() == View.VISIBLE) && (lineChart2.getVisibility() == View.GONE
                                    && barChart2.getVisibility() == View.GONE && pieChart2.getVisibility() == View.GONE)) {

                                moviesPerYear(lineChart2);
                            }
                            // charts already filled
                            else if ((lineChart1.getVisibility() == View.VISIBLE || barChart1.getVisibility() == View.VISIBLE
                                    || pieChart1.getVisibility() == View.VISIBLE) && (lineChart2.getVisibility() == View.VISIBLE
                                    || barChart2.getVisibility() == View.VISIBLE || pieChart2.getVisibility() == View.VISIBLE)) {
                                clearCharts();
                            }
                        }
                        // amount of movies per genre
                        if(strings[0][id].equals("Genre")) {
                            if ((lineChart1.getVisibility() == View.GONE && barChart1.getVisibility() == View.GONE
                                    && pieChart1.getVisibility() == View.GONE) && (lineChart2.getVisibility() == View.GONE
                                    && barChart2.getVisibility() == View.GONE && pieChart2.getVisibility() == View.GONE)) {

                                moviesPerGenre(barChart1);
                            }
                            else if ((lineChart1.getVisibility() == View.VISIBLE || barChart1.getVisibility() == View.VISIBLE
                                    || pieChart1.getVisibility() == View.VISIBLE) && (lineChart2.getVisibility() == View.GONE
                                    && barChart2.getVisibility() == View.GONE && pieChart2.getVisibility() == View.GONE)) {

                                moviesPerGenre(barChart2);
                            }
                            else if ((lineChart1.getVisibility() == View.VISIBLE || barChart1.getVisibility() == View.VISIBLE
                                    || pieChart1.getVisibility() == View.VISIBLE) && (lineChart2.getVisibility() == View.VISIBLE
                                    || barChart2.getVisibility() == View.VISIBLE || pieChart2.getVisibility() == View.VISIBLE)) {
                                clearCharts();
                            }
                        }
                        if(strings[0][id].equals("Director")) {
                            if ((lineChart1.getVisibility() == View.GONE && barChart1.getVisibility() == View.GONE
                                    && pieChart1.getVisibility() == View.GONE) && (lineChart2.getVisibility() == View.GONE
                                    && barChart2.getVisibility() == View.GONE && pieChart2.getVisibility() == View.GONE)) {

                                moviesPerDirector(pieChart1);
                            }
                            else if ((lineChart1.getVisibility() == View.VISIBLE || barChart1.getVisibility() == View.VISIBLE
                                    || pieChart1.getVisibility() == View.VISIBLE) && (lineChart2.getVisibility() == View.GONE
                                    && barChart2.getVisibility() == View.GONE && pieChart2.getVisibility() == View.GONE)) {

                                moviesPerDirector(pieChart2);
                            }
                            else if ((lineChart1.getVisibility() == View.VISIBLE || barChart1.getVisibility() == View.VISIBLE
                                    || pieChart1.getVisibility() == View.VISIBLE) && (lineChart2.getVisibility() == View.VISIBLE
                                    || barChart2.getVisibility() == View.VISIBLE || pieChart2.getVisibility() == View.VISIBLE)) {
                                clearCharts();
                            }
                        }
                        // show rt imdb score over years
                        if(strings[0][0].equals("Year") && (strings[0][id].equals("Avg. RT") || strings[0][id].equals("Avg. Imdb"))) {
                            if ((lineChart1.getVisibility() == View.GONE && barChart1.getVisibility() == View.GONE
                                    && pieChart1.getVisibility() == View.GONE) && (lineChart2.getVisibility() == View.GONE
                                    && barChart2.getVisibility() == View.GONE && pieChart2.getVisibility() == View.GONE)) {

                                imdb_rt_score_years(lineChart1);
                            }
                            else if ((lineChart1.getVisibility() == View.VISIBLE || barChart1.getVisibility() == View.VISIBLE
                                    || pieChart1.getVisibility() == View.VISIBLE) && (lineChart2.getVisibility() == View.GONE
                                    && barChart2.getVisibility() == View.GONE && pieChart2.getVisibility() == View.GONE)) {

                                imdb_rt_score_years(lineChart2);
                            }
                            else if ((lineChart1.getVisibility() == View.VISIBLE || barChart1.getVisibility() == View.VISIBLE
                                    || pieChart1.getVisibility() == View.VISIBLE) && (lineChart2.getVisibility() == View.VISIBLE
                                    || barChart2.getVisibility() == View.VISIBLE || pieChart2.getVisibility() == View.VISIBLE)) {
                                clearCharts();
                            }
                        }
                    }
                });

                txt.setOnLongClickListener(new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View view){
                        int id = txt.getId();
                        if(strings[0][id].equals("Year")){
                            table.removeAllViews();
                            String[][] strings = util.initPerYearArray(myData2);
                            initTable(strings);
                            cm1.setText("Year");
                        }
                        if(strings[0][id].equals("Genre") || strings[0][id].equals("Dom. genre") || strings[0][id].equals("Fav. genre")){
                            table.removeAllViews();
                            String[][] strings = util.initPerGenreArray(myData3);
                            initTable(strings);
                            cm1.setText("Genre");
                        }
                        if(strings[0][id].equals("Director")){
                            table.removeAllViews();
                            String[][] strings = util.initPerDirectorArray(myData4);
                            initTable(strings);
                            cm1.setText("Directors");
                        }
                        if(strings[0][id].equals("Year")){

                        }
                        return true;
                    }
                });
                tr.addView(txt);
            }
            table.addView(tr);
        }
        Log.i(TAG, "Initialised table");
    }
    public void initChart(){
        // enable scaling and dragging
        lineChart1.setDragEnabled(true);
        lineChart1.setScaleEnabled(true);
        // force pinch zoom along both axis
        lineChart1.setPinchZoom(true);
        lineChart1.setVisibility(View.GONE);

        // enable scaling and dragging
        lineChart2.setDragEnabled(true);
        lineChart2.setScaleEnabled(true);
        // force pinch zoom along both axis
        lineChart2.setPinchZoom(true);
        lineChart2.setVisibility(View.GONE);

        barChart1.setVisibility(View.GONE);
        barChart2.setVisibility(View.GONE);

        pieChart1.setVisibility(View.GONE);
        pieChart2.setVisibility(View.GONE);
    }

    public void clearCharts(){
        lineChart1.clear();
        lineChart2.clear();

        barChart1.clear();
        barChart2.clear();

        pieChart1.clear();
        pieChart2.clear();

        lineChart1.setVisibility(View.GONE);
        lineChart2.setVisibility(View.GONE);

        barChart1.setVisibility(View.GONE);
        barChart2.setVisibility(View.GONE);

        pieChart1.setVisibility(View.GONE);
        pieChart2.setVisibility(View.GONE);
    }

    public void moviesPerYear(LineChart l){
        l.setVisibility(View.VISIBLE);
        l.getDescription().setEnabled(false);
        l.setDrawGridBackground(false);
        l.getXAxis().setDrawAxisLine(false);

        //prep entries
        ArrayList<Entry> entries = new ArrayList<>();
        for (PerYear perYear : myData2) {
            entries.add(new Entry(perYear.year_, perYear.moviesPerYear_));
        }

        XAxis xAxis = l.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(tfLight);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setLabelCount(entries.size());
        xAxis.setLabelCount(118);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-90);

        YAxis leftAxis = l.getAxisLeft();
        leftAxis.setLabelCount(5, false);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = l.getAxisRight();
        rightAxis.setLabelCount(5, false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);

        // make line chart with entries
        LineDataSet dataSet = new LineDataSet(entries, "Movies per Year");
        //dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        dataSet.setColor(MY_COLORS[5]);
        dataSet.setCircleColor(MY_COLORS[0]);
        dataSet.setCircleRadius(3.5f);
        dataSet.setValueTextSize(8f);

        LineData lineData = new LineData(dataSet);
        l.setData(lineData);
        l.animateY(100);

        l.invalidate(); // refresh
    }

    public void moviesPerGenre(BarChart b){
        //final int[] colors = {Color.rgb(255, 255, 255), };

        b.setVisibility(View.VISIBLE);
        b.getDescription().setEnabled(false);
        b.setMaxVisibleValueCount(10);

        List<BarEntry> entries = new ArrayList<>();
        for(int i = 0; i < myData3.size(); i++){
            entries.add(new BarEntry(i, myData3.get(i).amount_movies_));
        }
        BarDataSet set = new BarDataSet(entries, "Movies per genre");

        set.setColors(MY_COLORS);

        BarData data = new BarData(set);
        data.setBarWidth(0.9f); // set custom bar width
        b.setData(data);
        b.setFitBars(true); // make the x-axis fit exactly all bars

        List<String> xAxisLabel = new ArrayList<>(Arrays.asList( "Action", "Adventure",
                "Black Comedy", "Comedy", "Concert", "Crime", "Documentary",
                "Drama", "Horror", "Musical", "Romantic Comedy", "Romantic Drama",
                "Thriller", "Western"));
        XAxis xAxis = b.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabel));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(xAxisLabel.size());
        xAxis.setLabelRotationAngle(-60);

        b.getAxisLeft().setDrawGridLines(false);
        b.animateY(100);
        b.invalidate(); // refresh
    }

    public void moviesPerDirector(PieChart p){
        p.setVisibility(View.VISIBLE);
        // total amount of movies
        List<PieEntry> entries = new ArrayList<>();
        for(PerDirector d : myData4){
            if(d.movies_director_>=2){
                float percent = d.movies_director_;//(d.movies_director_*100)/movies;
                entries.add(new PieEntry(percent, d.name_));
            }
        }

        PieDataSet set = new PieDataSet(entries, "Movies per Director (with more than 2 Movies)");
        set.setColors(MY_COLORS);

        PieData data = new PieData(set);
        p.setHoleRadius(50f);
        p.setTransparentCircleRadius(45f);
        p.setData(data);
        p.animateY(250);
        p.setEntryLabelColor(Color.BLACK);
        p.setEntryLabelTextSize(10f);
        set.setSliceSpace(0.8f);

        p.invalidate();
    }

    public void imdb_rt_score_years(LineChart l){
        l.setVisibility(View.VISIBLE);
        l.getDescription().setEnabled(false);
        l.setDrawGridBackground(false);
        l.getXAxis().setDrawAxisLine(false);

        //prep entries
        List<Entry> entries1 = new ArrayList<>();
        for (PerYear perYear : myData2) {
            entries1.add(new Entry(perYear.year_, perYear.averageIMDBScore_*10));
        }
        List<Entry> entries2 = new ArrayList<>();
        for (PerYear perYear : myData2) {
            entries2.add(new Entry(perYear.year_, perYear.averageTomatoeScore_));
        }

        XAxis xAxis = l.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(tfLight);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setLabelCount(entries1.size());
        xAxis.setLabelCount(118);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-90);

        YAxis leftAxis = l.getAxisLeft();
        leftAxis.setLabelCount(5, false);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = l.getAxisRight();
        rightAxis.setLabelCount(5, false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);

        // make line chart with entries
        LineDataSet dataSet1 = new LineDataSet(entries1, "Average IMDb score (*10)");
        LineDataSet dataSet2 = new LineDataSet(entries2, "Average Rotten Tomatoes score");

        dataSet1.setColor(MY_COLORS[5]);
        dataSet1.setCircleColor(MY_COLORS[0]);
        dataSet1.setCircleRadius(1.5f);
        dataSet1.setValueTextSize(8f);

        dataSet2.setColor(MY_COLORS[4]);
        dataSet2.setCircleColor(MY_COLORS[1]);
        dataSet2.setCircleRadius(1.5f);
        dataSet2.setValueTextSize(8f);

        List<ILineDataSet> datas = new ArrayList<>();
        datas.add(dataSet1);
        datas.add(dataSet2);

        LineData lineData = new LineData(datas);
        l.setData(lineData);
        l.animateY(100);
        l.invalidate(); // refresh
    }
}