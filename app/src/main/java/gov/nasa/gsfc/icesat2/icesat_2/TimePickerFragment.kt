package gov.nasa.gsfc.icesat2.icesat_2

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val TAG = "TimePickerFragment"

class TimePickerFragment(private val activity: Activity) : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private lateinit var listener: ITimePickerCallback


    fun setListener(listener: ITimePickerCallback) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, false)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        Log.d(TAG, "Time set as $hourOfDay: $minute")
        listener.timePicked(hourOfDay, minute)
    }
}

class DatePickerFragment(private val activity: Activity) : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private lateinit var listener: ITimePickerCallback
    private var year = 0
    private var month = 0
    private var day = 0

    fun setListener(listener: ITimePickerCallback, year: Int, month: Int, day: Int) {
        this.listener = listener
        this.year = year
        this.month = month
        this.day = day
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        Log.d(TAG, "Date set as $year $month $day")

        listener.datePicked(year, month, day)
    }

}