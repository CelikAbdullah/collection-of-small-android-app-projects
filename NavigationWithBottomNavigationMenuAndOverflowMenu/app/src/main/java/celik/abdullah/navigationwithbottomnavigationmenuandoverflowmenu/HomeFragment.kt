package celik.abdullah.navigationwithbottomnavigationmenuandoverflowmenu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import celik.abdullah.navigationwithbottomnavigationmenuandoverflowmenu.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    // Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
    private var fragmentHomeBinding: FragmentHomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        fragmentHomeBinding = binding

        binding.textview.setOnClickListener{
            findNavController().navigate(R.id.settingsFragment)
        }

        // inflate the layout for this fragment
        return binding.root
    }

    override fun onDestroyView() {
        fragmentHomeBinding = null
        super.onDestroyView()
    }
}