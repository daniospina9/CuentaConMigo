# Rediseño contable de Tarjetas de Crédito

> Estado: **DISEÑO — pendiente de aprobación e implementación.**
> Alcance acordado: **solo el modelo nuevo.** El cierre de la brecha de integridad
> (`credit_card_transactions.destinationAccountId` sin FK) queda para una fase posterior.

---

## 1. Contexto y problema

Hoy, una compra con Tarjeta de Crédito (TC) crea una `Transaction` con:

- `depositAccountId = null`
- `destinationAccountId = <categoría PRINCIPAL>`
- `type = EXPENSE`

Es decir, las compras de TC se categorizan contra el **pool de categorías principal**, mezclándose
con los gastos reales de las cuentas de depósito. El problema conceptual es que una compra con TC es
una **deuda incurrida (devengado)**, no plata que salió de una cuenta de depósito. La plata real sale
recién cuando se **paga** la tarjeta (base caja).

Esto produce además una **inconsistencia real** en el informe financiero actual:

- `getExpenseTotalsByDestination` ("Gastos por categoría") **incluye** las compras de TC
  (la transacción tiene `destinationAccountId`, aunque `depositAccountId` sea null).
- El `IncomeStatement` ("Estado de resultados") **excluye** las compras de TC, porque solo suma
  ingresos/egresos de cuentas de depósito (itera sobre `depositAccountRepository.getByUser`).

→ El informe se contradice a sí mismo.

---

## 2. Modelo objetivo

Patrón **plantilla / instancia (template / instance)**:

- **Categorías principales** (`creditCardId IS NULL`) = la PLANTILLA / catálogo.
- **Cada TC** recibe sus propias categorías = CLONES, una copia por tarjeta (`creditCardId = <card>`).

Flujo contable:

| Evento | Antes | Después |
|---|---|---|
| **Compra con TC** | categoría PRINCIPAL | categoría CLON de esa TC (`creditCardId = card`) |
| **Interés / cargo** | "Pago de Intereses" PRINCIPAL | "Pago de Intereses" CLON de esa TC |
| **Pago de TC** | egreso de depósito SIN categoría (`destinationAccountId = null`) | egreso de depósito categorizado a "Tarjetas de Crédito" (PRINCIPAL) |

Resultado:

- **Informe por TC** (nueva pantalla): desglose por categoría de lo gastado con esa tarjeta (devengado).
- **Informe principal**: solo refleja plata real que salió del bolsillo. Las TC aparecen como una
  única categoría **"Tarjetas de Crédito"** (los pagos). Sin doble conteo: las compras viven en los
  clones (filtradas fuera del principal), solo los pagos entran al principal.

---

## 3. Cambios de esquema

### 3.1 `DestinationAccountEntity` — dos columnas nuevas

```kotlin
val creditCardId: Long? = null,     // null = categoría principal; != null = clon de esa TC
val templateAccountId: Long? = null // id de la categoría principal de la que es clon (para sincronizar)
```

- **`creditCardId`** → se declara como **Foreign Key real a `credit_cards`, `onDelete = CASCADE`**,
  con su `Index`. Es coherente con el patrón ya existente (`credit_card_transactions.creditCardId` y
  `credit_card_extracts.creditCardId` ya son FK CASCADE) y con la tabla mejor modelada (`transactions`).
- **`templateAccountId`** → se deja como `Long?` **suelto** (no FK declarada), igual que `parentAccountId`.
  Motivo: es una auto-referencia a `destination_accounts`, y los FK auto-referenciales en Room traen
  complicaciones de orden de inserción (es justamente por eso que `parentAccountId` ya es suelto).
  La integridad la maneja el código.

> **Hallazgo que valida el CASCADE como seguro:** `DeleteCreditCardUseCase` **bloquea** borrar una
> tarjeta con movimientos (`hasTransactions` → throw) y hace **soft-delete** (`isActive = false`).
> O sea, una `credit_cards` casi nunca se borra físicamente, y cuando se borra es porque NO tiene
> transacciones → sus clones tampoco tienen → el CASCADE limpia sin chocar con el `RESTRICT` de
> `transactions.destinationAccountId`. No hay conflicto en la práctica.

### 3.2 Modelo de dominio y mapper

Agregar `creditCardId: Long? = null` y `templateAccountId: Long? = null` a:

- `domain/model/DestinationAccount.kt`
- `DestinationAccountMapper.kt` (`toDomain` y `toEntity`)

### 3.3 Identidad ≠ nombre (regla firme)

Todo vínculo se ancla en **id**, nunca en nombre:

