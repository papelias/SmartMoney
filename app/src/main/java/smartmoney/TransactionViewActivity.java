package smartmoney;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class TransactionViewActivity extends AppCompatActivity
{
    private static final String TAG = "GoalWatch";

    static final String ACTION_NEW_EXPENSE = "ActionAddExpense";

    private String capturedUncommittedReceipt = null;
    private DBHelper _db;

    private EditText _nameEdit;
    private TextView _nameView;
    private EditText _accountEdit;
    private TextView _accountView;
    private EditText _valueEdit;
    private TextView _valueView;
    private EditText _noteEdit;
    private TextView _noteView;
    private TextView _budgetView;
    private TextView _dateView;
    private Button _viewButton;
    private Button _updateButton;
    private View _endingDivider;
    private EditText _dateEdit;
    private Spinner _budgetSpinner;

    private int _transactionId;
    private int _type;
    private boolean _updateTransaction;
    private boolean _viewTransaction;

    private void extractIntentFields(Intent intent)
    {
        final Bundle b = intent.getExtras();
        String action = intent.getAction();
        if(b != null)
        {
            _transactionId = b.getInt("id");
            _type = b.getInt("type");
            _updateTransaction = b.getBoolean("update", false);
            _viewTransaction = b.getBoolean("view", false);
        }
        else if(action != null)
        {
            _updateTransaction = false;
            _viewTransaction = false;

            if(action.equals(ACTION_NEW_EXPENSE))
            {
                _type = DBHelper.TransactionDbIds.EXPENSE;
            } else
            {
                Log.d(TAG, "Unsupported action '" + action + "', bailing");
                finish();
            }
        }
        else
        {
            Log.d(TAG, "Launched TransactionViewActivity without arguments, bailing");
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.transaction_view_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        _db = new DBHelper(this);

        _nameEdit = (EditText) findViewById(R.id.nameEdit);
        _nameView = (TextView) findViewById(R.id.nameView);
        _accountEdit = (EditText) findViewById(R.id.accountEdit);
        _accountView = (TextView) findViewById(R.id.accountView);
        _valueEdit = (EditText) findViewById(R.id.valueEdit);
        _valueView = (TextView) findViewById(R.id.valueView);
        _noteEdit = (EditText) findViewById(R.id.noteEdit);
        _noteView = (TextView) findViewById(R.id.noteView);
        _budgetView = (TextView) findViewById(R.id.budgetView);
        _dateView = (TextView) findViewById(R.id.dateView);
        _viewButton = (Button)findViewById(R.id.viewButton);
        _updateButton = (Button)findViewById(R.id.updateButton);
        _endingDivider = findViewById(R.id.endingDivider);
        _dateEdit = (EditText) findViewById(R.id.dateEdit);
        _budgetSpinner = (Spinner) findViewById(R.id.budgetSpinner);
        _accountView.setVisibility(View.GONE);
        extractIntentFields(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        Log.i(TAG, "Received new intent");
        extractIntentFields(intent);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onResume()
    {
        super.onResume();

        if(_type == DBHelper.TransactionDbIds.EXPENSE)
        {
            if (_updateTransaction)
            {
                setTitle(R.string.editExpenseTransactionTitle);
            }
            else if (_viewTransaction)
            {
                setTitle(R.string.viewExpenseTransactionTitle);
            }
            else
            {
                setTitle(R.string.addExpenseTransactionTitle);
            }
        }
        else if(_type == DBHelper.TransactionDbIds.REVENUE)
        {
            if(_updateTransaction)
            {
                setTitle(R.string.editRevenueTransactionTitle);
            }
            else if(_viewTransaction)
            {
                setTitle(R.string.viewRevenueTransactionTitle);
            }
            else
            {
                setTitle(R.string.addRevenueTransactionTitle);
            }
        }

        final Calendar date = new GregorianCalendar();
        final DateFormat dateFormatter = SimpleDateFormat.getDateInstance();

        _dateEdit.setText(dateFormatter.format(date.getTime()));

        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day)
            {
                date.set(year, month, day);
                _dateEdit.setText(dateFormatter.format(date.getTime()));
            }
        };

        _dateEdit.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    int year = date.get(Calendar.YEAR);
                    int month = date.get(Calendar.MONTH);
                    int day = date.get(Calendar.DATE);
                    DatePickerDialog datePicker = new DatePickerDialog(TransactionViewActivity.this,
                            dateSetListener, year, month, day);
                    datePicker.show();
                }
            }
        });

        List<String> actualBudgetNames = _db.getGoalNames();
        LinkedList<String> budgetNames = new LinkedList<>(actualBudgetNames);

        // A user is not allowed to create a blank budget. However, here
        // an empty budget is injected, so that a user can create a
        // transaction that has no budget.
        budgetNames.addFirst("");

        // Add budget items to spinner if it has not been initialized yet
        if(_budgetSpinner.getCount() == 0)
        {
            ArrayAdapter<String> budgets = new ArrayAdapter<>(this, R.layout.spinner_textview, budgetNames);
            _budgetSpinner.setAdapter(budgets);
        }

        if(_updateTransaction || _viewTransaction)
        {
            Transaction transaction = _db.getTransaction(_transactionId);
            (_updateTransaction ? _nameEdit : _nameView).setText(transaction.description);
            (_updateTransaction ? _accountEdit : _accountView).setText(transaction.account);

            int budgetIndex = budgetNames.indexOf(transaction.budget);
            if(budgetIndex >= 0)
            {
                _budgetSpinner.setSelection(budgetIndex);
            }
            _budgetView.setText(_viewTransaction ? transaction.budget : "");

            (_updateTransaction ? _valueEdit : _valueView).setText(String.format(Locale.US, "%.2f", transaction.value));
            (_updateTransaction ? _noteEdit : _noteView).setText(transaction.note);
            (_updateTransaction ? _dateEdit : _dateView).setText(dateFormatter.format(new Date(transaction.dateMs)));
/*
            _receiptLocationField.setText(transaction.receipt);
*/

            if(_viewTransaction)
            {
                _budgetSpinner.setVisibility(View.GONE);
                _nameEdit.setVisibility(View.GONE);
                _accountEdit.setVisibility(View.GONE);
                _valueEdit.setVisibility(View.GONE);
                _noteEdit.setVisibility(View.GONE);
                _dateEdit.setVisibility(View.GONE);

            }
            else
            {
                _budgetView.setVisibility(View.GONE);
                _nameView.setVisibility(View.GONE);
                _accountView.setVisibility(View.GONE);
                _valueView.setVisibility(View.GONE);
                _noteView.setVisibility(View.GONE);
                _dateView.setVisibility(View.GONE);

            }
        }
        else
        {
            _budgetView.setVisibility(View.GONE);
            _nameView.setVisibility(View.GONE);
            _accountView.setVisibility(View.GONE);
            _valueView.setVisibility(View.GONE);
            _noteView.setVisibility(View.GONE);
            _dateView.setVisibility(View.GONE);

        }

    }

    private void doSave()
    {
        final String name = _nameEdit.getText().toString();
        // name field is optional, so it is OK if it is empty

        final String budget = (String)_budgetSpinner.getSelectedItem();
        if (budget == null)
        {
            Snackbar.make(_budgetSpinner, R.string.budgetMissing, Snackbar.LENGTH_LONG).show();
            return;
        }
        Goal goal  = new DBHelper(this).getGoal(budget);
        Toast.makeText(getApplicationContext(),  goal.name + " " + goal.current + " " + goal.max + " " + _type, Toast.LENGTH_SHORT).show();
        final String account = _accountEdit.getText().toString();
        // The account field is optional, so it is OK if it is empty

        final String valueStr = _valueEdit.getText().toString();
        if (valueStr.isEmpty())
        {
            Snackbar.make(_valueEdit, R.string.valueMissing, Snackbar.LENGTH_LONG).show();
            return;
        }else if(((goal.max - (goal.current + Integer.parseInt(valueStr))) < 0)  && (_type == DBHelper.TransactionDbIds.REVENUE)){
            Toast.makeText(getApplicationContext(), "Goal achieved! Excess amount for goal is P" + ((goal.max - (goal.current + Integer.parseInt(valueStr))) - goal.max), Toast.LENGTH_LONG).show();
        }else if(((goal.current - Integer.parseInt(valueStr)) < 0) && (_type == DBHelper.TransactionDbIds.EXPENSE)){
            Snackbar.make(_valueEdit, "Expense cannot be added!", Snackbar.LENGTH_LONG).show();
            return;
        }

        else if ((goal.max == (goal.current + Integer.parseInt(valueStr))) && (_type == DBHelper.TransactionDbIds.REVENUE)) {
            Toast.makeText(getApplicationContext(), "You have successfully completed a goal!", Toast.LENGTH_SHORT).show();
        }


        double value;
        try
        {
            value = Double.parseDouble(valueStr);
        }
        catch (NumberFormatException e)
        {
            Snackbar.make(_valueEdit, R.string.valueInvalid, Snackbar.LENGTH_LONG).show();
            return;
        }

        final String note = _noteEdit.getText().toString();
        // The note field is optional, so it is OK if it is empty

        final String dateStr = _dateEdit.getText().toString();
        final DateFormat dateFormatter = SimpleDateFormat.getDateInstance();
        long dateMs;
        try
        {
            dateMs = dateFormatter.parse(dateStr).getTime();
        }
        catch (ParseException e)
        {
            Snackbar.make(_dateEdit, R.string.dateInvalid, Snackbar.LENGTH_LONG).show();
            return;
        }

        if(_updateTransaction)
        {

            _db.updateTransaction(_transactionId, _type, name, account,
                    budget, value, note, dateMs);

        }
        else
        {
            _db.insertTransaction(_type, name, account, budget,
                    value, note, dateMs);
        }

        finish();
    }


    @Override
    protected void onDestroy()
    {
        if(capturedUncommittedReceipt != null)
        {
            // The receipt was captured but never used
            Log.i(TAG, "Deleting unsaved image: " + capturedUncommittedReceipt);
            File unneededReceipt = new File(capturedUncommittedReceipt);
            if(unneededReceipt.delete() == false)
            {
                Log.e(TAG, "Unable to delete unnecessary file: " + capturedUncommittedReceipt);
            }
            capturedUncommittedReceipt = null;
        }

        _db.close();

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if(_viewTransaction)
        {
            getMenuInflater().inflate(R.menu.view_menu, menu);
        }
        else if(_updateTransaction)
        {
            getMenuInflater().inflate(R.menu.edit_menu, menu);
        }
        else
        {
            getMenuInflater().inflate(R.menu.add_menu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == R.id.action_save)
        {
            doSave();
            return true;
        }

        if(id == android.R.id.home)
        {
            finish();
            return true;
        }

        if(id == R.id.action_edit)
        {
            finish();

            Intent i = new Intent(getApplicationContext(), TransactionViewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("id", _transactionId);
            bundle.putInt("type", _type);
            bundle.putBoolean("update", true);
            i.putExtras(bundle);
            startActivity(i);
            return true;
        }

        if(id == R.id.action_delete)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.deleteTransactionTitle);
            builder.setMessage(R.string.deleteTransactionConfirmation);
            builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    Log.e(TAG, "Deleting transaction: " + _transactionId);

                    _db.deleteTransaction(_transactionId);
                    finish();

                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private File getNewImageLocation()
    {
        File imageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if(imageDir == null)
        {
            Log.e(TAG, "Failed to locate directory for pictures");
            Toast.makeText(this, R.string.pictureCaptureError, Toast.LENGTH_LONG).show();
            return null;
        }

        if(imageDir.exists() == false)
        {
            if(imageDir.mkdirs() == false)
            {
                Log.e(TAG, "Failed to create receipts image directory");
                Toast.makeText(this, R.string.pictureCaptureError, Toast.LENGTH_LONG).show();
                return null;
            }
        }

        UUID imageFilename = UUID.randomUUID();
        File receiptFile = new File(imageDir, imageFilename.toString() + ".jpg");

        return receiptFile;
    }

    private boolean reencodeImageWithQuality(String original, int quality)
    {
        File destFile = new File(original);
        File tmpLocation = getNewImageLocation();

        try
        {
            if (tmpLocation == null)
            {
                throw new IOException("Could not create location for tmp file");
            }

            boolean created = tmpLocation.createNewFile();
            if (created == false)
            {
                throw new IOException("Could not create tmp file");
            }

            Bitmap bitmap = BitmapFactory.decodeFile(original);
            FileOutputStream fOut = new FileOutputStream(tmpLocation);
            boolean fileWritten = bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fOut);
            fOut.flush();
            fOut.close();

            if (fileWritten == false)
            {
                throw new IOException("Could not down compress file");
            }

            boolean renamed = tmpLocation.renameTo(destFile);
            if (renamed == false)
            {
                throw new IOException("Could not move converted file");
            }

            Log.i(TAG, "Image file " + original + " saved at quality " + quality);

            return true;
        }
        catch(IOException e)
        {
            Log.e(TAG, "Failed to encode image", e);

            for(File item : new File[]{tmpLocation, destFile})
            {
                if(item != null)
                {
                    boolean result = item.delete();
                    if(result == false)
                    {
                        Log.w(TAG, "Failed to delete image file: " + item.getAbsolutePath());
                    }
                }
            }
        }

        return false;
    }


}
