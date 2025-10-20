package br.edu.utfpr.trabalhoconclusaocurso.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.trabalhoconclusaocurso.R
import br.edu.utfpr.trabalhoconclusaocurso.activities.MapActivity
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Atividade
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoricAdapter(
    private val atividades: MutableList<Atividade>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<HistoricAdapter.HistoricoViewHolder>() {

    interface OnItemClickListener {
        fun onRenomear(atividade: Atividade, position: Int)
        fun onExcluir(atividade: Atividade, position: Int)
    }

    class HistoricoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNome: TextView = itemView.findViewById(R.id.tvNome)
        val tvData: TextView = itemView.findViewById(R.id.tvData)
        val tvDistancia: TextView = itemView.findViewById(R.id.tvDistancia)
        val tvDuracao: TextView = itemView.findViewById(R.id.tvDuracao)
        val btnRenomear: ImageButton = itemView.findViewById(R.id.btnRenomear)
        val btnExcluir: ImageButton = itemView.findViewById(R.id.btnExcluir)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_atividade, parent, false)
        return HistoricoViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoricoViewHolder, position: Int) {
        val atividade = atividades[position]

        holder.tvNome.text = atividade.nome
        val timestamp = try {
            atividade.dataHora.toLong()
        } catch (e: Exception) {
            0L
        }

        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.tvData.text = sdf.format(date)

        holder.tvDistancia.text = "Distância: ${String.format("%.2f", atividade.distancia / 1000f)} km"

        val horas = atividade.duracao / 3600
        val minutos = (atividade.duracao % 3600) / 60
        holder.tvDuracao.text = "Duração: ${horas}h ${minutos}m"
        holder.btnRenomear.setOnClickListener {
            listener.onRenomear(atividade, position)
        }
        holder.btnExcluir.setOnClickListener {
            listener.onExcluir(atividade, position)
        }

        holder.itemView.setOnClickListener { view ->
            val context = view.context
            val intent = Intent(context, MapActivity::class.java)
            intent.putExtra("ATIVIDADE_ID", atividade.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = atividades.size

    fun removerItem(position: Int) {
        atividades.removeAt(position)
        notifyItemRemoved(position)
    }

    fun atualizarItem(position: Int, atividade: Atividade) {
        atividades[position] = atividade
        notifyItemChanged(position)
    }
}
