package me.simple.picker.datepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.widget.TextPickerLinearLayout
import me.simple.picker.utils.PickerUtils
import java.util.*
import kotlin.IllegalArgumentException

open class DatePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextPickerLinearLayout(context, attrs, defStyleAttr) {

    val yearPickerView = YearPickerView(context)
    val monthPickerView = MonthPickerView(context)
    val dayPickerView = DayPickerView(context)

    private var mOnDateSelectedListener: ((
        year: String,
        month: String,
        day: String
    ) -> Unit)? = null

    private var mStartYear: Int = PickerUtils.START_YEAR
    private var mStartMonth: Int = PickerUtils.START_YEAR
    private var mStartDay: Int = PickerUtils.START_YEAR

    private var mEndYear: Int = PickerUtils.getCurrentYear()
    private var mEndMonth: Int = PickerUtils.getCurrentMonth()
    private var mEndDay: Int = PickerUtils.getCurrentDay()


    init {
        weightSum = 3f

        addViewInLayout(yearPickerView, 0, generateDefaultLayoutParams(), true)
        addViewInLayout(monthPickerView, 1, generateDefaultLayoutParams(), true)
        addViewInLayout(dayPickerView, 2, generateDefaultLayoutParams(), true)
        requestLayout()

        resetLayoutManager()

        setDateInterval()
    }

    override fun resetLayoutManager() {
        super.resetLayoutManager()
        setListener()
    }

    /**
     *
     */
    private fun setListener() {
        yearPickerView.removeAllOnItemSelectedListener()
        yearPickerView.addOnSelectedItemListener { position ->
            val year = yearPickerView.getYear()
            when (year) {
                mStartYear -> {
                    monthPickerView.setMonthInterval(mStartMonth)
                }
                mEndYear -> {
                    monthPickerView.setMonthInterval(endMonth = mEndMonth)
                }
                else -> {
                    monthPickerView.setMonthInterval()
                }
            }

            yearPickerView.post {
                val month = monthPickerView.getMonth()
                setDayInterval(year, month)

                dispatchOnItemSelected()
            }
        }

        monthPickerView.removeAllOnItemSelectedListener()
        monthPickerView.addOnSelectedItemListener { position ->
            val year = yearPickerView.getYear()
            val month = monthPickerView.getMonth()

            setDayInterval(year, month)

            dispatchOnItemSelected()
        }

        dayPickerView.removeAllOnItemSelectedListener()
        dayPickerView.addOnSelectedItemListener { position ->
            dispatchOnItemSelected()
        }
    }

    /**
     * 设置日期-天的区间
     */
    private fun setDayInterval(
        year: Int,
        month: Int
    ) {
        if (year == mStartYear && month == mStartMonth) {
            val endDay = PickerUtils.getDayCountInMonth(year, month)
            dayPickerView.setDayInterval(mStartDay, endDay)
        } else if (year == mEndYear && month == mEndMonth) {
            dayPickerView.setDayInterval(endDay = mEndDay)
        } else {
            dayPickerView.setDayIntervalByMonth(year, month)
        }
    }

    /**
     *
     */
    private fun dispatchOnItemSelected() {
        this.post {
            val year = yearPickerView.getYearStr()
            val month = monthPickerView.getMonthStr()
            val day = dayPickerView.getDayStr()

            mOnDateSelectedListener?.invoke(year, month, day)
        }
    }

    /**
     * 设置日期时间的区间
     */
    fun setDateInterval(
        start: Calendar = PickerUtils.getStartCalendar(),
        end: Calendar = PickerUtils.getCurrentCalendar()
    ) {
        setDateInterval(
            PickerUtils.getYear(start), PickerUtils.getMonth(start), PickerUtils.getDay(start),
            PickerUtils.getYear(end), PickerUtils.getMonth(end), PickerUtils.getDay(end)
        )
    }

