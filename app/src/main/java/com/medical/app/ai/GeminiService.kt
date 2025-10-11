package com.medical.app.ai

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor() {

    private lateinit var model: GenerativeModel
    private var isInitialized = false
    
    companion object {
        private const val TAG = "GeminiService"
    }
    /**
     * Inicializa el servicio de Gemini con la API key
     */
    fun initialize(apiKey: String) {
        Log.d(TAG, "Iniciando GeminiService...")
        Log.d(TAG, "API Key length: ${apiKey.length}")
        Log.d(TAG, "API Key preview: ${apiKey.take(10)}...")
        
        try {
            model = GenerativeModel(
                modelName = "gemini-2.0-flash-exp",
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 40
                    topP = 0.95f
                    maxOutputTokens = 1024
                }
            )
            isInitialized = true
            Log.i(TAG, "GeminiService inicializado correctamente con modelo: gemini-2.0-flash-exp")
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar GeminiService", e)
            throw e
        }
    }
    suspend fun generateGeneralStatsReport(data: Map<String, Any>): String {
        Log.d(TAG, "generateGeneralStatsReport - Iniciando...")
        
        if (!isInitialized) {
            Log.e(TAG, "generateGeneralStatsReport - Servicio no inicializado")
            throw IllegalStateException("GeminiService no está inicializado")
        }

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
            
            Log.d(TAG, "generateGeneralStatsReport - Prompt length: ${prompt.length}")
            Log.d(TAG, "generateGeneralStatsReport - Enviando request a Gemini API...")

            val response = model.generateContent(prompt)
            
            Log.d(TAG, "generateGeneralStatsReport - Response recibida")
            Log.d(TAG, "generateGeneralStatsReport - Response text length: ${response.text?.length ?: 0}")
            
            val result = response.text ?: "No se pudo generar el reporte"
            Log.i(TAG, "generateGeneralStatsReport - Completado exitosamente")
            result
        } catch (e: Exception) {
            Log.e(TAG, "generateGeneralStatsReport - Error: ${e.message}", e)
            Log.e(TAG, "generateGeneralStatsReport - Error type: ${e.javaClass.simpleName}")
            Log.e(TAG, "generateGeneralStatsReport - Stack trace: ${e.stackTraceToString()}")
            throw Exception("Error en Gemini: ${e.message}", e)
        }
    }

    /**
     * Genera análisis de pacientes por mes
     */
    suspend fun generatePatientsTrendReport(monthlyData: List<Pair<String, Int>>): String {
        Log.d(TAG, "generatePatientsTrendReport - Iniciando...")
        
        if (!isInitialized) {
            Log.e(TAG, "generatePatientsTrendReport - Servicio no inicializado")
            throw IllegalStateException("GeminiService no está inicializado")
        }

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
            
            Log.d(TAG, "generatePatientsTrendReport - Data points: ${monthlyData.size}")
            Log.d(TAG, "generatePatientsTrendReport - Enviando request a Gemini API...")

            val response = model.generateContent(prompt)
            
            Log.d(TAG, "generatePatientsTrendReport - Response recibida")
            val result = response.text ?: "No se pudo generar el análisis"
            Log.i(TAG, "generatePatientsTrendReport - Completado exitosamente")
            result
        } catch (e: Exception) {
            Log.e(TAG, "generatePatientsTrendReport - Error: ${e.message}", e)
            Log.e(TAG, "generatePatientsTrendReport - Error type: ${e.javaClass.simpleName}")
            throw Exception("Error en Gemini: ${e.message}", e)
        }
    }

    /**
     * Genera análisis de citas por estado
     */
    suspend fun generateAppointmentsAnalysis(statusData: Map<String, Int>): String {
        Log.d(TAG, "generateAppointmentsAnalysis - Iniciando...")
        
        if (!isInitialized) {
            Log.e(TAG, "generateAppointmentsAnalysis - Servicio no inicializado")
            throw IllegalStateException("GeminiService no está inicializado")
        }

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
            
            Log.d(TAG, "generateAppointmentsAnalysis - Status count: ${statusData.size}")
            Log.d(TAG, "generateAppointmentsAnalysis - Enviando request a Gemini API...")

            val response = model.generateContent(prompt)
            
            Log.d(TAG, "generateAppointmentsAnalysis - Response recibida")
            val result = response.text ?: "No se pudo generar el análisis"
            Log.i(TAG, "generateAppointmentsAnalysis - Completado exitosamente")
            result
        } catch (e: Exception) {
            Log.e(TAG, "generateAppointmentsAnalysis - Error: ${e.message}", e)
            Log.e(TAG, "generateAppointmentsAnalysis - Error type: ${e.javaClass.simpleName}")
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
        Log.d(TAG, "generateCustomReport - Iniciando...")
        Log.d(TAG, "generateCustomReport - Report type: $reportType")
        
        if (!isInitialized) {
            Log.e(TAG, "generateCustomReport - Servicio no inicializado")
            throw IllegalStateException("GeminiService no está inicializado")
        }

        return try {
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
            
            Log.d(TAG, "generateCustomReport - Criteria count: ${criteria.size}")
            Log.d(TAG, "generateCustomReport - Data length: ${data.length}")
            Log.d(TAG, "generateCustomReport - Enviando request a Gemini API...")

            val response = model.generateContent(prompt)
            
            Log.d(TAG, "generateCustomReport - Response recibida")
            val result = response.text ?: "No se pudo generar el reporte personalizado"
            Log.i(TAG, "generateCustomReport - Completado exitosamente")
            result
        } catch (e: Exception) {
            Log.e(TAG, "generateCustomReport - Error: ${e.message}", e)
            Log.e(TAG, "generateCustomReport - Error type: ${e.javaClass.simpleName}")
            throw Exception("Error en Gemini: ${e.message}", e)
        }
    }

    /**
     * Genera insights y recomendaciones basadas en datos del consultorio
     */
    suspend fun generateInsights(contextData: String): String {
        Log.d(TAG, "generateInsights - Iniciando...")
        
        if (!isInitialized) {
            Log.e(TAG, "generateInsights - Servicio no inicializado")
            throw IllegalStateException("GeminiService no está inicializado")
        }

        return try {
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
            
            Log.d(TAG, "generateInsights - Context data length: ${contextData.length}")
            Log.d(TAG, "generateInsights - Enviando request a Gemini API...")

            val response = model.generateContent(prompt)
            
            Log.d(TAG, "generateInsights - Response recibida")
            val result = response.text ?: "No se pudieron generar insights"
            Log.i(TAG, "generateInsights - Completado exitosamente")
            result
        } catch (e: Exception) {
            Log.e(TAG, "generateInsights - Error: ${e.message}", e)
            Log.e(TAG, "generateInsights - Error type: ${e.javaClass.simpleName}")
            throw Exception("Error en Gemini: ${e.message}", e)
        }
    }
}