- Las transacciones ya apuntan por `destinationAccountId` (id) → **renombrar una categoría ya es seguro hoy**.
- El vínculo plantilla↔clon se ancla en `templateAccountId` (id) → renombrar/sincronizar es a prueba de balas.
- `name` es solo etiqueta de presentación; al renombrar una principal, se **propaga** a los clones por id.

---

## 4. Cambios en queries (`DestinationAccountDao`)

### 4.1 Excluir clones del catálogo principal

Las queries que alimentan los módulos principales deben filtrar `creditCardId IS NULL`:

```sql
-- getByUser (módulo principal de cuentas destino)
SELECT * FROM destination_accounts
WHERE userId = :userId AND parentAccountId IS NULL AND creditCardId IS NULL
ORDER BY name ASC
```

Revisar también: `getInvestmentAccount(s)`, `getSavingsAccounts` → agregar `AND creditCardId IS NULL`
para que el catálogo principal no se mezcle con clones.

### 4.2 Nuevas queries para clones por TC

```sql
-- categorías de una TC (para el diálogo de compra y la pantalla de desglose)
SELECT * FROM destination_accounts
WHERE userId = :userId AND creditCardId = :creditCardId AND parentAccountId IS NULL
ORDER BY name ASC

-- lookup por nombre DENTRO de una tarjeta (para getOrCreate por-TC) — ver §5.4
SELECT * FROM destination_accounts
WHERE userId = :userId AND name = :name AND creditCardId = :creditCardId
LIMIT 1

-- clones de una plantilla (para propagar renombrado / sincronizar)
SELECT * FROM destination_accounts WHERE templateAccountId = :templateId
```

### 4.3 `getExpenseTotalsByDestination` — dos variantes

**Informe principal** (agregar filtro para excluir clones):

```sql
... WHERE da.userId = :userId
  AND da.creditCardId IS NULL          -- << NUEVO: solo categorías principales
  AND ( ... )                          -- (resto igual)
```

**Informe por TC** (nueva query, scopeada a la tarjeta):

```sql
... WHERE da.userId = :userId
  AND da.creditCardId = :creditCardId  -- << solo categorías de esta tarjeta
  AND ( ... )
```

> ⚠️ **Punto crítico**: sin el `AND da.creditCardId IS NULL` en el informe principal, las compras de TC
> (ahora en clones) se sumarían de nuevo al principal → doble conteo. Este filtro es obligatorio.

---

## 5. Cambios en use cases

### 5.1 `RegisterPurchaseUseCase` (compra)

- La compra debe categorizar contra el **clon de la TC**, no contra la principal.
- El `destinationAccountId` que llega del diálogo ya será el del clon (ver §6, el diálogo carga clones).
- La `Transaction` vinculada sigue igual: `depositAccountId = null`, `destinationAccountId = <clon>`,
  `type = EXPENSE`. (El patrón de fila espejo se mantiene → la regla "no borrar con transacciones" sigue
  funcionando sola para los clones.)

### 5.2 `RegisterPurchaseUseCase` / `RegisterExtractUseCase` (interés y cargo)

- `getOrCreateInterestAccount` debe resolver la "Pago de Intereses" **de la tarjeta** (clon), no la principal.
- Hoy crea con `isDefault = true`, `AccountType.EXPENSE`, lookup por `getByName(userId, name)`.
- **Cambio**: el lookup y la creación deben incluir `creditCardId = <card>` (ver §5.4).

### 5.3 `RegisterPaymentUseCase` (pago) — cambio mínimo

Hoy crea la `Transaction` con `destinationAccountId = null`. El único cambio:

```kotlin
destinationAccountId = getOrCreateCreditCardPaymentAccount(userId).id   // "Tarjetas de Crédito" PRINCIPAL
```

- Sigue siendo **UNA sola** `Transaction` vinculada, que ahora lleva `depositAccountId` (depósito)
  **y** `destinationAccountId` ("Tarjetas de Crédito").
- **Borrado cruzado gratis**: `DeleteCreditCardTransactionUseCase` ya borra la fila vinculada
  (`linkedTransactionId`). Al ser una sola fila, borrar el pago desde la TC elimina de un saque el
  efecto en depósito Y la entrada en "Tarjetas de Crédito". Sin código nuevo.

### 5.4 `getByName` no distingue por tarjeta — hay que arreglarlo

> **Hallazgo a cuidar:** `getByName(userId, name)` matchea por `userId + name` solamente. Con la columna
> nueva, buscar "Pago de Intereses" devolvería cualquiera (la principal o la de otra tarjeta).

Solución: agregar lookup con `creditCardId` y usarlo en los `getOrCreate`:

