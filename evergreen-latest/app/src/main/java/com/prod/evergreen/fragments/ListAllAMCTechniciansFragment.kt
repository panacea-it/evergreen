package com.prod.evergreen.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.prod.evergreen.activities.AddUser
import com.prod.evergreen.api.SharedViewModel
import com.prod.evergreen.databinding.FragmentListAllAMCTechniciansBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.SharedPreferencesHelper


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ListAllAMCTechniciansFragment : Fragment() {
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var param1: String? = null
    private var param2: String? = null
    val items = listOf("Clients", "Pocs","Technicians","Managers")
    val types = listOf("client_admin", "client","technician","eg_admin")
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    lateinit var binding: FragmentListAllAMCTechniciansBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentListAllAMCTechniciansBinding.inflate(layoutInflater, container, false)
        sharedPreferencesHelper= SharedPreferencesHelper(requireActivity())
        val accessLevel = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        if (accessLevel.equals("client")||accessLevel.equals("technician")){
            binding.adduser.visibility=View.GONE
        }
        else{
            binding.adduser.visibility=View.VISIBLE
        }



   return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttons.llPocs.setOnClickListener {
            binding.viewPager.currentItem = 0
        }

        binding.buttons.llAdmin.setOnClickListener {
            binding.viewPager.currentItem = 3
        }
        binding.buttons.llManagers.setOnClickListener {
            binding.viewPager.currentItem = 1


        }
        binding.buttons.llTechncians.setOnClickListener {
            binding.viewPager.currentItem = 2
        }



        sharedViewModel.sharedDatacounter.observe(viewLifecycleOwner) { data ->
            binding.buttons.pocs.text = data.client_admin.toString()
            binding.buttons.managers.text = data.client.toString()
            binding.buttons.technicins.text = data.technician.toString()
            binding.buttons.admin.text = data.eg_admin.toString()
        }


binding.adduser.setOnClickListener {
    startActivity(Intent(requireActivity(), AddUser::class.java))
}

        binding.viewPager.adapter = ViewPagerAdapter(requireActivity(),items,types)



        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = items[position]

        }.attach()



    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListAllAMCTechniciansFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private  class ViewPagerAdapter(fa: FragmentActivity, val list: List<String>, val types: List<String>) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = list.size

        override fun createFragment(position: Int): Fragment {
            return AmcListFragment.newInstance(list[position], types[position])

        }
    }

}