package smartmoney;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link goalfragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class goalfragment extends Fragment {
    private PieChart mChart;
    private DBHelper _db;
    private int type;
    private long budgetStartMs, budgetEndMs;
    private OnFragmentInteractionListener mListener;


    public goalfragment() {
        // Required empty public constructor
    }

    public static goalfragment newInstance(Long param1, Long param2,int param3) {
        goalfragment fragment = new goalfragment();
        Bundle args = new Bundle();
        args.putLong("budgetStartMs", param1);
        args.putLong("budgetEndMs", param2);
        args.putInt("type",param3);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            budgetStartMs = getArguments().getLong("budgetStartMs");
            budgetEndMs = getArguments().getLong("budgetEndMs");
            type = getArguments().getInt("type");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.goal_fragment, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _db = new DBHelper(getContext());
        final ListView budgetList = (ListView)view.findViewById(R.id.list);
        final TextView helpText = (TextView)view.findViewById(R.id.helpText);
        mChart = (PieChart)view.findViewById(R.id.chart);
        //------------------- INITIALIZING CHART -------------------//
        mChart.setUsePercentValues(true );
        mChart.getDescription().setEnabled(true);
        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);


        //mChart.setCenterTextTypeface(mTfLight);
        mChart.setCenterText("");

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);
        mChart.setDrawEntryLabels(true);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        //------------------- END -------------------//
        //
        if(_db.getGoalCount() > 0)
        {
            budgetList.setVisibility(View.VISIBLE);
            helpText.setVisibility(View.GONE);
        }
        else
        {
            budgetList.setVisibility(View.GONE);
            mChart.setVisibility(View.GONE);
            helpText.setVisibility(View.VISIBLE);
            helpText.setText(R.string.noBudgets);
        }
        //
        final List<Goal> budgets = _db.getGoals(budgetStartMs, budgetEndMs);
        final GoalAdapter budgetListAdapter = new GoalAdapter(getContext(), budgets,type);
        budgetList.setAdapter(budgetListAdapter);

        setData(type,budgets.size(),100,mChart,budgets);
        registerForContextMenu(budgetList);

        budgetList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Goal budget = (Goal)parent.getItemAtPosition(position);
                if(budget == null)
                {
                    Log.w("", "Clicked budget at position " + position + " is null");
                    return;
                }

                Intent i = new Intent(getContext(), TransactionActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("budget", budget.name);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        //-------- DATE ----//
        final Calendar date = Calendar.getInstance();

        date.setTimeInMillis(budgetStartMs);
        String budgetStartString = DateFormat.getDateInstance(DateFormat.SHORT).format(date.getTime());

        date.setTimeInMillis(budgetEndMs);
        String budgetEndString = DateFormat.getDateInstance(DateFormat.SHORT).format(date.getTime());

        String dateRangeFormat = getResources().getString(R.string.dateRangeFormat);
        String dateRangeString = String.format(dateRangeFormat, budgetStartString, budgetEndString);

        final TextView dateRangeField = (TextView) view.findViewById(R.id.dateRange);
        dateRangeField.setText(dateRangeString);
        //-------- END -------//

        Goal blankBudget = _db.getBlankGoal(budgetStartMs, budgetEndMs);
        int max = 0;
        int current = 0;


        setupTotalEntry(view,budgets, blankBudget);
    }

    //------------------------------ ADDING DATA TO TOTAL PROGRESS BAR -----------------------------------//
    private void setupTotalEntry(View view, final List<Goal> budgets, final Goal blankBudget)
    {

        final TextView budgetName = (TextView)view.findViewById(R.id.budgetName);
        final TextView budgetValue = (TextView)view.findViewById(R.id.budgetValue);
        final ProgressBar budgetBar = (ProgressBar)view.findViewById(R.id.budgetBar);

        budgetName.setText(R.string.totalBudgetTitle);

        int max = 0;
        int current = 0;
        if(type ==1 ){
        for(Goal budget : budgets)
        {
            max += budget.max;
            current += budget.current;
        }


        //current += blankBudget.current;

        budgetBar.setMax(max);
        budgetBar.setProgress(current);

        String fraction = String.format(getResources().getString(R.string.fraction), current, max);
        budgetValue.setText(fraction);
        }
        else
        {
            for(Goal budget : budgets)
            {
                max += budget.max;
                current += budget.exp;
            }

            //current += blankBudget.current;

            budgetBar.setMax(max);
            budgetBar.setProgress(current);

            String fraction = String.format(getResources().getString(R.string.fraction), current, max);
            budgetValue.setText(fraction);

        }
    }
    //------------------------------ END -----------------------------------//

    //------------------------------- ADDING DATA TO CHART ----------------------------------------//
    //type = 1 == GENERAL // type = 2 == EXPENSE
    private void setData(int type ,int count, float range, PieChart mChart, List<Goal> budgets) {

        float mult = range;
        float max=0;
        float total = 0;
        float expenseTotal = 0;
        for(Goal g : budgets){
            max += g.current;
            total += g.max;
            expenseTotal += g.exp;}
        //------------------ ADDING -----------//
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        ArrayList<String> labels = new ArrayList<String>();
        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        PieDataSet dataSet = new PieDataSet(entries, "");
        if(type == 1){
            for (int i = 0; i < count ; i++) {

                entries.add(new PieEntry((float) (budgets.get(i).max-budgets.get(i).current)*mult / total, budgets.get(i % budgets.size()).name));
                labels.add(budgets.get(i).name);
            }
        }else if(type == 2){
            for (int i = 0; i < count ; i++) {

                entries.add(new PieEntry((float) (expenseTotal-budgets.get(i).exp)*mult / expenseTotal, budgets.get(i % budgets.size()).name));
                labels.add(budgets.get(i).name);
            }
        }

        //entries.add(new PieEntry((float) (total-max) / total*mult, "Lacking Fund"));
        //---------------- END ------------------//


        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add a lot of colors
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);




        // mChart.setUsePercentValues(false);
        // radius of the center hole in percent of maximum radius
        mChart.setHoleRadius(45f);
        mChart.setTransparentCircleRadius(50f);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);


        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(25f);
        data.setValueTextColor(Color.BLACK);
//        data.setValueTypeface(mTfLight);
        mChart.setData(data);
        mChart.setEntryLabelColor(Color.BLACK);
        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }
    //----------------------- END ------------------//
}
