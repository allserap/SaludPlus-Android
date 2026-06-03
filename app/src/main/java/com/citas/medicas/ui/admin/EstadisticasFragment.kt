package com.citas.medicas.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.citas.medicas.R
import com.citas.medicas.databinding.FragmentEstadisticasBinding
import com.citas.medicas.models.UnidadMedicaResponse
import com.citas.medicas.models.HistoricoCitasResponse
import com.citas.medicas.ui.auth.AuthViewModel
import com.citas.medicas.ui.base.BaseFragment
import com.citas.medicas.utils.configurarConHint

class EstadisticasFragment : BaseFragment(R.layout.fragment_estadisticas) {

    // region Inicializacion
    private var _binding: FragmentEstadisticasBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    private var listaUnidades: List<UnidadMedicaResponse> = emptyList()
    private var isInitialSelection = true
    // endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEstadisticasBinding.bind(view)

        setupObservers()
        setupListeners()

        // 1. Cargamos primero los catálogos (Unidades médicas) para llenar el Spinner.
        authViewModel.cargarCatalogos()

        // 2. Estado inicial limpio (No llamamos a cargarReporteHistorico(null) aquí)
        resetearInterfaz()
    }

    private fun setupListeners() {
        binding.spUnidades.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isInitialSelection) {
                    isInitialSelection = false
                    return
                }
                // position 0 = "Seleccione unidad médica" (el hint)
                if (position == 0) {
                    // Al regresar al hint, limpiamos la pantalla por completo
                    resetearInterfaz()
                } else if ((position - 1) < listaUnidades.size) {
                    val unidadSeleccionada = listaUnidades[position - 1]
                    if (unidadSeleccionada.id == -1) {
                        authViewModel.cargarReporteHistorico(null) // "Todas las unidades"
                    } else {
                        authViewModel.cargarReporteHistorico(unidadSeleccionada.id) // Unidad específica
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupObservers() {
        // Observer para el histórico de citas que viene del servidor
        authViewModel.historicoCitas.observe(viewLifecycleOwner) { listaCitas ->

            Log.d("API_GRAPH_CHECK", "¿La lista es nula? ${listaCitas == null}. Tamaño: ${listaCitas?.size ?: 0}")
            if (listaCitas != null) {
                // Pintamos directamente la respuesta del backend sin filtros locales innecesarios
                mostrarEstadisticasEnGraficos(listaCitas)
            }
        }

        // Observer para el Spinner de unidades médicas
        authViewModel.unidadesMedicas.observe(viewLifecycleOwner) { lista ->
            if (lista != null) {
                val opcionTodas = UnidadMedicaResponse(id = -1, unidadMedica = "Todas las unidades")
                listaUnidades = listOf(opcionTodas) + lista

                val nombres = listaUnidades.map { it.unidadMedica }.toTypedArray()
                binding.spUnidades.configurarConHint(nombres, "Seleccione unidad médica")
            }
        }
    }

    private fun mostrarEstadisticasEnGraficos(citas: List<HistoricoCitasResponse>) {
        Log.d("DEBUG_GRAFICOS", "Pintando gráficos con ${citas.size} registros devueltos por el servidor.")

        // Sumatorias limpias basadas en las banderas calculadas por tu base de datos
        val asistidas = citas.sumOf { it.asistida }
        val canceladas = citas.sumOf { it.cancelada }
        val reprogramadas = citas.sumOf { it.reprogramada }
        val noAsistidas = citas.sumOf { it.noAsistida }

        // Calcular el total general de citas consolidadas
        val totalCitas = asistidas + canceladas + reprogramadas + noAsistidas

        // Calcular la tasa de completitud (evitando divisiones por cero)
        val tasaCompletitud = if (totalCitas > 0) {
            (asistidas.toFloat() / totalCitas.toFloat()) * 100
        } else {
            0f
        }

        Log.d("DEBUG_GRAFICOS", "Resultados -> Asistidas: $asistidas, Canceladas: $canceladas, Reprogramadas: $reprogramadas, NoAsistidas: $noAsistidas")

        // 2. ASIGNACIÓN A LA INTERFAZ (Lo que faltaba)
        // Tarjetas Superiores
        binding.tvCompletadas.text = asistidas.toString()
        binding.tvCanceladas.text = canceladas.toString()
        binding.tvReprogramadas.text = reprogramadas.toString()

        // Resumen Inferior (Tarjeta Azul)
        binding.tvTotalCitas.text = "Total de Citas: $totalCitas"
        binding.tvPorcentajeCompletadas.text = String.format("Tasa de completitud: %.1f%%", tasaCompletitud)

        // Pasamos los números listos a las funciones de renderizado de MPAndroidChart
        renderBarChart(asistidas, canceladas)
        renderPieChart(asistidas, canceladas, reprogramadas)
    }

    // region RenderizadoGraficos
    private fun renderBarChart(asistidas: Int, canceladas: Int) {
        val entries = arrayListOf(
            BarEntry(0f, asistidas.toFloat()),
            BarEntry(1f, canceladas.toFloat())
        )

        val dataSet = BarDataSet(entries, "").apply {
            colors = listOf(Color.parseColor("#2ECC71"), Color.parseColor("#E74C3C"))
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }

        binding.barChart.apply {
            data = BarData(dataSet).apply { barWidth = 0.4f }
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.setDrawGridLines(false)

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(listOf("Asistidas", "Canceladas"))
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                isGranularityEnabled = true
                setDrawGridLines(false)
            }
            animateY(800)
            invalidate()
        }
    }

    private fun renderPieChart(asistidas: Int, canceladas: Int, reprogramadas: Int) {
        val entries = arrayListOf(
            PieEntry(asistidas.toFloat(), "Asistidas"),
            PieEntry(canceladas.toFloat(), "Canceladas"),
            PieEntry(reprogramadas.toFloat(), "Reprogramadas"),
        )

        // Filtramos las entradas con valor 0 para evitar distorsiones visuales en el PieChart
        val entriesFiltradas = entries.filter { it.value > 0 }

        val dataSet = PieDataSet(entriesFiltradas, "").apply {
            colors = listOf(
                Color.parseColor("#2ECC71"), // Verde
                Color.parseColor("#E74C3C"), // Rojo
                Color.parseColor("#F39C12")  // Naranja
            )
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 45f
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(10f)
            animateXY(800, 800)
            invalidate()
        }
    }
    // endregion

    override fun resetearInterfaz() {
        // Al resetear la interfaz limpiamos los gráficos pasando una lista vacía
        mostrarEstadisticasEnGraficos(emptyList())

        binding.tvCompletadas.text = "0"
        binding.tvCanceladas.text = "0"
        binding.tvReprogramadas.text = "0"
        binding.tvTotalCitas.text = "Total de Citas: --"
        binding.tvPorcentajeCompletadas.text = "Tasa de completitud: --%"
    }

    override fun onDestroyView() {
        // Prevención estricta de fugas de memoria (Memory Leaks)
        _binding?.let {
            it.barChart.clear()
            it.pieChart.clear()
            it.spUnidades.onItemSelectedListener = null
        }
        super.onDestroyView()
        _binding = null
    }
}