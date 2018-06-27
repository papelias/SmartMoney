package smartmoney;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import java.util.LinkedList;
import java.util.List;


public class MainActivity extends AppCompatActivity
{
    static class MenuAdapter extends ArrayAdapter<MainMenuItem>
    {

        public MenuAdapter(Context context, List<MainMenuItem> items)
        {
            super(context, 0, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            // Get the data item for this position
            MainMenuItem item = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view

            if (convertView == null)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.main_button,
                        parent, false);
            }

            TextView menuText = (TextView) convertView.findViewById(R.id.menu);
            TextView menuDescText = (TextView) convertView.findViewById(R.id.menudesc);
            ImageView icon = (ImageView) convertView.findViewById(R.id.image);

            menuText.setText(item.menuTextId);
            menuDescText.setText(item.menuDescId);
            //icon.setImageResource(item.iconId);

            return convertView;
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }



    private final static String TAG = "GoalWatch";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        List<MainMenuItem> menuItems = new LinkedList<>();
        menuItems.add(new MainMenuItem(/*R.drawable.purse, */R.string.budgetsTitle,
                R.string.budgetDescription));
        menuItems.add(new MainMenuItem(/*R.drawable.transaction, */R.string.transactionsTitle,
                R.string.transactionsDescription));

        final ListView buttonList = (ListView) findViewById(R.id.list);
        final MenuAdapter buttonListAdapter = new MenuAdapter(this, menuItems);
        buttonList.setAdapter(buttonListAdapter);
        buttonList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                MainMenuItem item = (MainMenuItem)parent.getItemAtPosition(position);
                if(item == null)
                {
                    Log.w(TAG, "Clicked menu item at position " + position + " is null");
                    return;
                }

                Class goalClass = null;

                switch(item.menuTextId)
                {
                    case R.string.budgetsTitle:
                        goalClass = GoalActivity.class;
                        break;
                    case R.string.transactionsTitle:
                        goalClass = TransactionActivity.class;
                        break;
                    default:
                        Log.w(TAG, "Unexpected menu text id: " + item.menuTextId);
                        break;
                }

                if(goalClass != null)
                {
                    Intent i = new Intent(getApplicationContext(), goalClass);
                    startActivity(i);
                }
            }
        });

    }

    static class MainMenuItem
    {
        //public final int iconId;
        public final int menuTextId;
        public final int menuDescId;

        public MainMenuItem(/*int iconId, */int menuTextId, int menuDescId)
        {
            //this.iconId = iconId;
            this.menuTextId = menuTextId;
            this.menuDescId = menuDescId;
        }
    }

}
