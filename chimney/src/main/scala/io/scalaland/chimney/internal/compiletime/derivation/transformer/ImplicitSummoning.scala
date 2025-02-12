package io.scalaland.chimney.internal.compiletime.derivation.transformer

private[compiletime] trait ImplicitSummoning { this: Derivation =>

  import ChimneyType.Implicits.*

  final protected def summonTransformerSafe[From, To](implicit
      ctx: TransformationContext[From, To]
  ): Option[Expr[io.scalaland.chimney.Transformer[From, To]]] =
    if (isForwardReferenceToItself[From, To](ctx.config.preventResolutionForTypes)) None
    else summonTransformerUnchecked[From, To]

  final protected def summonPartialTransformerSafe[From, To](implicit
      ctx: TransformationContext[From, To]
  ): Option[Expr[io.scalaland.chimney.PartialTransformer[From, To]]] =
    if (isForwardReferenceToItself[From, To](ctx.config.preventResolutionForTypes)) None
    else summonPartialTransformerUnchecked[From, To]

  final protected def summonTransformerUnchecked[From: Type, To: Type]
      : Option[Expr[io.scalaland.chimney.Transformer[From, To]]] =
    Expr.summonImplicit[io.scalaland.chimney.Transformer[From, To]]

  final protected def summonPartialTransformerUnchecked[From: Type, To: Type]
      : Option[Expr[io.scalaland.chimney.PartialTransformer[From, To]]] =
    Expr.summonImplicit[io.scalaland.chimney.PartialTransformer[From, To]]

  // prevents: val t: Transformer[A, B] = a => t.transform(a)
  private def isForwardReferenceToItself[From: Type, To: Type](
      preventResolutionForTypes: Option[(??, ??)]
  ): Boolean = preventResolutionForTypes.exists { case (from, to) =>
    from.Underlying =:= Type[From] && to.Underlying =:= Type[To]
  }
}
