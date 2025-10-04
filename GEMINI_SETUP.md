# Configuración de Gemini AI para Reportes Avanzados

## Paso 1: Obtener API Key de Gemini

1. Ve a [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Inicia sesión con tu cuenta de Google
3. Haz clic en "Create API Key"
4. Copia la API key generada

## Paso 2: Configurar la API Key en la App

### Opción 1: Configuración Temporal (Para desarrollo)

Edita el archivo: `app/src/main/java/com/medical/app/ui/reports/AiReportViewModel.kt`

Busca la línea:
```kotlin
private val apiKey: String
    get() = "TU_API_KEY_AQUI" // TODO: Mover a configuración segura
```

Reemplaza `"TU_API_KEY_AQUI"` con tu API key real.

### Opción 2: Configuración Segura (Recomendado para producción)

1. Crea un archivo `local.properties` en la raíz del proyecto (si no existe)
2. Agrega tu API key:
   ```properties
   GEMINI_API_KEY=tu_api_key_aqui
   ```

3. Modifica `app/build.gradle.kts` para leer la API key:
   ```kotlin
   android {
       defaultConfig {
           // ...
           buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\"")
       }
   }
   ```

4. Actualiza `AiReportViewModel.kt`:
   ```kotlin
   private val apiKey: String
       get() = BuildConfig.GEMINI_API_KEY
   ```

## Paso 3: Sincronizar Gradle

1. En Android Studio, haz clic en "Sync Now" o
2. Ejecuta: `./gradlew build`

## Reportes Disponibles con Gemini AI

### 1. Estadísticas Generales
- Análisis completo de pacientes, citas y consultas
- Tendencias y patrones identificados
- Recomendaciones personalizadas

### 2. Tendencia de Pacientes
- Análisis de crecimiento mensual
- Identificación de meses pico
- Proyecciones futuras

### 3. Análisis de Citas
- Distribución por estado
- Identificación de problemas operativos
- Estrategias para reducir cancelaciones

## Características de los Reportes AI

✅ **Análisis Inteligente**: Gemini analiza tus datos y proporciona insights valiosos
✅ **Recomendaciones Accionables**: Sugerencias específicas para mejorar
✅ **Formato Profesional**: Reportes estructurados en markdown
✅ **Contexto Médico**: Análisis especializado para consultorios médicos

## Límites y Costos

- **Gemini 1.5 Flash**: Gratis hasta 15 RPM (requests per minute)
- Para más información: [Pricing de Gemini](https://ai.google.dev/pricing)

## Solución de Problemas

### Error: "GeminiService no está inicializado"
- Verifica que hayas configurado correctamente la API key
- Asegúrate de que la API key sea válida

### Error: "API key inválida"
- Verifica que copiaste la API key completa
- Genera una nueva API key si es necesario

### Los reportes tardan mucho
- Gemini puede tardar 5-15 segundos en generar reportes complejos
- Esto es normal para análisis detallados

## Próximas Mejoras

- [ ] Reportes personalizados con filtros avanzados
- [ ] Exportación a PDF con formato
- [ ] Gráficos generados por AI
- [ ] Comparativas entre períodos
- [ ] Predicciones y forecasting

## Soporte

Para más información sobre Gemini AI:
- [Documentación oficial](https://ai.google.dev/docs)
- [Ejemplos de código](https://github.com/google/generative-ai-android)