- `getOrCreateInterestAccount(userId, creditCardId)` → busca/crea el clon "Pago de Intereses" de esa TC.
- `getOrCreateCreditCardPaymentAccount(userId)` → busca/crea "Tarjetas de Crédito" PRINCIPAL
  (`creditCardId = null`), `isDefault = true`. **Lazy-only, NO sembrada** (decisión de Daniel).

> La cuenta "Tarjetas de Crédito" existe **si y solo si** hay al menos un pago: la crea la migración
> (si hay pagos viejos) o el primer pago en runtime. Usuarios sin pagos nunca la ven.

---

## 6. Cambios en UI

### 6.1 `CreditCardDetailViewModel`

- Hoy: `destinationAccounts = destinationAccountRepository.getByUser(userId).filter { != SAVINGS }`.
- Después: cargar los **clones de esta tarjeta** → nueva query `getByUserAndCard(userId, creditCardId)`
  (filtrando SAVINGS igual que hoy). El diálogo "Registrar compra" usa esa lista.
- `purchaseSubAccounts` (subcuentas de inversión): como NO se clonan inversiones por TC (§9.1), el diálogo
  de compra de TC no tendrá categorías de inversión. Revisar si el selector de subcuenta de inversión del
  diálogo puede quedar inactivo/oculto en el contexto de TC.

### 6.2 Nueva pantalla dedicada: "Gastos por categoría" de la TC

- Acceso: **botón** en `CreditCardDetailScreen` (p. ej. junto a la card de resumen de deuda).
- Contenido: reusa el patrón del informe financiero (`CategoryRow` con %), alimentado por la variante
  por-TC de `getExpenseTotalsByDestination` (§4.3).
- Navegación: nueva ruta en `AppNavGraph` con `creditCardId` como argumento.

---

## 7. Clonado de categorías

### 7.1 Al crear una TC

Cuando se crea una tarjeta nueva, clonar **todas las categorías principales de tipo `expense`** (decisión
§9.1) a esa TC, seteando `creditCardId = nuevaCard` y `templateAccountId = id de la principal`. (Hook en
el flujo de creación de TC — `CreateCreditCard...` en `DebtListScreen`.)

### 7.2 Sincronización plantilla → clon (híbrido pragmático)

- **Agregar** categoría principal → crear el clon en todas las TC existentes.
- **Renombrar** categoría principal → propagar `name` a los clones (por `templateAccountId`).
- **Borrar** categoría principal → **fuera de alcance por ahora** (la regla "no borrar con transacciones"
  ya lo bloquea casi siempre; se define en fase posterior).

---

## 8. Plan de migración (Room `17 → 18`)

Room envuelve cada migración en una transacción → **atómica**: si algo falla, rollback total.
Recomendación previa al release: **probar la migración contra una copia de la base real**.

### Paso A — Esquema (simple, estilo `MIGRATION_16_17`)

```sql
ALTER TABLE destination_accounts ADD COLUMN creditCardId INTEGER;
ALTER TABLE destination_accounts ADD COLUMN templateAccountId INTEGER;
CREATE INDEX IF NOT EXISTS index_destination_accounts_creditCardId
  ON destination_accounts (creditCardId);
```

> Nota: para que `creditCardId` sea FK declarada a nivel Room, el `@Entity` la define; en la migración
> SQL basta con la columna + index (SQLite no añade FK a tablas existentes sin recrearlas; Room valida
> el esquema declarado). Si se quiere la FK física estricta, requiere recrear la tabla
> (estilo `MIGRATION_8_9`) — **decisión abierta §9.2**.

### Paso B — Datos: clonar categorías por TC

Por cada TC y cada categoría principal aplicable, insertar un clon con `templateAccountId` = id original:

```sql
INSERT INTO destination_accounts
  (userId, name, type, isDefault, investmentSubtype, parentAccountId,
   assetInitialValue, creditCardId, templateAccountId)
SELECT da.userId, da.name, da.type, da.isDefault, da.investmentSubtype, NULL,
       da.assetInitialValue, cc.id, da.id
FROM destination_accounts da
JOIN credit_cards cc ON cc.userId = da.userId
WHERE da.creditCardId IS NULL
  AND da.parentAccountId IS NULL
  AND da.type = 'expense';     -- ver §9.1 (¿incluir inversión/ahorro?)
```

### Paso C — Re-apuntar compras a los clones

```sql
-- transacción vinculada de cada compra → clon correspondiente (por templateAccountId + creditCardId)
UPDATE transactions
SET destinationAccountId = (
  SELECT clone.id FROM destination_accounts clone
  JOIN credit_card_transactions cct ON cct.linkedTransactionId = transactions.id
  WHERE clone.creditCardId = cct.creditCardId
    AND clone.templateAccountId = transactions.destinationAccountId
)
WHERE transactions.id IN (
  SELECT linkedTransactionId FROM credit_card_transactions
  WHERE type = 'PURCHASE' AND linkedTransactionId IS NOT NULL
);
-- idem para credit_card_transactions.destinationAccountId
```

