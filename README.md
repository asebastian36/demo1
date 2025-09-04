# 🧬 Algoritmo Genético - Optimización Evolutiva

Proyecto Spring Boot que implementa un **algoritmo genético** para optimizar una función objetivo a partir de una población inicial de números binarios. Inspirado en cálculos manuales de Excel, este sistema replica y mejora el proceso evolutivo con cruces, mutaciones y análisis visual.

---

## 🎯 Objetivo

Simular un algoritmo genético que:

- Convierta binarios a valores reales.
- Evalúe su adaptación mediante una función cuadrática.
- Aplique **cruce simple o doble** y **mutación aleatoria**.
- Evolucione la población durante varias generaciones.
- Muestre resultados en una interfaz web con gráficos.

---

## 📦 Tecnologías utilizadas
- **Java 17+**
- **Spring Boot** (Web, Data JPA)
- **Thymeleaf** (vistas)
- **H2 Database** (almacenamiento en memoria)
- **JFreeChart** (gráficas)
- **Maven** (gestión de dependencias)

---

## 🧠 Flujo del algoritmo genético

### 1. **Entrada del usuario**
El usuario sube un archivo `.txt` con 15 números binarios y especifica:
- `xmin`, `xmax`: rango del valor real
- `L`: longitud de precisión (bits)
- Tipo de cruce: **simple (bit 4)** o **doble (bits 3-9)**

### 2. **Generación de la población inicial**
- Los binarios se leen y normalizan a longitud `L`.
- Se convierten a:
  - **Decimal** → usando `Integer.parseInt(bin, 2)`
  - **Real** → mapeo lineal:  
    \[
    x = x_{min} + \frac{decimal}{2^L - 1} \cdot (x_{max} - x_{min})
    \]
  - **Adaptativo** → función:  
    \[
    f(x) = x^2 + 2x + 5
    \]

### 3. **Ordenamiento**
- Los individuos se ordenan por **valor adaptativo descendente**.

### 4. **Cruce (por generación)**
- Se aplican **11 pares fijos** de cruces:
  - Generación 2: pares como (4,14), (3,7), etc.
  - Generación 3: pares diferentes
- **Cruce simple**: intercambia bits después del 4to bit.
- **Cruce doble**: intercambia bits del 3ro al 9no (7 bits centrales).
- **Reemplazo posicional**:
  - Hijo 1 vs Padre 1 → si mejora, reemplaza
  - Hijo 2 vs Padre 2 → si mejora, reemplaza

### 5. **Mutación aleatoria**
- Cada individuo tiene un **2% de probabilidad** de mutar un bit aleatorio.
- La mutación ocurre **después del cruce**, sobre la generación actual.

### 6. **Persistencia**
- Cada generación se guarda en la base de datos (H2).
- Se usa `IndividualService` como capa intermedia.

### 7. **Visualización**
- Resultados mostrados en pestañas por generación.
- Gráfica de evolución del valor adaptativo.
- Datos exportados como imagen Base64.

---

## 🔧 Servicios clave

### `GeneticAlgorithmService`
- Coordinador principal.
- Aplica evolución por generaciones.
- Usa `CrossoverService` y `MutationService`.

### `CrossoverService`
- Ejecuta cruces según tipo (`single` o `double`).
- Reemplaza individuos **in-situ** si los hijos son mejores.

### `MutationService`
- Aplica mutación aleatoria con baja probabilidad.
- Mantiene diversidad genética.

### `IndividualService`
- Capa entre `IndividualRepository` y servicios.
- Garantiza encapsulamiento.

---

## 🚀 Cómo ejecutar

* Clonar el proyecto 
* Ejecutar (si se tiene java 17 o superior en el path de su respectivo s.o):
> En la ruta que se clono el proyecto
   ```bash
   mvn spring-boot:run
   ```
* Abrir en el navegador:
   ```
   http://localhost:8080
   ```
   