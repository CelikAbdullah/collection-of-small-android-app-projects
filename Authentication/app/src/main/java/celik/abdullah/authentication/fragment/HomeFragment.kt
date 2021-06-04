package celik.abdullah.authentication.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import celik.abdullah.authentication.R
import dagger.hilt.android.AndroidEntryPoint

/*
* The UI controller representing the starting page of the app.
* I kept it very simple since this app should focus only on authentication.
* */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

}