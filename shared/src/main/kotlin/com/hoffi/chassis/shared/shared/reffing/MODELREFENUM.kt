package com.hoffi.chassis.shared.shared.reffing

enum class MODELREFENUM { MODEL, DTO, TABLE, DCO ; companion object { val sentinel: MODELREFENUM = MODEL }}
//enum class MODELINSTANCEENUM { DTO, TABLE, DCO   ; companion object { val sentinel: MODELINSTANCEENUM = DTO }}
enum class MODELKIND { DTOKIND, TABLEKIND        ; companion object { val sentinel: MODELKIND = DTOKIND }}
