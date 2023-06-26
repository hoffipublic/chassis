package com.hoffi.chassis.shared.helpers

import arrow.core.*

fun <A> Either.Companion.tryCatch(f: () -> A): Either<Throwable, A> = try { f().right() } catch (t: Throwable) { t.nonFatalOrThrow().left() }
inline infix fun <A :Throwable, B> Either<A, B>.onThrow(default: (A) -> B): B = this.getOrElse(default)

