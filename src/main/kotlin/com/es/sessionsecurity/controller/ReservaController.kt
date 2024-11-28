package com.es.sessionsecurity.controller

import com.es.sessionsecurity.error.exception.BadRequestException
import com.es.sessionsecurity.model.Reserva
import com.es.sessionsecurity.service.ReservaService
import com.es.sessionsecurity.service.SessionService
import com.es.sessionsecurity.service.UsuarioService
import com.es.sessionsecurity.util.CipherUtils
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/reservas")
class ReservaController {

    @Autowired
    private lateinit var usuarioService: UsuarioService
    @Autowired
    private lateinit var reservaService: ReservaService
    @Autowired
    private lateinit var sessionService: SessionService

    /*
    OBTENER TODAS LAS RESERVAS POR EL NOMBRE DE USUARIO DE UN CLIENTE
     */
    @GetMapping("/{nombre}")
    fun getByNombreUsuario(
        @PathVariable nombre: String,
        request: HttpServletRequest
    ) : ResponseEntity<List<Reserva>?> {

        /*
        COMPROBAR QUE LA PETICIÓN ESTÁ CORRECTAMENTE AUTORIZADA PARA REALIZAR ESTA OPERACIÓN
         */
        // 1º Extraemos el token de la Cookie
        val cookie: Cookie? = request.cookies.find { c: Cookie? ->  c?.name == "tokenSession"}
        val token = cookie?.value

        // 2º Comprobar la validez del token
        if (sessionService.checkToken(token)) {
            val userToken = sessionService.findByToken(token ?: "")
            return if (userToken.usuario.rol == "USER") {
                val userBD = usuarioService.findByName(nombre)
                val cipherUtils = CipherUtils()
                val tokenUserBD = cipherUtils.encrypt(userBD.nombre)
                if (token == tokenUserBD) {
                    ResponseEntity<List<Reserva>?>(reservaService.findByUsuario_Nombre(nombre), HttpStatus.OK)
                } else {
                    throw BadRequestException("El usuario ${userToken.usuario.nombre} no puede ver las reservas de otros usuarios")
                }
            } else {
                ResponseEntity<List<Reserva>?>(reservaService.findByUsuario_Nombre(nombre), HttpStatus.OK)
            }
        }

        // RESPUESTA
        return ResponseEntity<List<Reserva>?>(null, HttpStatus.BAD_REQUEST) // cambiar null por las reservas

    }

    /*
    INSERTAR UNA NUEVA RESERVA
     */
    @PostMapping("/")
    fun insert(
        @RequestBody nuevaReserva: Reserva,
        request: HttpServletRequest
    ) : ResponseEntity<Reserva?>{

        val cookie: Cookie? = request.cookies.find { c: Cookie? ->  c?.name == "tokenSession"}
        val token = cookie?.value

        if (sessionService.checkToken(token)) {
            val userToken = sessionService.findByToken(token ?: "")
            return if (userToken.usuario.rol == "USER") {
                val userRs = nuevaReserva.usuario
                nuevaReserva.id = null
                val cipherUtils = CipherUtils()
                val tokenUserRs = cipherUtils.encrypt(userRs.nombre)
                if (token == tokenUserRs) {
                    ResponseEntity<Reserva?>(reservaService.insert(nuevaReserva), HttpStatus.CREATED)
                } else {
                    throw BadRequestException("El usuario ${userToken.usuario.nombre} no puede agregar una reserva de otros usuarios")
                }
            } else {
                ResponseEntity<Reserva?>(reservaService.insert(nuevaReserva), HttpStatus.CREATED)
            }
        }




        // RESPUESTA
        return ResponseEntity<Reserva?>(null, HttpStatus.BAD_REQUEST); // cambiar null por la reserva
    }

}