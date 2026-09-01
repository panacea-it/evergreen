package com.prod.evergreen.fragments

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.activities.MainActivity
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.NetworkState
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.FragmentHomeBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.CustomMarkerView
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.DataItem1
import com.prod.evergreen.models.DataItem2

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel


    lateinit var binding: FragmentHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        sharedPreferencesHelper = SharedPreferencesHelper(requireActivity())

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = HomeFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewmodel()
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        val accesslevel = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)

        if (accesslevel=="eg_super_admin"||accesslevel=="eg_admin"){
            binding.usersCounts.visibility=View.VISIBLE
            binding.usersJoinMonths.visibility=View.VISIBLE
            viewModel.fetchStats(token!!)
        }
        else{
            binding.usersCounts.visibility=View.GONE
            binding.usersJoinMonths.visibility=View.GONE
           viewModel.getAllTaskCountAPI(token!!)
        }




        binding.llOpen.setOnClickListener {
            findNavController().navigate(R.id.taskFragment)
            (activity as MainActivity).setTitleTextView("Tasks List")

        }
        binding.llHold.setOnClickListener {
            findNavController().navigate(R.id.taskFragment)
            (activity as MainActivity).setTitleTextView("Tasks List")
        }
        binding.llInprogress.setOnClickListener {
            findNavController().navigate(R.id.taskFragment)
            (activity as MainActivity).setTitleTextView("Tasks List")

        }
        binding.llClose.setOnClickListener {
            findNavController().navigate(R.id.taskFragment)
            (activity as MainActivity).setTitleTextView("Tasks List")
        }


        //


        binding.llPocs.setOnClickListener {
            findNavController().navigate(R.id.amc_mangers)
            (activity as MainActivity).setTitleTextView("Users List")

        }
        binding.llClients.setOnClickListener {
            findNavController().navigate(R.id.amc_mangers)
            (activity as MainActivity).setTitleTextView("Users List")
        }
        binding.llMangers.setOnClickListener {
            findNavController().navigate(R.id.amc_mangers)
            (activity as MainActivity).setTitleTextView("Users List")

        }
        binding.llTechnicians.setOnClickListener {
            findNavController().navigate(R.id.amc_mangers)
            (activity as MainActivity).setTitleTextView("Users List")
        }

        viewModel.loading.observe(viewLifecycleOwner) { data ->
            if (data){
                ProgressDialogUtil.showProgressDialog(requireActivity(),"Loading")
            }
            else{
                ProgressDialogUtil.hideProgressDialog()
            }
        }

        viewModel.companiesStatsResponse.observe(viewLifecycleOwner) { response ->

            when (response) {
                is NetworkState.Success -> {
                    val companiesStatsData = response.data
                   if (companiesStatsData.status==200){
                       binding.tvCompaniesOnboard.text=companiesStatsData.message
                       val dataItems: List<DataItem1> = companiesStatsData.data!!


                       val entries = mutableListOf<Entry>()
                       val months = mutableListOf<String>()
                       for ((index, dataItem) in dataItems.withIndex()) {
                           // Add Entry with x-value as index and y-value as dataItem.value
                           entries.add(Entry(index.toFloat(), dataItem.value!!.toFloat()))
                           months.add(dataItem.month!!)
                       }
                       setupLineChart(entries, binding.lineChart,months.toTypedArray())

                   }
                }
                is NetworkState.Error -> {
                    val errorMessage = response.message ?: "Unknown error"
                    // Handle error for companies stats
                    print("charan"+errorMessage)

                }
            }


        }


        viewModel.userStatsResponse.observe(viewLifecycleOwner) { response ->

            when (response) {
                is NetworkState.Success -> {
                    val companiesStatsData = response.data
                    if (companiesStatsData.status==200){
                        binding.tvCompaniesOnboard.text=companiesStatsData.message
                        val dataItems: List<DataItem2> = companiesStatsData.data!!


                        for (entry in dataItems) {
                            when (entry.accessLevel) {
                                "technician" -> {
                                    binding.tvTechniciansHead.text = "Technician's"
                                    binding.tvTechnicians.text = entry.count.toString()
                                }
                                "client_admin" -> {
                                    binding.tvPocsHead.text = "Client Admins"
                                    binding.tvPocs.text = entry.count.toString()
                                }
                                "client" -> {
                                    binding.tvClietsHead.text = "Client's"
                                    binding.tvCliets.text = entry.count.toString()
                                }
                                "eg_admin" -> {
                                    binding.tvMangerHead.text = "Manager's"
                                    binding.tvManger.text = entry.count.toString()
                                }
                                // Add more cases as needed
                                else -> {
                                    // Handle unexpected access levels or add more cases if necessary
                                }
                            }
                        }

                    }
                }
                is NetworkState.Error -> {
                    val errorMessage = response.message ?: "Unknown error"
                    // Handle error for companies stats

                }
            }
        }



        viewModel.getAllTaskCountResponse.observe(viewLifecycleOwner) { response ->

            when (response) {
                is NetworkState.Success -> {
                    val companiesStatsData = response.data
                    if (companiesStatsData.success==200){

if (companiesStatsData.data!=null) {
    binding.tvOpen.text = companiesStatsData.data.open.toString()
    binding.tvHold.text = companiesStatsData.data.hold.toString()
    binding.tvInprogress.text = companiesStatsData.data.inProgress.toString()
    binding.tvCompleted.text = companiesStatsData.data.closed.toString()
}
                    }
                }
                is NetworkState.Error -> {
                    val errorMessage = response.message ?: "Unknown error"
                    // Handle error for companies stats

                }
            }
        }

    }



    private fun setupLineChart(entries: List<Entry>, lineChart: LineChart, toTypedArray: Array<String>){

        val customMarkerView = CustomMarkerView(requireActivity(), R.layout.custom_tooltip)

        // Create a dataset from the data entries
        val dataSet = LineDataSet(entries, "Companies Onboarded")
        dataSet.color = Color.parseColor("#4CB528")
        dataSet.valueTextColor = Color.BLACK
        dataSet.setDrawCircles(true)
        dataSet.setDrawCircleHole(false)
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#4CB528AD") // Light blue
        dataSet.fillAlpha = 90
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.lineWidth = 2f

        // Create a Linear Gradient
        val drawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor("#4CB528"), Color.TRANSPARENT)
        )

        // Assign the GradientDrawable to the fill drawable
        dataSet.fillDrawable = drawable

        // Create a LineData object from the dataset
        val lineData = LineData(dataSet)

        // Set the LineData to the LineChart
        lineChart.data = lineData
        lineChart.marker = customMarkerView

        // Customize the appearance of the chart
        lineChart.legend.isEnabled = false
        lineChart.setPinchZoom(false) // Disable pinch-to-zoom
        lineChart.setDoubleTapToZoomEnabled(false) // Disable double-tap zoom
        lineChart.setDrawGridBackground(false)
        lineChart.description.isEnabled = false
        lineChart.animateX(1000, Easing.EaseInExpo)
        lineChart.axisRight.isEnabled = false

        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(toTypedArray)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.BLACK
        xAxis.textSize = 10f
        xAxis.granularity = 1f // Set granularity to 1 to show all labels

        val yAxis = lineChart.axisLeft
        yAxis.textColor = Color.BLACK
        yAxis.setDrawGridLines(true)

        // Disable left, right, and top axis lines
        lineChart.axisLeft.setDrawAxisLine(true)
        lineChart.axisRight.setDrawAxisLine(true)
        lineChart.xAxis.setDrawAxisLine(true)

        // Disable left, right, and top axis grid lines
        lineChart.axisLeft.setDrawGridLines(true)
        lineChart.axisRight.setDrawGridLines(true)
        lineChart.xAxis.setDrawGridLines(true)

        lineChart.setScaleEnabled(true) // Disable scaling
        lineChart.invalidate() // Refresh chart

    }



    private fun setViewmodel() {
        val repository = MainRepository(
            RetrofitService.getInstance(requireActivity()),
            XApplication.database.newsDao(),
            XApplication.database.companyDao())
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
    }
    }

