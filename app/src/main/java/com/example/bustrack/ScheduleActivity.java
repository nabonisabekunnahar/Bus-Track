package com.example.bustrack;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bustrack.Model.BusSchedule;

import android.content.Context;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BusScheduleAdapter adapter;
    private List<BusSchedule> busScheduleList = new ArrayList<>();
    private String shift;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        // Get the selected shift from the Intent
        shift = getIntent().getStringExtra("shift");

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BusScheduleAdapter(this, busScheduleList);
        recyclerView.setAdapter(adapter);

        // Start the scraping task to get the schedule for the selected shift
        new ScrapeBusScheduleTask().execute();
    }

    private class ScrapeBusScheduleTask extends AsyncTask<Void, Void, List<BusSchedule>> {
        @Override
        protected List<BusSchedule> doInBackground(Void... params) {
            List<BusSchedule> scheduleList = new ArrayList<>();

            try {
                // Load the HTML file from your local assets or a remote URL
                Document doc = Jsoup.connect("https://www.kuet.ac.bd/office/transportation").get();

                // Get data for the selected shift only
                scheduleList.addAll(getScheduleForShift(doc, "#" + shift));

            } catch (IOException e) {
                e.printStackTrace();
            }

            return scheduleList;
        }

        @Override
        protected void onPostExecute(List<BusSchedule> result) {
            // Update the data in the adapter
            busScheduleList.clear();
            busScheduleList.addAll(result);
            adapter.notifyDataSetChanged();
        }

        private List<BusSchedule> getScheduleForShift(Document doc, String shiftId) {
            List<BusSchedule> shiftSchedule = new ArrayList<>();
            Elements tableRows = doc.select(shiftId + " table tbody tr");

            for (Element row : tableRows) {
                Elements columns = row.select("td");
                if (columns.size() >= 4) { // Ensure there are enough columns
                    String tripName = columns.get(0).text();
                    String startTime = columns.get(1).text();
                    String startSpot = columns.get(2).text();
                    String remarks = columns.get(3).text();

                    shiftSchedule.add(new BusSchedule(tripName, startTime, startSpot, remarks));
                }
            }
            return shiftSchedule;
        }
    }
}