### Paso D — "Pago de Intereses" por TC + re-apuntar intereses/cargos

- Clonar "Pago de Intereses" por TC que tenga intereses/cargos (mismo patrón que Paso B, filtrando esa categoría).
- Re-apuntar las transacciones de `type IN ('INTEREST','FEE')` a su clon por TC (mismo patrón que Paso C).

### Paso E — "Tarjetas de Crédito" + re-apuntar pagos

```sql
-- crear "Tarjetas de Crédito" PRINCIPAL para cada usuario que tenga pagos de TC
INSERT INTO destination_accounts (userId, name, type, isDefault, creditCardId, templateAccountId)
SELECT DISTINCT cct.userId, 'Tarjetas de Crédito', 'expense', 1, NULL, NULL
FROM credit_card_transactions cct
WHERE cct.type = 'PAYMENT'
  AND NOT EXISTS (
    SELECT 1 FROM destination_accounts d
    WHERE d.userId = cct.userId AND d.name = 'Tarjetas de Crédito' AND d.creditCardId IS NULL
  );

-- re-apuntar la transacción vinculada de cada pago viejo a esa cuenta
UPDATE transactions
SET destinationAccountId = (
  SELECT d.id FROM destination_accounts d
  WHERE d.userId = transactions.userId AND d.name = 'Tarjetas de Crédito' AND d.creditCardId IS NULL
)
WHERE transactions.id IN (
  SELECT linkedTransactionId FROM credit_card_transactions
  WHERE type = 'PAYMENT' AND linkedTransactionId IS NOT NULL
);
```

> ⚠️ Los Pasos B–D, para **inversión/ahorro** con jerarquía padre/hija, requieren clonar padre y luego
> hijas re-mapeando `parentAccountId` (mapeo de ids viejo→nuevo). Eso es difícil en SQL puro. Si se
> decide incluir inversión por TC (§9.1), conviene hacer el backfill de datos en **Kotlin** (un paso
> único y guardado), con control programático del mapeo de ids, en vez de SQL correlacionado.

### Registro de la migración

- Definir `MIGRATION_17_18` en el `companion object` de `AppDatabase` (estilo existente).
- Registrarla en `.addMigrations(...)` del builder de Room (en el módulo DI, no en `AppDatabase.kt`).
- Subir `version = 18`.

---

## 9. Decisiones (RESUELTAS)

### 9.1 ¿Qué categorías se clonan por TC? → **SOLO tipo `expense`** ✅ (decidido por Daniel)
Se clonan únicamente las cuentas destino de tipo `expense`. Se EXCLUYEN `savings` e `investment`.
- De las 6 por defecto, se clonan 4: Necesidades Básicas, Juegos y Diversión, Donativos, Pago de Intereses.
  Quedan afuera: Ahorros a Largo Plazo (`savings`) e Inversiones (`investment`).
- Motivo: con una TC se hacen GASTOS; el diálogo de compra ya filtra `savings`, e invertir con TC es raro.
- Consecuencia: la migración queda simple en SQL puro (sin jerarquía padre/hija). El `Paso B` ya filtra
  `da.type = 'expense'`.

### 9.2 FK de `creditCardId` → **columna + index** ✅ (decidido)
Se agrega como columna + index vía `ALTER TABLE` (sin recrear tabla). Room valida el esquema del `@Entity`.
La FK física estricta queda para la fase posterior de "endurecer integridad".

---

## 10. Alcance

**Incluido:** modelo plantilla/clon, columnas nuevas, queries por TC, filtro del informe principal,
nueva pantalla de desglose por TC, getOrCreate por-TC y de "Tarjetas de Crédito" (lazy), clonado al
crear TC + sync agregar/renombrar, migración 17→18.

**Fuera de alcance (fase posterior):** cierre de la brecha de FK en
`credit_card_transactions.destinationAccountId`; borrado/sincronización de borrado de categorías
principales; FK física estricta de `creditCardId` (si se elige 9.2 simple).

---

## 11. Riesgos y testing

- **Migración**: probar contra copia de la base real antes del release. Atómica (rollback si falla).
- **Doble conteo**: verificar que el informe principal solo cuente `creditCardId IS NULL` (§4.3).
- **getOrCreate**: verificar que resuelve por `creditCardId` (§5.4) para no mezclar intereses entre tarjetas.
- **Sin duplicados**: el `getOrCreate` busca antes de crear → migración + runtime no duplican.
- **Regla de borrado**: se mantiene gratis (patrón de fila espejo en `transactions`).
