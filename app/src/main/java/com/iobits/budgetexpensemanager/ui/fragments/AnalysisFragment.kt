package com.iobits.budgetexpensemanager.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.FragmentAnalysisBinding
import com.iobits.budgetexpensemanager.managers.AdsManager
import com.iobits.budgetexpensemanager.managers.AnalyticsManager
import com.iobits.budgetexpensemanager.myApplication.MyApplication
import com.iobits.budgetexpensemanager.ui.adapters.TransactionTabAdapter
import com.iobits.budgetexpensemanager.ui.dataModels.GraphDataModel
import com.iobits.budgetexpensemanager.ui.dataModels.Transaction
import com.iobits.budgetexpensemanager.ui.viewModels.DataShareViewModel
import com.iobits.budgetexpensemanager.ui.viewModels.MainViewModel
import com.iobits.budgetexpensemanager.utils.CustomBarChartRender
import com.iobits.budgetexpensemanager.utils.gone
import com.iobits.budgetexpensemanager.utils.handleBackPress
import com.iobits.budgetexpensemanager.utils.safeNavigate
import com.iobits.budgetexpensemanager.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


class AnalysisFragment : Fragment() {
    private val TAG = "AnalysisFragmentTAG"
    private val mainViewModel: MainViewModel by activityViewModels()
    private val dataShareViewModel: DataShareViewModel by activityViewModels()
    val binding by lazy {
        FragmentAnalysisBinding.inflate(layoutInflater)
    }
    private var tabAdapter: TransactionTabAdapter? = null
    private var transactionList = ArrayList<Transaction>()
    var currentFilter = 0

