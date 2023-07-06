package io.scalaland.chimney.dsl

import io.scalaland.chimney.internal.*
import io.scalaland.chimney.internal.compiletime.derivation.transformer.TransformerMacros
import io.scalaland.chimney.internal.compiletime.dsl.TransformerIntoMacros
import io.scalaland.chimney.internal.runtime.{TransformerCfg, TransformerFlags}

final class TransformerInto[From, To, Cfg <: TransformerCfg, Flags <: TransformerFlags](
    val source: From,
    val td: TransformerDefinition[From, To, Cfg, Flags]
) extends FlagsDsl[[Flags1 <: TransformerFlags] =>> TransformerInto[From, To, Cfg, Flags1], Flags] {

  def partial: PartialTransformerInto[From, To, Cfg, Flags] =
    new PartialTransformerInto[From, To, Cfg, Flags](source, td.partial)

  transparent inline def withFieldConst[T, U](
      inline selector: To => T,
      inline value: U
  )(using U <:< T): TransformerInto[From, To, ? <: TransformerCfg, Flags] = {
    ${ TransformerIntoMacros.withFieldConstImpl('this, 'selector, 'value) }
  }

  transparent inline def withFieldComputed[T, U](
      inline selector: To => T,
      inline f: From => U
  )(using U <:< T): TransformerInto[From, To, ? <: TransformerCfg, Flags] = {
    ${ TransformerIntoMacros.withFieldComputedImpl('this, 'selector, 'f) }
  }

  transparent inline def withFieldRenamed[T, U](
      inline selectorFrom: From => T,
      inline selectorTo: To => U
  ): TransformerInto[From, To, ? <: TransformerCfg, Flags] = {
    ${ TransformerIntoMacros.withFieldRenamedImpl('this, 'selectorFrom, 'selectorTo) }
  }

  transparent inline def withCoproductInstance[Inst](
      inline f: Inst => To
  ): TransformerInto[From, To, ? <: TransformerCfg, Flags] = {
    ${ TransformerIntoMacros.withCoproductInstanceImpl('this, 'f) }
  }

  inline def transform[ImplicitScopeFlags <: TransformerFlags](using
      tc: TransformerConfiguration[ImplicitScopeFlags]
  ): To = {
    ${ TransformerIntoMacros.transform[From, To, Cfg, Flags, ImplicitScopeFlags]('source, 'td) }
  }
}
