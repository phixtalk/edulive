package com.edulive;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;


public class PollChartActivity extends AppCompatActivity {
    String userid, pollid,question,option_1,option_2,option_3;
    String votes_1, votes_2, votes_3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_chart);

        //show back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get values passed on from previous page
        Bundle recdData = getIntent().getExtras();
        pollid = recdData.getString("pollid");
        question = recdData.getString("question");
        option_1 = recdData.getString("option1");
        option_2 = recdData.getString("option2");
        option_3 = recdData.getString("option3");

        votes_1 = recdData.getString("count_a");
        votes_2 = recdData.getString("count_b");
        votes_3 = recdData.getString("count_c");

        findViewById(R.id.chart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                showChart();
            }
        });
    }

    //make back button work
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChart()
    {
        // Creating an  XYSeries for options
        XYSeries option1Series = new XYSeries(option_1);
        XYSeries option2Series = new XYSeries(option_2);
        XYSeries option3Series = new XYSeries(option_3);
        //XYSeries option4Series = new XYSeries(option_4);

        // Adding data to option Series
        option1Series.add(0,0);
        option2Series.add(0,0);
        option3Series.add(0,0);
        //option4Series.add(0,0);
        option2Series.add(1,0);
        option1Series.add(2,Integer.parseInt(votes_1));
        option3Series.add(3,0);
        //option4Series.add(4,0);
        option1Series.add(5,0);
        option2Series.add(6,Integer.parseInt(votes_2));
        option3Series.add(7,0);
        //option4Series.add(8,0);
        option1Series.add(9,0);
        option3Series.add(10,Integer.parseInt(votes_3));
        option2Series.add(11,0);
        //option4Series.add(12,0);
        /*
        option1Series.add(13,0);
        //option4Series.add(14,Integer.parseInt(votes_4));
        option2Series.add(15,0);
        option3Series.add(16,0);
        */
        /*
        if (option_3.isEmpty())
        {
            option1Series.add(0,0);
            option2Series.add(0,0);
            option3Series.add(0,0);
            option4Series.add(0,0);
            option2Series.add(1,0);
            option3Series.add(2,0);
            option4Series.add(3,0);
            option1Series.add(4,Integer.parseInt(votes_1));
            option1Series.add(5,0);
            option2Series.add(6,0);
            option3Series.add(7,0);
            option4Series.add(8,0);
            option1Series.add(9,0);
            option2Series.add(10,Integer.parseInt(votes_2));
            option3Series.add(11,0);
            option4Series.add(12,0);
            option1Series.add(13,0);
            option2Series.add(14,0);
            option3Series.add(15,0);
            option4Series.add(16,0);
        }
        else if (option_4.isEmpty())
        {
            option1Series.add(0,0);
            option2Series.add(0,0);
            option3Series.add(0,0);
            option4Series.add(0,0);
            option2Series.add(1,0);
            option3Series.add(2,0);
            option4Series.add(3,0);
            option1Series.add(4,Integer.parseInt(votes_1));
            option1Series.add(5,0);
            option3Series.add(6,0);
            option2Series.add(7,Integer.parseInt(votes_2));
            option4Series.add(8,0);
            option1Series.add(9,0);
            option3Series.add(10,Integer.parseInt(votes_3));
            option2Series.add(11,0);
            option4Series.add(12,0);
            option1Series.add(13,0);
            option2Series.add(14,0);
            option3Series.add(15,0);
            option4Series.add(16,0);
        }
        else
        {
            option1Series.add(0,0);
            option2Series.add(0,0);
            option3Series.add(0,0);
            //option4Series.add(0,0);
            option2Series.add(1,0);
            option1Series.add(2,Integer.parseInt(votes_1));
            option3Series.add(3,0);
            //option4Series.add(4,0);
            option1Series.add(5,0);
            option2Series.add(6,Integer.parseInt(votes_2));
            option3Series.add(7,0);
            //option4Series.add(8,0);
            option1Series.add(9,0);
            option3Series.add(10,Integer.parseInt(votes_3));
            option2Series.add(11,0);
            //option4Series.add(12,0);
            option1Series.add(13,0);
            //option4Series.add(14,Integer.parseInt(votes_4));
            option2Series.add(15,0);
            option3Series.add(16,0);
        }
        */

        // Creating a dataset to hold each series
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

        // Adding option Series to the dataset
        dataset.addSeries(option1Series);
        dataset.addSeries(option2Series);
        dataset.addSeries(option3Series);


        /*
        if (!option_3.isEmpty())
            dataset.addSeries(option3Series);

        if (!option_4.isEmpty())
            dataset.addSeries(option4Series);
        */

        // Creating XYSeriesRenderer to customize option1 Series
        XYSeriesRenderer option1Renderer = new XYSeriesRenderer();
        option1Renderer.setColor(Color.rgb(130, 130, 230));
        option1Renderer.setFillPoints(true);
        option1Renderer.setLineWidth(60);
        option1Renderer.setDisplayChartValues(false);

        // Creating XYSeriesRenderer to customize option2 Series
        XYSeriesRenderer option2Renderer = new XYSeriesRenderer();
        option2Renderer.setColor(Color.rgb(255, 0, 0));
        option2Renderer.setFillPoints(true);
        option2Renderer.setLineWidth(60);
        option2Renderer.setDisplayChartValues(false);

        // Creating XYSeriesRenderer to customize option3 Series
        XYSeriesRenderer option3Renderer = new XYSeriesRenderer();
        option3Renderer.setColor(Color.rgb(76, 153, 0));
        option3Renderer.setFillPoints(true);
        option3Renderer.setLineWidth(60);
        option3Renderer.setDisplayChartValues(false);

        /*
        // Creating XYSeriesRenderer to customize option4 Series
        XYSeriesRenderer option4Renderer = new XYSeriesRenderer();
        option4Renderer.setColor(Color.rgb(255, 128, 0));
        option4Renderer.setFillPoints(true);
        option4Renderer.setLineWidth(60);
        option4Renderer.setDisplayChartValues(false);
        */

        // Creating a XYMultipleSeriesRenderer to customize the whole chart
        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
        multiRenderer.setXLabels(0);
        multiRenderer.setChartTitle("Poll's Results");
        multiRenderer.setXTitle("Options");
        multiRenderer.setYTitle("Voters");
        multiRenderer.setZoomButtonsVisible(true);

//    	// Adding text to X label
//    	multiRenderer.addXTextLabel(5, option_1);
//    	multiRenderer.addXTextLabel(10, option_2);
//    	multiRenderer.addXTextLabel(15, option_3);
//    	multiRenderer.addXTextLabel(20, option_4);


        // Adding options Renderer to multipleRenderer
        // Note: The order of adding dataseries to dataset and renderers to multipleRenderer
        // should be same
        multiRenderer.addSeriesRenderer(option1Renderer);
        multiRenderer.addSeriesRenderer(option2Renderer);
        multiRenderer.addSeriesRenderer(option3Renderer);


        /*
        if (!option_3.isEmpty())
            multiRenderer.addSeriesRenderer(option3Renderer);

        if (!option_4.isEmpty())
            multiRenderer.addSeriesRenderer(option4Renderer);

        */


        Intent chart = ChartFactory.getBarChartIntent(getBaseContext(), dataset, multiRenderer, Type.DEFAULT);
        startActivity(chart);
    }
}
