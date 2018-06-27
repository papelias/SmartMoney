package smartmoney;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GoalActivity extends AppCompatActivity
{
    private final static String TAG = "BudgetWatch";

    private DBHelper _db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        _db = new DBHelper(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        final Calendar date = Calendar.getInstance();

        // Set to the last ms at the end of the month
        final long dateMonthEndMs = CalendarUtil.getEndOfMonthMs(date.get(Calendar.YEAR),
                date.get(Calendar.MONTH));

        // Set to beginning of the month
        final long dateMonthStartMs = CalendarUtil.getStartOfMonthMs(date.get(Calendar.YEAR),
                date.get(Calendar.MONTH));

        final Bundle b = getIntent().getExtras();
        final long budgetStartMs = b != null ? b.getLong("budgetStart", dateMonthStartMs) : dateMonthStartMs;
        final long budgetEndMs = b != null ? b.getLong("budgetEnd", dateMonthEndMs) : dateMonthEndMs;

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        //TITLE
        adapter.addFragment(goalfragment.newInstance(budgetStartMs,budgetEndMs,1), "GENERAL");
        adapter.addFragment(goalfragment.newInstance(budgetStartMs,budgetEndMs,2), "EXPENSE");

        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(viewPager);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.list)
        {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.view_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ListView listView = (ListView) findViewById(R.id.list);

        if(info != null)
        {
            Goal budget = (Goal) listView.getItemAtPosition(info.position);

            if (budget != null && item.getItemId() == R.id.action_edit)
            {
                Intent i = new Intent(getApplicationContext(), GoalViewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("id", budget.name);
                bundle.putBoolean("view", true);
                i.putExtras(bundle);
                startActivity(i);

                return true;
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.budget_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_add)
        {
            Intent i = new Intent(getApplicationContext(), GoalViewActivity.class);
            startActivity(i);
            return true;
        }

        if(id == R.id.action_calendar)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.budgetDateRangeHelp);

            final View view = getLayoutInflater().inflate(R.layout.budget_date_picker_layout, null, false);

            builder.setView(view);
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });
            builder.setPositiveButton(R.string.set, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    DatePicker startDatePicker = (DatePicker) view.findViewById(R.id.startDate);
                    DatePicker endDatePicker = (DatePicker) view.findViewById(R.id.endDate);

                    long startOfBudgetMs = CalendarUtil.getStartOfDayMs(startDatePicker.getYear(),
                            startDatePicker.getMonth(), startDatePicker.getDayOfMonth());
                    long endOfBudgetMs = CalendarUtil.getEndOfDayMs(endDatePicker.getYear(),
                            endDatePicker.getMonth(), endDatePicker.getDayOfMonth());

                    if (startOfBudgetMs > endOfBudgetMs)
                    {
                        Toast.makeText(GoalActivity.this, R.string.startDateAfterEndDate, Toast.LENGTH_LONG).show();
                        return;
                    }

                    Intent intent = new Intent(GoalActivity.this, GoalActivity.class);
                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);

                    Bundle bundle = new Bundle();
                    bundle.putLong("budgetStart", startOfBudgetMs);
                    bundle.putLong("budgetEnd", endOfBudgetMs);
                    intent.putExtras(bundle);
                    startActivity(intent);

                    GoalActivity.this.finish();
                }
            });

            builder.show();
            return true;
        }

        if(id == android.R.id.home)
        {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy()
    {
        _db.close();
        super.onDestroy();
    }



    //------------------- ADAPTER FOR THE TAB -----------------//
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}