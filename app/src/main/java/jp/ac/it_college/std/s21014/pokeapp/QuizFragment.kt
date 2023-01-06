package jp.ac.it_college.std.s21014.pokeapp

import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.OnDismissListener
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import jp.ac.it_college.std.s21014.pokeapp.databinding.FragmentQuizBinding
import kotlinx.coroutines.launch
import java.util.*

class QuizFragment : Fragment() {
    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private val args: QuizFragmentArgs by navArgs()

    private val IMG_URL_FORMAT =
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/%d.png"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvQuestionCount.text = getString(R.string.question_count, args.questionNumber)
        val pokemonIdList = args.pokemonIdArray.toMutableList()
        val buttons = listOf(
            binding.answer1,
            binding.answer2,
            binding.answer3,
            binding.answer4,
        ).shuffled()

        var pressed = false
        class ClickListener(val selected: String = "") : View.OnClickListener {
            override fun onClick(v: View?) {
                if (binding.imgPokemon.drawable == null || pressed) {
                    return
                }
                pressed = true
//                Toast.makeText(v.context, if (correct) "正解ですーーー" else "不正解", Toast.LENGTH_SHORT)
//                    .show()
                val selectedPokemonName = selected.ifEmpty { "時間切れ" }
                val correctPokemonName = buttons[0].text.toString()
                val correctPokemonImage =
                    binding.imgPokemon.drawable.toBitmap(100, 100, Bitmap.Config.ARGB_8888)
                args.resultDataArray[args.questionNumber - 1] = ResultData(
                    selectedPokemonName,
                    correctPokemonName,
                    correctPokemonImage
                )
                val greats = listOf(
                    getString(R.string.great_1),
                    getString(R.string.great_2),
                    getString(R.string.great_3),
                    getString(R.string.great_4),
                    getString(R.string.great_5)
                )
                val resO = ResourcesCompat.getDrawable(resources, R.drawable.mark_maru, null)
                val resX = ResourcesCompat.getDrawable(resources, R.drawable.mark_batsu, null)
                AnswerDialog(
                    if (selectedPokemonName == correctPokemonName)
                        getString(R.string.correct) else getString(R.string.incorrect),
                    if (selectedPokemonName == correctPokemonName)
                        greats.random() else getString(R.string.answer_is, correctPokemonName),
                    if (selectedPokemonName == correctPokemonName)
                        resO!! else resX!!
                ) {
                    Navigation.findNavController(view).navigate(
                        if (args.questionNumber >= 10) {
                            QuizFragmentDirections.quizToResult(
                                args.resultDataArray,
                                args.isHard
                            )
                        } else {
                            QuizFragmentDirections.quizToQuiz(
                                pokemonIdList.toIntArray(),
                                args.resultDataArray,
                                args.isHard
                            ).apply {
                                correctCount =
                                    args.correctCount + if (selectedPokemonName == correctPokemonName) 1 else 0
                                questionNumber = args.questionNumber + 1
                            }
                        }
                    )
                }.show(parentFragmentManager, "dialog_answer")
            }
        }

        val selectedIdList = mutableListOf<Int>()
        var i = 0
        while (i < 4) {
            val selectedId = pokemonIdList[Random().nextInt(pokemonIdList.size)]
            if (!selectedIdList.contains(selectedId)) {
                selectedIdList.add(selectedId)
                buttons[i].apply {
                    try {
                        text = pokemon.filter { p -> p.id == selectedId }[0].name
                    } catch (e: IndexOutOfBoundsException) {
                        println("Pokemon not found: $selectedId")
                        e.printStackTrace()
                    }
                    setOnClickListener(ClickListener(this.text.toString()))
                }
                if (i == 0) {
                    showPokemonImage(selectedId)
                }
                pokemonIdList.remove(selectedId)
                i++
            }
        }
        binding.btAnswer.setOnClickListener {
            val answer = binding.etAnswer.text.toString()
            if (answer.isEmpty()) {
                return@setOnClickListener
            }
            ClickListener(answer).onClick(null)
        }
        if (args.isHard) {
            binding.answer1.visibility = View.INVISIBLE
            binding.answer2.visibility = View.INVISIBLE
            binding.answer3.visibility = View.INVISIBLE
            binding.answer4.visibility = View.INVISIBLE
        } else {
            binding.etAnswer.visibility = View.INVISIBLE
            binding.btAnswer.visibility = View.INVISIBLE
        }
        val h = Handler(Looper.getMainLooper())
        h.postDelayed(object : Runnable {
            var time = if (args.isHard) 45 else 10
            override fun run() {
                try { // 別のFragmentに移動している場合
                    view.findNavController()
                } catch (e: IllegalStateException) {
                    return
                }
                if (time <= 0) {
                    ClickListener().onClick(null)
                    return
                }
                binding.tvTimer.text = getString(R.string.timer, time)
                time -= 1
                h.postDelayed(this, 1000L)
            }
        }, 0L)
    }

    @UiThread
    private fun showPokemonImage(id: Int) {
        lifecycleScope.launch {
            val url = IMG_URL_FORMAT.format(id)
            Picasso.get().load(url).into(binding.imgPokemon)
            binding.imgPokemon.setColorFilter(Color.rgb(0, 0, 0))
        }
    }

    class AnswerDialog(
        private val title: String,
        private val content: String,
        private val icon: Drawable,
        private val listener: OnDismissListener
    ) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return requireActivity().let {
                AlertDialog.Builder(it).apply {
                    setTitle(title)
                    setMessage(content)
                    setIcon(icon)
                    setPositiveButton(getString(R.string.to_next)) { _, _ -> }
                }
            }.create()
        }

        override fun onDismiss(dialog: DialogInterface) {
            listener.onDismiss(dialog)
        }
    }
}