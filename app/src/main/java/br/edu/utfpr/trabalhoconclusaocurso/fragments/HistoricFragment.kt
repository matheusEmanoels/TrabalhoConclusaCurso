package br.edu.utfpr.trabalhoconclusaocurso.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.trabalhoconclusaocurso.R
import br.edu.utfpr.trabalhoconclusaocurso.adapter.HistoricAdapter
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Atividade
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Usuario
import br.edu.utfpr.trabalhoconclusaocurso.data.repository.AtividadeRepository
import br.edu.utfpr.trabalhoconclusaocurso.services.DBHelper
import br.edu.utfpr.trabalhoconclusaocurso.utils.SessaoUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoricFragment : Fragment(), HistoricAdapter.OnItemClickListener {

    private lateinit var recyclerHistorico: RecyclerView
    private lateinit var adapter: HistoricAdapter
    private lateinit var dbHelper: DBHelper
    private lateinit var atividadeRepository: AtividadeRepository
    private var usuario: Usuario? = null
    private val atividades = mutableListOf<Atividade>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_historic, container, false)

        recyclerHistorico = view.findViewById(R.id.recyclerHistoric)
        recyclerHistorico.layoutManager = LinearLayoutManager(requireContext())

        dbHelper = DBHelper(requireContext())
        atividadeRepository = AtividadeRepository(dbHelper.writableDatabase)
        usuario = SessaoUsuario.getUsuario()

        adapter = HistoricAdapter(atividades, this)
        recyclerHistorico.adapter = adapter

        carregarAtividades()

        return view
    }

    private fun carregarAtividades() {
        lifecycleScope.launch(Dispatchers.IO) {
            val lista = atividadeRepository.listarPorUsuarioLocal(usuario?.id!!)
            withContext(Dispatchers.Main) {
                atividades.clear()
                atividades.addAll(lista)
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onRenomear(atividade: Atividade, position: Int) {
        val editText = EditText(requireContext())
        editText.setText(atividade.nome)

        AlertDialog.Builder(requireContext())
            .setTitle("Renomear Atividade")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val novoNome = editText.text.toString()
                atividade.nome = novoNome
                lifecycleScope.launch(Dispatchers.IO) {
                    atividadeRepository.atualizar(atividade)
                    withContext(Dispatchers.Main) {
                        adapter.atualizarItem(position, atividade)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onExcluir(atividade: Atividade, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir Atividade")
            .setMessage("Deseja realmente excluir esta atividade?")
            .setPositiveButton("Sim") { _, _ ->
                atividadeRepository.excluir(atividade) { sucesso ->
                    if (sucesso) {
                        adapter.removerItem(position)
                    } else {
                        Toast.makeText(requireContext(), "Falha ao excluir atividade", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}