    /**
     * 设置日期时间的区间
     */
    fun setDateInterval(
        startYear: Int = PickerUtils.START_YEAR,
        startMonth: Int = PickerUtils.START_MONTH,
        startDay: Int = PickerUtils.START_DAY,

        endYear: Int = PickerUtils.getCurrentYear(),
        endMonth: Int = PickerUtils.getCurrentMonth(),
        endDay: Int = PickerUtils.getCurrentDay()
    ) {
        this.mStartYear = startYear
        this.mStartMonth = startMonth
        this.mStartDay = startDay

        this.mEndYear = endYear
        this.mEndMonth = endMonth
        this.mEndDay = endDay

        yearPickerView.setYearInterval(startYear, endYear)

        if (mScrollToEnd) {
            monthPickerView.setMonthInterval(endMonth = endMonth)
        } else {
            monthPickerView.setMonthInterval(startMonth)
        }

        if (mScrollToEnd) {
            dayPickerView.setDayInterval(endDay = endDay)
        } else {
            dayPickerView.setDayInterval(
                startDay,
                PickerUtils.getDayCountInMonth(startYear, startMonth)
            )
        }

        if (mScrollToEnd) {
            scrollToEnd()
        }
    }

    /**
     * 选中当前时间的那个item
     */
    @Deprecated("方法名不合理", ReplaceWith("selectedCurrentDateItem"))
    fun scrollToCurrentDate() {
        selectedCurrentDateItem()
    }

    /**
     * 选中当前时间的那个item
     */
    fun selectedCurrentDateItem() {
        val currentCalendar = PickerUtils.getCurrentCalendar()
        setSelectedItem(currentCalendar)
    }

    /**
     *
     */
    fun setSelectedItem(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date
        setSelectedItem(calendar)
    }

    /**
     *
     */
    fun setSelectedItem(calendar: Calendar) {
        val year = PickerUtils.getYear(calendar)
        val month = PickerUtils.getMonth(calendar)
        val day = PickerUtils.getDay(calendar)
        setSelectedItem(year, month, day)
    }

    /**
     *
     */
    fun setSelectedItem(
        year: Int,
        month: Int,
        day: Int
    ) {
        if (year < mStartYear || year > mEndYear) {
            throw IllegalArgumentException("year must be >= $mStartYear and <= $mEndYear")
        }

        if (year == mStartYear && month < mStartMonth) {
            throw IllegalArgumentException("month must be >= $mStartMonth")
        }
        if (year == mEndYear && month > mEndMonth) {
            throw IllegalArgumentException("month must be <= $mEndMonth")
        }

        if (year == mStartYear && month == mStartMonth && day < mStartDay) {
            throw IllegalArgumentException("day must be >= $mStartDay")
        }
        if (year == mEndYear && month == mEndMonth && day > mEndDay) {
            throw IllegalArgumentException("day must be <= $mEndDay")
        }

        yearPickerView.post {
            yearPickerView.selectedItem(year)
        }
        monthPickerView.post {
            monthPickerView.selectedItem(month)
        }
        dayPickerView.post {
            dayPickerView.selectedItem(day)
        }
    }


    /**
     * 获取当前选中时间的Calendar
     */
    fun getCalendar(): Calendar = Calendar.getInstance().apply {
        set(
            yearPickerView.getYear(), monthPickerView.getMonth() - 1, dayPickerView.getDay(),
            0, 0, 0
        )
    }

    /**
     * 获取当前选中时间的Date
     */
    fun getDate(): Date = getCalendar().time

    /**
     * 获取年月日的字符串数组
     */
    fun getYearMonthDay() = arrayOf(
        yearPickerView.getYearStr(),
        monthPickerView.getMonthStr(),
        dayPickerView.getDayStr()
    )

    /**
     * 日期选中的监听
     */
    fun setOnDateSelectedListener(onSelected: (year: String, month: String, day: String) -> Unit) {
        this.mOnDateSelectedListener = onSelected
    }

}