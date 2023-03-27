package com.example.simpletracker.ui.slideshow

import android.app.AlarmManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.simpletracker.R
import com.example.simpletracker.Tag
import com.example.simpletracker.TagDatabase
import com.example.simpletracker.databinding.FragmentSlideshowBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class SlideshowFragment : Fragment() {

    private val tagDatabase by lazy { TagDatabase.getDatabase(requireContext()).tagDao() }
    private var _binding: FragmentSlideshowBinding? = null
    private var tagList: List<Tag>? = null
    private var tag1: Int = 0
    private var tag2: Int = 0
    private var correlation: Double = 0.0


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val spinner1 = view.findViewById<Spinner>(com.example.simpletracker.R.id.tag1Spinner)
        val spinner2 = view.findViewById<Spinner>(com.example.simpletracker.R.id.tag2Spinner)

        val spinnerAdapter: ArrayAdapter<String> = ArrayAdapter<String>(view.context, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner1.adapter = spinnerAdapter
        spinner2.adapter = spinnerAdapter

        lifecycleScope.launch {
            tagDatabase.getAlphabetizedTags().collect {
                tagList = it
                for (tag in it) {
                    spinnerAdapter.add(tag.tagName)
                }
                spinnerAdapter.notifyDataSetChanged()
            }
        }

        spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, viewLocal: View?, position: Int, id: Long) {
                tag1 = tagList?.get(position)?.tagId ?: 0
                Log.d("Tag1 selected","$tag1")
                checkForCalculate()
                lifecycleScope.launch {
                    tagDatabase.numPoints(tag1).collect {
                        view?.findViewById<TextView>(R.id.tag1Text1)?.text = "Number of Data Points: $it\n "
                        Log.d("Number of Data Points", it.toString())
                    }
                }
                lifecycleScope.launch {
                    tagDatabase.meanSeverity(tag1).collect {
                        var temp:Double
                        if (it == null)
                            temp = 0.0
                        else
                            temp = it * 100
                        temp = temp.roundToInt().toDouble()/100
                        view?.findViewById<TextView>(R.id.tag1Text2)?.text = "Average Severity: $temp\n "
                        Log.d("Average Severity", it.toString())
                    }
                }

                //TODO over the last x weeks?
            }
        }



        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, viewLocal: View?, position: Int, id: Long) {
                tag2 = tagList?.get(position)?.tagId ?: 0
                Log.d("Tag2 selected","$tag1")
                checkForCalculate()
                lifecycleScope.launch {
                    tagDatabase.numPoints(tag2).collect {
                        view?.findViewById<TextView>(R.id.tag2Text1)?.text = "Number of Data Points: $it\n "
                        Log.d("Number of Points", it.toString())
                    }
                }
                lifecycleScope.launch {
                    tagDatabase.meanSeverity(tag2).collect {
                        var temp:Double
                        if (it == null)
                            temp = 0.0
                        else
                            temp = it * 100
                        temp = temp.roundToInt().toDouble()/100
                        view?.findViewById<TextView>(R.id.tag2Text2)?.text = "Average Severity: $temp\n "
                        Log.d("Average Severity", it.toString())
                    }
                }


                //TODO over the last x weeks?
            }
        }



    }

    private fun checkForCalculate() {
        if (tag1 == 0 || tag2 == 0) return
        if (tag1 == tag2) {
            view?.findViewById<TextView>(R.id.resultsNum)?.text = ""
            view?.findViewById<TextView>(R.id.resultsText)?.text = "Please select two different tags"
            //TODO make this in strings.xml"
        } else {
            view?.findViewById<TextView>(R.id.resultsNum)?.text = "0.XX "
            view?.findViewById<TextView>(R.id.resultsText)?.text = "Calculating..."
            /*lifecycleScope.launch {
                tagDatabase.calculateCorrelation(tag1, tag2).collect {
                    var temp:Double = it * 100
                    temp = temp.roundToInt().toDouble()/100
                    view?.findViewById<TextView>(R.id.resultsNum)?.text = temp.toString()
                    Log.d("Correlation Coefficient", it.toString())
                    view?.findViewById<TextView>(R.id.resultsText)?.text = correlationStrengthString(it)
                }
            }// */
        }


        /*lifecycleScope.launch {
            tagDatabase.calculateCorrelation(tag1, tag2).collect {
                correlation = it

            }
        } // */

    }


    private fun correlationStrengthString(double: Double):String {
        //TODO make this update text talking about high/low
        if (double > 0.5){
            return "The two data tags are positively correlated\n" +
                    "If one goes up, the other will likely go up as well"
        }
        else
            return "There was an error calculating the correlation"
    }


}