package com.es.sessionsecurity.service

import com.es.sessionsecurity.model.Reserva
import com.es.sessionsecurity.repository.ReservaRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ReservaService {

    @Autowired
    private lateinit var reservaRepository: ReservaRepository

    fun findByUsuario_Nombre(nombre:String):List<Reserva> {
        return reservaRepository.findByUsuario_Nombre(nombre)
    }

}