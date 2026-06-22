package com.example.cuentaconmigo.domain.usecase.simple_debt

import com.example.cuentaconmigo.domain.model.SimpleDebt
import com.example.cuentaconmigo.domain.model.SimpleDebtKind
import com.example.cuentaconmigo.domain.repository.SimpleDebtRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSimpleDebtsUseCase @Inject constructor(
    private val repository: SimpleDebtRepository
) {
    operator fun invoke(
        userId: Long,
        kind: SimpleDebtKind = SimpleDebtKind.RECEIVED
    ): Flow<List<SimpleDebt>> = repository.getActive(userId, kind)
}