    var startDate :Date? = null
    var endDate :Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        AnalyticsManager.logEvent("User_Is_In_Analysis_Screen",null)
        initViews()
        initRv()
        performNavigation()
        return binding.root
    }

    private fun initViews() {
        /** Init Charts */
        transactionGetter()
        pieChart()

            loadAds()

//      groupBarChart(mainViewModel.categoryTotalTransactions)
    }
    private fun loadAds() {
        MyApplication.mInstance.adsManager.loadNativeAd(
            requireActivity(),
            binding.adView,
            AdsManager.NativeAdType.NOMEDIA_MEDIUM,
            getString(R.string.ADMOB_NATIVE_WITHOUT_MEDIA_V2),
            binding.shimmerLayout
        )    }

    private fun initRv() {
        tabAdapter = TransactionTabAdapter(requireContext())
        binding.topSelectionRv.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = tabAdapter
        }
        tabAdapter?.updateList(dataShareViewModel.tabList)

        /** Top selector listener */
        tabAdapter?.onClick = {
            mainViewModel.apply {
                if(transactionList.isNotEmpty()){
                    categoryTotalTransactions.clear()
                    when (it) {
                        0 -> {
                            currentFilter = 0
                            lifecycleScope.launch(Dispatchers.IO) {
                                calculateSpendingDaily(transactionList)
                                calculateGraphData(mainViewModel.categoryTotalTransactions)
                                calculatePieGraphData(mainViewModel.categoryTotalTransactions)
                                Log.d(TAG, "initRv: ${mainViewModel.graphDataList}")
                                groupBarChart(mainViewModel.graphDataList)
                                lineChart(mainViewModel.graphDataList)
                                pieChart()
                            }
                        }

                        1 -> {
                            currentFilter = 1
                            lifecycleScope.launch {
                                calculateSpendingWeekly(transactionList)
                                calculateGraphData(mainViewModel.categoryTotalTransactions)
                                calculatePieGraphData(mainViewModel.categoryTotalTransactions)
                                Log.d(TAG, "initRv: ${mainViewModel.graphDataList}")
                                groupBarChart(mainViewModel.graphDataList)
                                lineChart(mainViewModel.graphDataList)
                                pieChart()
                            }
                        }

                        2 -> {
                            currentFilter = 2
                            lifecycleScope.launch {
                                calculateSpendingMonthly(transactionList)
                                calculateGraphData(mainViewModel.categoryTotalTransactions)
                                calculatePieGraphData(mainViewModel.categoryTotalTransactions)
                                Log.d(TAG, "initRv: ${mainViewModel.graphDataList}")
                                groupBarChart(mainViewModel.graphDataList)
                                lineChart(mainViewModel.graphDataList)
                                pieChart()
                            }
                        }

                        3 -> {
                            currentFilter = 3
                            lifecycleScope.launch {
                                calculateSpendingYearly(transactionList)
                                calculateGraphData(mainViewModel.categoryTotalTransactions)
                                calculatePieGraphData(mainViewModel.categoryTotalTransactions)
                                Log.d(TAG, "initRv: ${mainViewModel.graphDataList}")
                                groupBarChart(mainViewModel.graphDataList)
                                lineChart(mainViewModel.graphDataList)
                                pieChart()
                            }
                        }
                        4 -> {
                            binding.progress.visible()
                            datePicker()
                        }
                    }
                }
            }
        }
    }
    private fun datePicker() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setTitleText("Select Starting and Ending Dates")
        val constraintsBuilder = CalendarConstraints.Builder()

        val currentMillis = System.currentTimeMillis()
        constraintsBuilder.setStart(currentMillis - TimeUnit.DAYS.toMillis(365 * 3)) // Example: Set minimum year to 5 years ago
        constraintsBuilder.setEnd(currentMillis + TimeUnit.DAYS.toMillis(365 * 3))   // Example: Set maximum year to 5 years in the future

        builder.setCalendarConstraints(constraintsBuilder.build())

        val materialDatePicker = builder.build()
        materialDatePicker.show(requireActivity().supportFragmentManager, "tagone")
        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            startDate = Date(selection.first)
            endDate = Date(selection.second)

            val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
            val formattedStartDate = dateFormat.format(startDate)
            val formattedEndDate = dateFormat.format(endDate)

            lifecycleScope.launch {
                mainViewModel.calculateSpendingDaily(transactionList)
                val myList =  mainViewModel.filterTransactionsByDate(mainViewModel.categoryTotalTransactions,formattedStartDate,formattedEndDate)
                binding.progress.gone()
                currentFilter = 4
                mainViewModel.categoryTotalTransactions.apply {
                clear()
                addAll(myList)
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    mainViewModel.calculateGraphData(mainViewModel.categoryTotalTransactions)
                    mainViewModel.calculatePieGraphData(mainViewModel.categoryTotalTransactions)
                    Log.d(TAG, "initRv: ${mainViewModel.graphDataList}")
                    groupBarChart(mainViewModel.graphDataList)
                    lineChart(mainViewModel.graphDataList)
                    pieChart()
                }

                Log.d(TAG, "datePicker SORTED LIST:$myList ")
            }
        }
        materialDatePicker.addOnDismissListener {
            binding.progress.gone()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun transactionGetter() {
        mainViewModel.getAccount().observe(viewLifecycleOwner, Observer { account ->
            if (account != null) {
                binding.apply {
                    incomeTv.text = "+" + account.income
                    expenseTv.text = "-" + account.expense
                }
                lifecycleScope.launch {
                    if(account.transactions.isNotEmpty()){
                    transactionList = account.transactions
                    Log.d(TAG, "transactionSorter: ${account.transactions} ")
                    lifecycleScope.launch(Dispatchers.IO) {
                        mainViewModel.apply {
                            categoryTotalTransactions.clear()
                            calculateSpendingDaily(account.transactions)
                            calculateGraphData(mainViewModel.categoryTotalTransactions)
                            calculatePieGraphData(mainViewModel.categoryTotalTransactions)
                            Log.d(TAG, "initRv: ${mainViewModel.graphDataList}")
                            withContext(Dispatchers.Main) {
                                groupBarChart(mainViewModel.graphDataList)
                                lineChart(mainViewModel.graphDataList)
                                pieChart()
                            }
                        }
                    }
                    }
                }
            }
        }
        )
    }

    override fun onResume() {
        super.onResume()
        handleBackPress {
            Log.d(TAG, "onResume: handleBackPress Working")
            mainViewModel.apply {
                onNavItemSelected?.invoke(1)
                navBarSetter?.invoke(1)
            }
        }
    }

    private fun performNavigation() {
        mainViewModel.onNavItemSelected = {
            when (it) {
                1 -> {
                    safeNavigate(
                        R.id.action_analysisFragment_to_homeFragment,
                        R.id.analysisFragment
                    )
                }

                2 -> {
                    safeNavigate(
                        R.id.action_analysisFragment_to_transactionsFragment,
                        R.id.analysisFragment
                    )
                }

                3 -> {}
                4 -> {
                    safeNavigate(
                        R.id.action_analysisFragment_to_budgetFragment,
                        R.id.analysisFragment
                    )
                }
            }
        }
    }

    //----------------------------------------- Charts -------------------------------------------//
    private fun groupBarChart(modelList: ArrayList<GraphDataModel>) {
        modelList.reverse()
        val barChart = binding.combinedChart
        val incomeValues = ArrayList<BarEntry>()
        val expenseValues = ArrayList<BarEntry>()
        val dateLabels = ArrayList<String>()
        for ((index, model) in modelList.withIndex()) {
            incomeValues.add(BarEntry(index.toFloat(), model.income))
            expenseValues.add(BarEntry(index.toFloat(), model.expense))
            val formattedDate = model.date
            dateLabels.add(formattedDate)
        }
        val incomeDataSet = BarDataSet(incomeValues, "Income")
        incomeDataSet.color = ContextCompat.getColor(requireContext(), R.color.green_bar)

        val expenseDataSet = BarDataSet(expenseValues, "Expense")
        expenseDataSet.color = ContextCompat.getColor(requireContext(), R.color.red_bar)

        val barData = BarData(incomeDataSet, expenseDataSet)
//        barData.barWidth = 0.4f
//        val groupSpace = 0.04f
//        val barSpace = 0.2f
//        if (modelList.size > 1) {
//            if (modelList.size < 3) {
//                barData.barWidth = 0.2f
//            }
//            barData.groupBars(0f, groupSpace, barSpace)
//        } else {
//            barData.barWidth = 0.1f
//            barData.groupBars(0f, 0.02f, 0f)
//        }

        val groupSpace = 0.2f
        val barSpace = 0.08f
        if (modelList.size > 1) {
            barData.barWidth = 0.2f
            barData.groupBars(0f, groupSpace, barSpace)
        } else {
            barData.barWidth = 0.1f
            barData.groupBars(0f, 0.02f, 0.05f)

        }

        val barChartRender =
            CustomBarChartRender(barChart, barChart.animator, barChart.viewPortHandler)
        barChartRender.setRadius(40)
        barChart.renderer = barChartRender
        barChart.data = barData

        val maxValue = modelList.mapNotNull { it.date }.maxOrNull() ?: 0

        // Customize the appearance of the chart
        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)

        val xAxis = barChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // Set the granularity to 1 to show all values
        xAxis.setDrawLabels(true)
        xAxis.isGranularityEnabled = true
        xAxis.position = XAxis.XAxisPosition.BOTTOM // Set the X-axis position to the bottom
        xAxis.textSize = 5f
        barChart.setVisibleXRangeMaximum(4f)
        xAxis.valueFormatter = IndexAxisValueFormatter(dateLabels) // Set custom date labels
        barChart.isDoubleTapToZoomEnabled = false
        barChart.setTouchEnabled(true)
        barChart.isDragEnabled = true

        val yAxis = barChart.axisLeft
//        yAxis.axisMinimum = 20f
//        yAxis.axisMaximum = if (maxValue > 200) {
//            maxValue.toFloat() + 20
//        } else 200f

        //     yAxis.setLabelCount(8, false)
        // Hide the right Y-axis
        barChart.axisRight.isEnabled = false

        // Refresh the chart
        barChart.invalidate()
    }
    private fun pieChart() {
        val pieChart: PieChart = binding.pieChart
        val entries = mutableListOf<PieEntry>()
        mainViewModel.pieDataList.forEach {
            entries.add(PieEntry(it.amount, it.category))
        }

        val dataSet = PieDataSet(entries, "Categories")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        val pieData = PieData(dataSet)
        pieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            data = pieData
        }
        if(entries.isNotEmpty()){
        // Find the index of the entry with the maximum amount
        val maxAmountIndex = mainViewModel.pieDataList.indexOfFirst { it.amount == mainViewModel.pieDataList.maxByOrNull { it.amount }?.amount }

        // Programmatically highlight the entry with the biggest value
        pieChart.highlightValues(arrayOf(Highlight(maxAmountIndex.toFloat(), 0, 0)))
        }

        // Refresh chart to apply changes
        pieChart.invalidate()
    }
    private fun lineChart(modelList: ArrayList<GraphDataModel>) {
        modelList.reverse()
        modelList.add(0, GraphDataModel(0f, 0f, "0-0-0"))
        val lineChart = binding.lineChart

        // Create sample data entries
        val entries = ArrayList<Entry>()
        modelList.forEachIndexed { index, it ->

            if (currentFilter != 2) {
                val date = it.date.split("-")
                entries.add(Entry(date[0].toFloat(), it.expense))
            } else {
                entries.add(Entry(index.toFloat(), it.expense))
            }
        }

        // Create a dataset and customize it
        val dataSet = LineDataSet(entries, "Expense")
        dataSet.apply {
            color = ContextCompat.getColor(requireContext(), R.color.red_bar)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.red_bar))
            lineWidth = 2f
            circleRadius = 4f
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        // Customize X-axis and Y-axis
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        val yAxisRight = lineChart.axisRight
        yAxisRight.isEnabled = false

        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.setDrawGridLines(false)
        dataSet.setDrawFilled(true)
        val fillGradient =
            ContextCompat.getDrawable(requireContext(), R.drawable.line_chart_gradient)
        dataSet.fillDrawable = fillGradient
        // Create and set LineData
        val lineData = LineData(dataSet)
        lineChart.description.isEnabled = false
        lineChart.data = lineData
        // Invalidate the chart to refresh
        lineChart.invalidate()
    }
}
