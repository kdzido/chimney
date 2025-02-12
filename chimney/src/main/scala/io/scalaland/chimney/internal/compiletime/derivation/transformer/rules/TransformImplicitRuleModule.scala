package io.scalaland.chimney.internal.compiletime.derivation.transformer.rules

import io.scalaland.chimney.dsl.{PreferPartialTransformer, PreferTotalTransformer}
import io.scalaland.chimney.internal.compiletime.DerivationResult
import io.scalaland.chimney.internal.compiletime.derivation.transformer.Derivation

private[compiletime] trait TransformImplicitRuleModule { this: Derivation =>

  protected object TransformImplicitRule extends Rule("Implicit") {

    def expand[From, To](implicit ctx: TransformationContext[From, To]): DerivationResult[Rule.ExpansionResult[To]] =
      ctx match {
        case TransformationContext.ForTotal(src) =>
          summonTransformerSafe[From, To].fold(DerivationResult.attemptNextRule[To]) { totalTransformer =>
            // We're constructing:
            // '{ ${ totalTransformer }.transform(${ src }) } }
            DerivationResult.expandedTotal(totalTransformer.transform(src))
          }
        case TransformationContext.ForPartial(src, failFast) =>
          import ctx.config.flags.implicitConflictResolution
          (summonTransformerSafe[From, To], summonPartialTransformerSafe[From, To]) match {
            case (Some(total), Some(partial)) if implicitConflictResolution.isEmpty =>
              // TODO: change from immediately terminating error to DerivationResult.fail
              reportError(
                s"""Ambiguous implicits while resolving Chimney recursive transformation:
                   |
                   |PartialTransformer[${Type.prettyPrint[From]}, ${Type.prettyPrint[To]}]: ${Expr.prettyPrint(partial)}
                   |Transformer[${Type.prettyPrint[From]}, ${Type.prettyPrint[To]}]: ${Expr.prettyPrint(total)}
                   |
                   |Please eliminate ambiguity from implicit scope or use enableImplicitConflictResolution/withFieldComputed/withFieldComputedPartial to decide which one should be used
                   |""".stripMargin
              )
            case (Some(totalTransformer), partialTransformerOpt)
                if partialTransformerOpt.isEmpty || implicitConflictResolution.contains(PreferTotalTransformer) =>
              // We're constructing:
              // '{ ${ totalTransformer }.transform(${ src }) } }
              DerivationResult.expandedTotal(totalTransformer.transform(src))
            case (totalTransformerOpt, Some(partialTransformer))
                if totalTransformerOpt.isEmpty || implicitConflictResolution.contains(PreferPartialTransformer) =>
              // We're constructing:
              // '{ ${ partialTransformer }.transform(${ src }, ${ failFast }) } }
              DerivationResult.expandedPartial(partialTransformer.transform(src, failFast))
            case _ => DerivationResult.attemptNextRule
          }
      }
  }
}
