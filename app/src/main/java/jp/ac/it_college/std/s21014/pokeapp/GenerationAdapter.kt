package jp.ac.it_college.std.s21014.pokeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import jp.ac.it_college.std.s21014.pokeapp.model.Pokedex

class GenerationAdapter(
    private val generations: List<Pokedex>,
    private val args: SelectGenerationFragmentArgs
) : RecyclerView.Adapter<GenerationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGeneration: TextView
        val root: ConstraintLayout

        init {
            tvGeneration = view.findViewById(R.id.tvGeneration)
            root = view.findViewById(R.id.rootGenerationRow)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.generation_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val g = generations[position]
        holder.tvGeneration.text = g.name
        holder.root.setOnClickListener {
            Navigation.findNavController(it).navigate(
                SelectGenerationFragmentDirections.selectToQuiz(
                    g.entries.map { e -> e.pokemon_id }.toIntArray(),
                    arrayOfNulls(10),
                    args.isHard
                )
            )
        }
    }

    override fun getItemCount(): Int = generations.size
}