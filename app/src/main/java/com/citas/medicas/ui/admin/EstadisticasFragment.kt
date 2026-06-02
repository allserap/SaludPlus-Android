package com.citas.medicas.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.citas.medicas.ui.auth.AuthViewModel
import com.citas.medicas.ui.base.BaseFragment

// Modelos mapeados localmente según la respuesta de tu API
data class CitaFormateada(
    val id: Int,
    val estado: String,
    val unidad_medica_id: Int,
    val unidad_medica: String
)

data class UnidadMedicaUI(val id: Int, val nombre: String) {
    override fun toString(): String = nombre
}

class EstadisticasCitasFragment : BaseFragment(R.layout.fragment_estadisticas) {

    // region Inicializacion
    private var _binding: FragmentEstadisticasBinding? = null
    private val binding get() = _binding!!

    // verificar
    private val authViewModel: AuthViewModel by viewModels()

    private var todasLasCitas: List<CitaFormateada> = emptyList()
    private var listaUnidades: List<UnidadMedicaUI> = emptyList()
    // endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEstadisticasBinding.bind(view)

        setupObservers()
        setupListeners()

        // Llamada inicial para disparar el flujo del repositorio/API
        // authViewModel.cargarReporteHistorico()

        simularRespuestaApi()
    }

    private fun setupListeners() {
        binding.spUnidades.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (listaUnidades.isNotEmpty()) {
                    val unidadSeleccionada = listaUnidades[position]
                    // Ejecuta el filtro dinámico e inmediato en el hilo principal
                    calcularYMostrarEstadisticas(unidadSeleccionada.id)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupObservers() {
        // modificar lógica
        /*
        authViewModel.historicoCitasResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                // Tu endpoint separa en 'proximas' y 'pasadas'; aquí las unificamos en Frontend
                todasLasCitas = response.proximas + response.pasadas
                mapearUnidadesMedicas(todasLasCitas)
            }
        }
        */
    }

    private fun mapearUnidadesMedicas(citas: List<CitaFormateada>) {
        // Extrae combinaciones únicas de id y nombre directamente de la respuesta general
        val unidadesUnicas = citas.distinctBy { it.unidad_medica_id }
            .map { UnidadMedicaUI(it.unidad_medica_id, it.unidad_medica) }
            .sortedBy { it.nombre }

        // Agrega la opción comodín para ver consolidados globales
        listaUnidades = listOf(UnidadMedicaUI(-1, "Todas las Unidades")) + unidadesUnicas

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listaUnidades)
        binding.spUnidades.adapter = adapter
    }

    private fun calcularYMostrarEstadisticas(unidadId: Int) {
        // Filtrado reactivo en memoria
        val citasFiltradas = if (unidadId == -1) {
            todasLasCitas
        } else {
            todasLasCitas.filter { it.unidad_medica_id == unidadId }
        }

        // Conteo usando predicados rápidos de colecciones en Kotlin
        val total = citasFiltradas.size
        val completadas = citasFiltradas.count { it.estado.equals("completada", ignoreCase = true) }
        val canceladas = citasFiltradas.count { it.estado.equals("cancelada", ignoreCase = true) }
        val reprogramadas = citasFiltradas.count { it.estado.equals("reprogramada", ignoreCase = true) }

        val tasaCompletitud = if (total > 0) (completadas.toFloat() / total.toFloat()) * 100 else 0f

        // Asignación a los componentes visuales
        with(binding) {
            tvCompletadas.text = completadas.toString()
            tvCanceladas.text = canceladas.toString()
            tvReprogramadas.text = reprogramadas.toString()

            tvTotalCitas.text = "Total de Citas: $total"
            tvPorcentajeCompletadas.text = String.format("Tasa de completitud: %.1f%%", tasaCompletitud)
        }

        // Actualización e invalidación de los componentes de gráficos
        renderBarChart(completadas, canceladas)
        renderPieChart(completadas, canceladas, reprogramadas)
    }

    // region RenderizadoGraficos
    private fun renderBarChart(completadas: Int, canceladas: Int) {
        val entries = arrayListOf(
            BarEntry(0f, completadas.toFloat()),
            BarEntry(1f, canceladas.toFloat())
        )

        val dataSet = BarDataSet(entries, "").apply {
            colors = listOf(Color.parseColor("#2ECC71"), Color.parseColor("#E74C3C"))
            valueTextSize = 12f
        }

        binding.barChart.apply {
            data = BarData(dataSet).apply { barWidth = 0.4f }
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.setDrawGridLines(false)

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(listOf("Completadas", "Canceladas"))
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                isGranularityEnabled = true
                setDrawGridLines(false)
            }
            animateY(800)
            invalidate()
        }
    }

    private fun renderPieChart(completadas: Int, canceladas: Int, reprogramadas: Int) {
        val entries = arrayListOf(
            PieEntry(completadas.toFloat(), "Completadas"),
            PieEntry(canceladas.toFloat(), "Canceladas"),
            PieEntry(reprogramadas.toFloat(), "Reprogramadas")
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#2ECC71"),
                Color.parseColor("#E74C3C"),
                Color.parseColor("#F39C12")
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

    private fun simularRespuestaApi() {
        val pasadasMock = listOf(
            CitaFormateada(1, "completada", 101, "Hospital Central"),
            CitaFormateada(2, "cancelada", 101, "Hospital Central"),
            CitaFormateada(3, "completada", 102, "Clínica San Benito"),
            CitaFormateada(4, "completada", 102, "Clínica San Benito")
        )
        val proximasMock = listOf(
            CitaFormateada(5, "reprogramada", 101, "Hospital Central"),
            CitaFormateada(6, "pendiente", 102, "Clínica San Benito")
        )

        todasLasCitas = pasadasMock + proximasMock
        mapearUnidadesMedicas(todasLasCitas)
    }

    override fun resetearInterfaz() {
        // añadir lógica
    }

    override fun onDestroyView() {
        // Limpieza estricta de gráficos y listeners para mitigar warnings del Recolector de Basura
        _binding?.let {
            it.barChart.clear()
            it.pieChart.clear()
            it.spUnidades.onItemSelectedListener = null
        }
        super.onDestroyView()
        _binding = null
    }
}