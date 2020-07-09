package gov.nasa.gsfc.icesat2.icesat_2

interface ITimePickerCallback {
    fun datePicked(year: Int, month: Int, day: Int)
    fun timePicked(hour: Int, minute: Int)
}
