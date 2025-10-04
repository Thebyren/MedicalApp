package com.medical.app.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor() {

    private lateinit var model: GenerativeModel
    private var isInitialized = false
    /**
     * Inicializa el servicio de Gemini con la API key
     */
    fun initialize(apiKey: String) {
        model = GenerativeModel(
            modelName = "gemini-2.5-pro",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 1024
            }
        )
        isInitialized = true
    }
    suspend fun generateGeneralStatsReport(data: Map<String, Any>): String {
        if (!isInitialized) throw IllegalStateException("GeminiService no está inicializado")

        return try {
            val prompt = buildString {
                appendLine("Analiza estas estadísticas médicas y genera un reporte breve:")
                appendLine()
                data.forEach { (key, value) ->
                    appendLine("$key: $value")
                }
                appendLine()
                appendLine("Incluye:")
                appendLine("1. Resumen (2-3 líneas)")
                appendLine("2. Análisis clave (3-4 puntos)")
                appendLine("3. Recomendaciones (2-3 acciones)")
                appendLine()
                appendLine("Máximo 300 palabras. Usa markdown.")
            }

            val response = model.generateContent(prompt)
            response.text ?: "No se pudo generar el reporte"
        } catch (e: Exception) {
            throw Exception("Error en Gemini: ${e.message}", e)
        }
    }

    /**
     * Genera análisis de pacientes por mes
     */
    suspend fun generatePatientsTrendReport(monthlyData: List<Pair<String, Int>>): String {
        if (!isInitialized) throw IllegalStateException("GeminiService no está inicializado")

        return try {
            val prompt = buildString {
                appendLine("Analiza esta tendencia de actividad mensual:")
                appendLine()
                monthlyData.forEach { (month, count) ->
                    appendLine("$month: $count")
                }
                appendLine()
                appendLine("Proporciona:")
                appendLine("1. Tendencia (crecimiento/estable/decrecimiento)")
                appendLine("2. Meses destacados")
                appendLine("3. Recomendaciones (2-3)")
                appendLine()
                appendLine("Máximo 250 palabras. Markdown.")
            }

            val response = model.generateContent(prompt)
            response.text ?: "No se pudo generar el análisis"
        } catch (e: Exception) {
            throw Exception("Error en Gemini: ${e.message}", e)
        }
    }

    /**
     * Genera análisis de citas por estado
     */
    suspend fun generateAppointmentsAnalysis(statusData: Map<String, Int>): String {
        if (!isInitialized) throw IllegalStateException("GeminiService no está inicializado")

        return try {
            val prompt = buildString {
                appendLine("Analiza esta distribución de citas:")
                appendLine()
                statusData.forEach { (status, count) ->
                    appendLine("$status: $count")
                }
                appendLine()
                appendLine("Proporciona:")
                appendLine("1. Eficiencia operativa")
                appendLine("2. Problemas identificados")
                appendLine("3. Recomendaciones (2-3)")
                appendLine()
                appendLine("Máximo 250 palabras. Markdown.")
            }

            val response = model.generateContent(prompt)
            response.text ?: "No se pudo generar el análisis"
        } catch (e: Exception) {
            throw Exception("Error en Gemini: ${e.message}", e)
        }
    }

    /**
     * Genera reporte personalizado basado en criterios específicos
     */
    suspend fun generateCustomReport(
        reportType: String,
        criteria: Map<String, Any>,
        data: String
    ): String {
        if (!isInitialized) throw IllegalStateException("GeminiService no está inicializado")

        val prompt = buildString {
            appendLine("Eres un experto en análisis médico y generación de reportes. Genera un reporte de tipo: $reportType")
            appendLine()
            appendLine("CRITERIOS:")
            criteria.forEach { (key, value) ->
                appendLine("- $key: $value")
            }
            appendLine()
            appendLine("DATOS:")
            appendLine(data)
            appendLine()
            appendLine("Genera un reporte completo, profesional y accionable que incluya:")
            appendLine("- Resumen ejecutivo")
            appendLine("- Análisis detallado")
            appendLine("- Visualización de insights clave")
            appendLine("- Recomendaciones específicas")
            appendLine("- Conclusiones")
            appendLine()
            appendLine("Formato: Markdown estructurado y profesional.")
        }

        val response = model.generateContent(prompt)
        return response.text ?: "No se pudo generar el reporte personalizado"
    }

    /**
     * Genera insights y recomendaciones basadas en datos del consultorio
     */
    suspend fun generateInsights(contextData: String): String {
        if (!isInitialized) throw IllegalStateException("GeminiService no está inicializado")

        val prompt = buildString {
            appendLine("Eres un consultor médico con experiencia en optimización de consultorios.")
            appendLine("Analiza los siguientes datos y proporciona insights valiosos:")
            appendLine()
            appendLine(contextData)
            appendLine()
            appendLine("Proporciona:")
            appendLine("1. Top 3 insights más importantes")
            appendLine("2. Oportunidades de mejora inmediatas")
            appendLine("3. Riesgos o alertas que requieren atención")
            appendLine("4. Sugerencias estratégicas a largo plazo")
            appendLine()
            appendLine("Sé conciso pero específico. Formato markdown.")
        }

        val response = model.generateContent(prompt)
        return response.text ?: "No se pudieron generar insights"
    }
}
