package jp.ac.it_college.std.s21014.pokeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import jp.ac.it_college.std.s21014.pokeapp.databinding.FragmentSelectGenerationBinding

class SelectGenerationFragment : Fragment() {
    private var _binding: FragmentSelectGenerationBinding? = null
    private val binding get() = _binding!!
    private val args: SelectGenerationFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectGenerationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvGenerations.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = GenerationAdapter(pokedex, args)
        }
        binding.tvMode.text = getString(R.string.mode, if (args.isHard) getString(R.string.hard) else getString(R.string.easy))
    }
}