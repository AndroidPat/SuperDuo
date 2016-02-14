package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;


public class WidgetIntentService extends IntentService {


    public WidgetIntentService() {
        super("WidgetIntentService");
    }


    private static final String[] SCORES_COLUMNS = {
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.MATCH_DAY
    };
    // these indices must match the projection

    private double detail_match_id = 0;
    private static final int COL_DATE = 1;
    private static final int COL_MATCHTIME = 2;
    private static final int COL_HOME = 3;
    private static final int COL_AWAY = 4;
    private static final int COL_LEAGUE = 5;
    private static final int COL_HOME_GOALS = 6;
    private static final int COL_AWAY_GOALS = 7;
    private static final int COL_ID = 8;
    private static final int COL_MATCHDAY = 9;

    String home = "";
    String away = "";
    String score = "";
    String time = "";

    @Override
    protected void onHandleIntent(Intent intent) {

        ArrayList<String> homes = new ArrayList<>();
        ArrayList<String> aways = new ArrayList<>();
        ArrayList<String> times = new ArrayList<>();
        ArrayList<Integer> h_goals = new ArrayList<>();
        ArrayList<Integer> a_goals = new ArrayList<>();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                WidgetProvider.class));


        Date fragmentDate = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String today = format.format(fragmentDate);


        Uri uri = DatabaseContract.scores_table.buildScoreWithDate();


        Cursor data = getContentResolver().query(uri,
                SCORES_COLUMNS,
                null,
                new String[]{today},
                null);


        //widget would open application even if no scores are available for 'today'
        int layoutId = R.layout.widget_score;
        RemoteViews views = new RemoteViews(getPackageName(), layoutId);
        for (int appWidgetId : appWidgetIds) {
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }


        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        //displaying the most recent game if the score is not blank
        int i = -2;
        data.moveToPosition(-1);
        while (data.moveToNext()) {

            homes.add(data.getString(COL_HOME));
            aways.add(data.getString(COL_AWAY));
            times.add(data.getString(COL_MATCHTIME));
            h_goals.add(data.getInt(COL_HOME_GOALS));
            a_goals.add(data.getInt(COL_AWAY_GOALS));
            i = i + 1;
            if ((data.getInt(COL_HOME_GOALS) < 0 || data.getInt(COL_AWAY_GOALS) < 0)) {
                if (i < 0) {
                    i = 0;
                }
                break;
            }
        }
        data.close();

        home = homes.get(i);
        away = aways.get(i);
        time = times.get(i);
        score = (Utilities.getScores(h_goals.get(i), a_goals.get(i)));



        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            // Create an Intent to launch MainActivity


            views.setTextViewText(R.id.home_name_widget, home);
            views.setTextViewText(R.id.score_textview_widget, score);
            views.setTextViewText(R.id.away_name_widget, away);
            views.setTextViewText(R.id.data_textview_widget, time);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                views.setContentDescription(R.id.home_name_widget, home);
                views.setContentDescription(R.id.score_textview_widget, score);
                views.setContentDescription(R.id.away_name_widget, away);
                views.setContentDescription(R.id.data_textview_widget, time);
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }
}

