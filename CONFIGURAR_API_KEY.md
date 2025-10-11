# 🔑 Configuración de API Key de Gemini

## ✅ Pasos para Configurar

### 1. Edita el archivo `local.properties`

Abre el archivo `local.properties` en la raíz del proyecto (mismo nivel que `build.gradle.kts`).

Si no existe, créalo.

### 2. Agrega tu API Key

Agrega la siguiente línea al archivo:

```properties
GEMINI_API_KEY=AIzaSyBAprAi4ekmFlrXqCjO-Rq84mARlNN_7t4
```

**IMPORTANTE**: Reemplaza el valor con tu API key real si es diferente.

### 3. Ejemplo completo de `local.properties`

Tu archivo `local.properties` debería verse así:

```properties
## This file contains machine specific properties for the build.
# Location of the Android SDK
sdk.dir=C\:\\Users\\byron\\AppData\\Local\\Android\\Sdk

# Gemini AI API Key
GEMINI_API_KEY=AIzaSyBAprAi4ekmFlrXqCjO-Rq84mARlNN_7t4
```

### 4. Sincroniza el proyecto

En Android Studio:
1. Haz clic en **File → Sync Project with Gradle Files**
2. O presiona el botón **"Sync Now"** que aparece en la parte superior

### 5. Compila el proyecto

Ejecuta:
```bash
.\gradlew.bat clean build
```

O desde Android Studio: **Build → Rebuild Project**

## ✅ Verificación

Para verificar que la configuración funciona:

1. Ejecuta la app
2. Ve a **Reportes** desde el dashboard
3. Selecciona cualquier reporte (Estadísticas Generales, Pacientes, etc.)
4. Si todo está bien, verás el reporte generado por Gemini AI

## 🔒 Seguridad

✅ **El archivo `local.properties` está en `.gitignore`**
- No se subirá a Git
- Tu API key está segura
- Cada desarrollador debe configurar su propia API key

✅ **La API key se lee en tiempo de compilación**
- Se almacena en `BuildConfig.GEMINI_API_KEY`
- No está hardcodeada en el código fuente

## ❌ Solución de Problemas

### Error: "GEMINI_API_KEY not found"

**Solución**: 
1. Verifica que el archivo `local.properties` existe
2. Verifica que la línea `GEMINI_API_KEY=...` está presente
3. Sincroniza Gradle nuevamente

### Error: "API key inválida"

**Solución**:
1. Verifica que copiaste la API key completa
2. Genera una nueva API key en: https://makersuite.google.com/app/apikey
3. Actualiza `local.properties` con la nueva key

### La app no compila después de agregar la API key

**Solución**:
1. Limpia el proyecto: `.\gradlew.bat clean`
2. Sincroniza Gradle
3. Rebuild: `.\gradlew.bat build`

## 📝 Notas Adicionales

- **Gratis**: Gemini 1.5 Flash es gratuito hasta 15 requests/minuto
- **Límites**: Consulta https://ai.google.dev/pricing para más detalles
- **Seguridad**: Nunca compartas tu API key públicamente

## 🎯 ¿Listo?

Una vez configurado, tus reportes con Gemini AI estarán listos para usar. ¡Disfruta de análisis inteligentes y recomendaciones personalizadas! 🚀
