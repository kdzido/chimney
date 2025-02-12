package io.scalaland.chimney.internal.compiletime.derivation.transformer.rules

import io.scalaland.chimney.internal.compiletime.DerivationResult
import io.scalaland.chimney.internal.compiletime.derivation.transformer.Derivation

private[compiletime] trait TransformValueClassToTypeRuleModule {
  this: Derivation & TransformProductToProductRuleModule =>

  protected object TransformValueClassToTypeRule extends Rule("ValueClassToType") {

    @scala.annotation.nowarn("msg=Unreachable case")
    def expand[From, To](implicit ctx: TransformationContext[From, To]): DerivationResult[Rule.ExpansionResult[To]] =
      Type[From] match {
        case ValueClassType(from2) =>
          import from2.{Underlying, value as valueFrom}
          // We're constructing:
          // '{ ${ derivedTo } // using ${ src }.from internally }
          deriveRecursiveTransformationExpr[from2.Underlying, To](valueFrom.unwrap(ctx.src))
            .flatMap(DerivationResult.expanded)
            // fall back to case classes expansion; see https://github.com/scalalandio/chimney/issues/297 for more info
            .orElse(TransformProductToProductRule.expand(ctx))
            .orElse(
              DerivationResult
                .notSupportedTransformerDerivationForField(valueFrom.fieldName)(ctx)
                .log(
                  s"Failed to resolve derivation from ${Type.prettyPrint[from2.Underlying]} (wrapped by ${Type
                      .prettyPrint[From]}) to ${Type.prettyPrint[To]}"
                )
            )
        case _ => DerivationResult.attemptNextRule
      }
  }
}
