package com.example.cuentaconmigo.core.db.repository.mappers

import com.example.cuentaconmigo.core.db.entities.UserEntity
import com.example.cuentaconmigo.domain.model.User

// Entity ↔ Domain
fun UserEntity.toDomain() = User(id = id, name = name)