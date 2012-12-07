package com.example.calendarsample;
 
import android.annotation.TargetApi;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Instances;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
 
public class CalendarListFragment extends ListFragment implements
    LoaderManager.LoaderCallbacks, ViewBinder {
    SimpleCursorAdapter mAdapter;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("スケジュールはありません");
        String[] from;
 
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            from = new String[] { Instances.TITLE, Instances.BEGIN };
        } else {
            from = new String[] { Instances.TITLE, Instances.BEGIN };
        }
 
        mAdapter = new SimpleCursorAdapter(getActivity(),
            android.R.layout.simple_list_item_2, null, from,
            new int[] { android.R.id.text1, android.R.id.text2 }, 0);
        mAdapter.setViewBinder(this);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
    }
    @TargetApi(14)
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String tz = Time.TIMEZONE_UTC;
        Time time = new Time(tz);
        time.setToNow();
        time.allDay = true;
        time.year = time.year - 1;
        time.month = 0;
        time.monthDay = 1;
        time.hour = 0;
        time.minute = 0;
        time.second = 0;
        int begin = Time.getJulianDay(time.toMillis(true), 0);
        time.year += 4;
        time.month = 11;
        time.monthDay = 31;
        int end = Time.getJulianDay(time.toMillis(true), 0);
        Uri content_by_day_uri;
        String[] instance_projection;
        String sort_order;
 
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            content_by_day_uri =
                CalendarContract.Instances.CONTENT_BY_DAY_URI;
            instance_projection = new String[] {
                Instances._ID,
                Instances.EVENT_ID,
                Instances.BEGIN,
                Instances.END,
                Instances.TITLE
            };
            sort_order =
                Instances.BEGIN + " ASC, " + Instances.END + " DESC, "
                    + Instances.TITLE + " ASC";
        } else {
            final String authority = "com.android.calendar";
            content_by_day_uri = Uri.parse("content://" + authority
                + "/instances/whenbyday");
            instance_projection = new String[] {
                "_id",
                "event_id",
                "begin",
                "end",
                "title"
            };
            sort_order = "begin ASC, end DESC, title ASC";
        }
        Uri baseUri = buildQueryUri(begin, end, content_by_day_uri);
        return new CursorLoader(getActivity(), baseUri,
                instance_projection, null,
                null, sort_order);
    }
    @Override
    public void onLoadFinished(Loader loader, Object data) {
        mAdapter.swapCursor((Cursor)data);
    }
    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.swapCursor(null);
    }
 
    @Override
    public boolean setViewValue(View view, Cursor cursor, int index) {
        Log.v("debug", "index:" + String.valueOf(index));
        switch (index) {
        case 4:
            ((TextView)view).setText(cursor.getString(index));
            return true;
        case 2:
            String text = cursor.getString(index);
            Time time = new Time();
            time.set(Long.parseLong(text));
            ((TextView)view).setText(time.format3339(false));
            return true;
        default:
            break;
        }
        return false;
    }
    private Uri buildQueryUri(int start, int end, Uri content_by_day_uri) {
        StringBuilder path = new StringBuilder();
        path.append(start);
        path.append('/');
        path.append(end);
        Uri uri =
            Uri.withAppendedPath(content_by_day_uri, path.toString());
        return uri;
    }
}