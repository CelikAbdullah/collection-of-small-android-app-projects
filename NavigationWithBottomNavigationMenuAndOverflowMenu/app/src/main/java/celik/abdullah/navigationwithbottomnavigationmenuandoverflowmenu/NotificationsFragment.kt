package celik.abdullah.navigationwithbottomnavigationmenuandoverflowmenu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import celik.abdullah.navigationwithbottomnavigationmenuandoverflowmenu.databinding.FragmentNotificationsBinding


class NotificationsFragment : Fragment() {

    // Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
    private var fragmentNotificationsBinding : FragmentNotificationsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNotificationsBinding.inflate(layoutInflater, container, false)
        fragmentNotificationsBinding = binding

        binding.textview.setOnClickListener{
            findNavController().navigate(R.id.settingsFragment)
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onDestroyView() {
        fragmentNotificationsBinding = null
        super.onDestroyView()
    }
}