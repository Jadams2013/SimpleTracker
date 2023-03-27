package com.example.simpletracker.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simpletracker.*
import com.example.simpletracker.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch
import java.util.*



class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val tagDatabase by lazy { TagDatabase.getDatabase(requireContext()).tagDao() }
    private lateinit var adapter: TagsRVAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle? ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /* TODO LAST make sure this is unnecessary and then remove
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        */

        root?.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.floatingActionButton)
            ?.setOnClickListener {
                newTag()
            }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        observeTags()
    }


    private fun newTag(view:View? = null) {
        val intent = Intent(activity, EditTagActivity::class.java)
        newTagResultLauncher.launch(intent)
    }

    private fun setRecyclerView() {
        val tagsRecyclerview = view?.findViewById<RecyclerView>(R.id.tags_view)
        tagsRecyclerview?.layoutManager = LinearLayoutManager(activity)
        tagsRecyclerview?.setHasFixedSize(true)
        adapter = TagsRVAdapter()
        adapter.setItemListener(object : RecyclerClickListener {

            /*/ Tap the 'X' to delete the tag. TODO LAST make sure this is unnecessary and then remove
            override fun onItemRemoveClick(position: Int) {
                val tagsList = adapter.currentList.toMutableList()
                val tagId = tagsList[position].tagId
                val tagName = tagsList[position].tagName
                val tagNote = tagsList[position].tagNote
                val tagDateAdded = tagsList[position].dateAdded
                val removeTag = Tag(tagId, tagName, tagNote, tagDateAdded)
                tagsList.removeAt(position)
                adapter.submitList(tagsList)
                lifecycleScope.launch {
                    tagDatabase.deleteTag(removeTag)
                    tagDatabase.deletePointsByTag(removeTag.tagId)
                }
            }// */

            // Tap the tag to edit.
            override fun onItemClick(position: Int) {
                val intent = Intent(activity, EditTagActivity::class.java)
                val tagsList = adapter.currentList.toMutableList()
                intent.putExtra("tag_id", tagsList[position].tagId)

                /*/TODO LAST remove this?
                intent.putExtra("tag_name", tagsList[position].tagName)
                intent.putExtra("tag_note", tagsList[position].tagNote)
                intent.putExtra("tag_date_added", tagsList[position].dateAdded.time)// */

                editTagResultLauncher.launch(intent)
            }

            override fun onSavePointClick(position: Int, severity: Int) {
                Log.d("savePointNow", "position $position")
                val tagsList = adapter.currentList.toMutableList()
                val newPoint = Tag.Point(0, tagsList[position].tagId, Date(), severity)
                lifecycleScope.launch {
                    tagDatabase.insert(newPoint)
                }
            }

        })
        tagsRecyclerview?.adapter = adapter
    }

    private fun observeTags(view: View? = null) {
        lifecycleScope.launch {
            tagDatabase.getAlphabetizedTags().collect { tagsList ->
                Log.d("**observeTags**","${tagsList.size} tags")
                adapter.submitList(tagsList)
            }
        }
    }



    private val newTagResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {/* TODO LAST make sure this is unnecessary and then remove
                // Get the new tag from the AddTagActivity
                val tagName = result.data?.getStringExtra("tag_name")
                val tagNote = result.data?.getStringExtra("tag_note")
                val dateAdded = Date()
                //val points: Set<Point> = emptySet()
                // Add the new tag at the top of the list
                val newTag = Tag(0, tagName.toString(), tagNote.toString(), dateAdded)
                lifecycleScope.launch {
                    tagDatabase.insert(newTag)
                }*/
            }
        }

    val editTagResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                /* TODO LAST make sure this is unnecessary and then remove
                // Get the edited tag from the AddTagActivity
                val tagId = result.data?.getIntExtra("tag_id", 0) ?: 0
                var tagName: String = result.data?.getStringExtra("tag_name") ?: ""
                val tagNote = result.data?.getStringExtra("tag_note")
                val tagDateAdded = Date(result.data?.getLongExtra("tag_date_added", 0) ?: 0)
                // Update the tag in the list
                val editedTag = Tag(tagId, tagName, tagNote, tagDateAdded)

                if (result.data?.getBooleanExtra("delete",false) == true){
                    lifecycleScope.launch {
                        tagDatabase.deleteTag(editedTag)
                        tagDatabase.deletePointsByTag(editedTag.tagId)
                    }
                } else {
                    lifecycleScope.launch {
                        tagDatabase.updateTag(editedTag)
                    }
                }*/
                //observeTags() //unnecessary?
            }
        }
}