Tablas Principales
1.1 Tabla usuarios
sqlCREATE TABLE usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    tipo_usuario TEXT NOT NULL CHECK(tipo_usuario IN ('MEDICO', 'PACIENTE')),
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT 1
);
1.2 Tabla medicos
sqlCREATE TABLE medicos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INTEGER NOT NULL,
    nombre TEXT NOT NULL,
    apellidos TEXT NOT NULL,
    especialidad TEXT NOT NULL,
    numero_colegiado TEXT UNIQUE NOT NULL,
    telefono TEXT,
    hospital_clinica TEXT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
1.3 Tabla pacientes
sqlCREATE TABLE pacientes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INTEGER NOT NULL,
    nombre TEXT NOT NULL,
    apellidos TEXT NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    genero TEXT CHECK(genero IN ('M', 'F', 'O')),
    telefono TEXT,
    direccion TEXT,
    numero_seguridad_social TEXT,
    contacto_emergencia TEXT,
    telefono_emergencia TEXT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
1.4 Tabla medico_paciente (Relación Muchos a Muchos)
sqlCREATE TABLE medico_paciente (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    medico_id INTEGER NOT NULL,
    paciente_id INTEGER NOT NULL,
    fecha_asignacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT 1,
    FOREIGN KEY (medico_id) REFERENCES medicos(id) ON DELETE CASCADE,
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE,
    UNIQUE(medico_id, paciente_id)
);
1.5 Tabla consultas
sqlCREATE TABLE consultas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    medico_id INTEGER NOT NULL,
    paciente_id INTEGER NOT NULL,
    fecha_consulta DATETIME NOT NULL,
    motivo_consulta TEXT NOT NULL,
    diagnostico TEXT,
    observaciones TEXT,
    proxima_cita DATETIME,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (medico_id) REFERENCES medicos(id),
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id)
);
1.6 Tabla tratamientos
sqlCREATE TABLE tratamientos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    consulta_id INTEGER NOT NULL,
    medicamento TEXT NOT NULL,
    dosis TEXT NOT NULL,
    frecuencia TEXT NOT NULL,
    duracion_dias INTEGER,
    indicaciones TEXT,
    FOREIGN KEY (consulta_id) REFERENCES consultas(id) ON DELETE CASCADE
);
1.7 Tabla historial_medico
sqlCREATE TABLE historial_medico (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    paciente_id INTEGER NOT NULL,
    tipo_registro TEXT NOT NULL, -- 'ALERGIA', 'ENFERMEDAD_CRONICA', 'CIRUGIA', 'ANTECEDENTE_FAMILIAR'
    descripcion TEXT NOT NULL,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT 1,
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id)
);