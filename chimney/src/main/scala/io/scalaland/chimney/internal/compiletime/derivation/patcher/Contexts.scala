package io.scalaland.chimney.internal.compiletime.derivation.patcher

private[compiletime] trait Contexts { this: Derivation =>

  /** Stores all the "global" information that might be needed: types used, user configuration, runtime values, etc */
  final case class PatcherContext[A, Patch](
      obj: Expr[A],
      patch: Expr[Patch],
      config: PatcherConfig,
      derivationStartedAt: java.time.Instant
  )(
      val A: Type[A],
      val Patch: Type[Patch]
  ) {

    final type Target = A
    val Target = A
  }
  object PatcherContext {

    def create[A: Type, Patch: Type](
        obj: Expr[A],
        patch: Expr[Patch],
        config: PatcherConfig
    ): PatcherContext[A, Patch] =
      PatcherContext(obj = obj, patch = patch, config = config, derivationStartedAt = java.time.Instant.now())(
        A = Type[A],
        Patch = Type[Patch]
      )
  }

  // unpacks Types from Contexts
  implicit final protected def ctx2AType[A, Patch](implicit ctx: PatcherContext[A, Patch]): Type[A] = ctx.A
  implicit final protected def ctx2PatchType[A, Patch](implicit ctx: PatcherContext[A, Patch]): Type[Patch] = ctx.Patch

  // for unpacking Exprs from Context, import ctx.* should be enough
}
