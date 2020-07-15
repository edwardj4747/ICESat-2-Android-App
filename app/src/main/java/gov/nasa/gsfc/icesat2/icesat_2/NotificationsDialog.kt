package gov.nasa.gsfc.icesat2.icesat_2

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class NotificationsDialog : DialogFragment() {
    private lateinit var listener: MarkerSelectedFragment

    fun setListener(listener: MarkerSelectedFragment) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val selectedItems = ArrayList<Int>() // Where we track the selected items
            val builder = AlertDialog.Builder(it)
            // Set the dialog title
            builder.setTitle(R.string.remindMe)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(R.array.notificationOptions, null,
                    DialogInterface.OnMultiChoiceClickListener { dialog, which, isChecked ->
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            selectedItems.add(which)
                        } else if (selectedItems.contains(which)) {
                            // Else, if the item is already in the array, remove it
                            selectedItems.remove(Integer.valueOf(which))
                        }
                    })
                // Set the action buttons
                .setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        // User clicked OK, so save the selectedItems results somewhere
                        // or return them to the component that opened the dialog
                        if (this::listener.isInitialized) {
                            listener.notificationOptionsChosen(selectedItems)
                        }

                    })
                .setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->

                    })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}