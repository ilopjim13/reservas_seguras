package com.es.sessionsecurity.service

import com.es.sessionsecurity.error.exception.BadRequestException
import com.es.sessionsecurity.error.exception.NotFoundException
import com.es.sessionsecurity.model.Session
import com.es.sessionsecurity.model.Usuario
import com.es.sessionsecurity.repository.SessionRepository
import com.es.sessionsecurity.repository.UsuarioRepository
import com.es.sessionsecurity.util.CipherUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@Service
class UsuarioService {

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository
    @Autowired
    private lateinit var sessionRepository: SessionRepository

    fun login(userLogin: Usuario) : String {

        // COMPROBACIÓN DE LOS CAMPOS DEL OBJETO USERLOGIN
        if(userLogin.password.isBlank() || userLogin.nombre.isBlank()) {
            throw BadRequestException("Los campos nombre y password son obligatorios")
        }

        // COMPROBAR CREDENCIALES
        // 1 Obtener el usuario de la base de datos
        var userBD: Usuario = usuarioRepository
            .findByNombre(userLogin.nombre)
            .orElseThrow{NotFoundException("El usuario proporcionado no existe en BDD")}

        val cipherUtils = CipherUtils()
        val userLoginEncrypt = cipherUtils.encrypt(userLogin.password)
        if(userBD.password == userLogin.password) {
            // 3 GENERAR EL TOKEN
            val cipherUtils = CipherUtils()
            var token: String = ""
            token = cipherUtils.encrypt(userBD.nombre)

            // 4 CREAR UNA SESSION
            val s: Session = Session(
                null,
                token,
                userBD,
                LocalDateTime.now().plusMinutes(3)
            )

            // 5 INSERTAMOS EN BDD
            sessionRepository.save(s)

            return token
        } else {
            // SI LA CONTRASEÑA NO COINCIDE, LANZAMOS EXCEPCIÓN
            throw NotFoundException("Las credenciales son incorrectas")
        }
    }

    fun findByName(nombre: String):Usuario {
        return usuarioRepository.findByNombre(nombre).orElseThrow{NotFoundException("El usuario proporcionado no existe en BDD")}
    }

    fun insert(nuevoUsuario: Usuario):Usuario {
        if(nuevoUsuario.password.isBlank() || nuevoUsuario.nombre.isBlank()) {
            throw BadRequestException("Los campos nombre y password son obligatorios")
        }

        if (nuevoUsuario.rol.uppercase() !in listOf("ADMIN", "USER") ) {
            throw BadRequestException("El campo rol debe ser USER o ADMIN")
        }

        val cipherUtils = CipherUtils()
        nuevoUsuario.password =cipherUtils.encrypt(nuevoUsuario.password)

        usuarioRepository.save(nuevoUsuario)

        return usuarioRepository.findByNombre(nuevoUsuario.nombre).orElseThrow()
    }

}