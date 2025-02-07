package io.scalaland.chimney

import io.scalaland.chimney.dsl.{PartialTransformerDefinition, TransformerDefinition, TransformerDefinitionCommons}
import io.scalaland.chimney.internal.runtime.{TransformerCfg, TransformerFlags}

/** Type class expressing total transformation between
  * source type `From` and target type `To`.
  *
  * @tparam From type of input value
  * @tparam To   type of output value
  *
  * @since 0.1.0
  */
trait Transformer[From, To] extends Transformer.AutoDerived[From, To] {

  /** Run transformation using provided value as a source.
    *
    * @param src source value
    * @return     transformed value
    *
    * @since 0.1.0
    */
  def transform(src: From): To
}

object Transformer extends TransformerCompanionPlatform {

  /** Creates an empty [[io.scalaland.chimney.dsl.TransformerDefinition]] that
    * you can customize to derive [[io.scalaland.chimney.Transformer]].
    *
    * @see [[io.scalaland.chimney.dsl.TransformerDefinition]] for available settings
    *
    * @tparam From type of input value
    * @tparam To type of output value
    * @return [[io.scalaland.chimney.dsl.TransformerDefinition]] with defaults
    *
    * @since 0.4.0
    */
  def define[From, To]: TransformerDefinition[From, To, TransformerCfg.Empty, TransformerFlags.Default] =
    new TransformerDefinition(TransformerDefinitionCommons.emptyRuntimeDataStore)

  /** Creates an empty [[io.scalaland.chimney.dsl.PartialTransformerDefinition]] that
    * you can customize to derive [[io.scalaland.chimney.PartialTransformer]].
    *
    * @see [[io.scalaland.chimney.dsl.PartialTransformerDefinition]] for available settings
    *
    * @tparam From type of input value
    * @tparam To type of output value
    * @return [[io.scalaland.chimney.dsl.PartialTransformerDefinition]] with defaults
    *
    * @since 0.7.0
    */
  def definePartial[From, To]: PartialTransformerDefinition[From, To, TransformerCfg.Empty, TransformerFlags.Default] =
    new PartialTransformerDefinition(TransformerDefinitionCommons.emptyRuntimeDataStore)

  trait AutoDerived[From, To] {
    def transform(src: From): To
  }
}
