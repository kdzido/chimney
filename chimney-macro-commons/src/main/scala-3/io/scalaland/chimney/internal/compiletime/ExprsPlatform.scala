package io.scalaland.chimney.internal.compiletime

import scala.quoted
import scala.reflect.ClassTag

private[compiletime] trait ExprsPlatform extends Exprs { this: DefinitionsPlatform =>

  import quotes.*, quotes.reflect.*

  final override protected type Expr[A] = quoted.Expr[A]
  protected object Expr extends ExprModule {

    object platformSpecific {

      // Required by -Xcheck-macros to pass.
      def resetOwner[T: Type](a: Expr[T]): Expr[T] =
        a.asTerm.changeOwner(Symbol.spliceOwner).asExprOf[T]
    }
    import platformSpecific.resetOwner

    val Nothing: Expr[Nothing] = '{ ??? }
    val Null: Expr[Null] = '{ null }
    val Unit: Expr[Unit] = '{ () }

    def Int(value: Int): Expr[Int] = scala.quoted.Expr(value)
    def String(value: String): Expr[String] = scala.quoted.Expr(value)

    object Function1 extends Function1Module {
      def apply[A: Type, B: Type](fn: Expr[A => B])(a: Expr[A]): Expr[B] = '{
        ${ resetOwner(fn) }.apply(${ resetOwner(a) })
      }
    }

    object Function2 extends Function2Module {
      def tupled[A: Type, B: Type, C: Type](fn2: Expr[(A, B) => C]): Expr[((A, B)) => C] = '{ ${ fn2 }.tupled }
    }

    object Array extends ArrayModule {
      def apply[A: Type](args: Expr[A]*): Expr[Array[A]] =
        '{ scala.Array.apply[A](${ quoted.Varargs(args.toSeq) }*)(${ summonImplicit[ClassTag[A]].get }) }

      def map[A: Type, B: Type](array: Expr[Array[A]])(fExpr: Expr[A => B]): Expr[Array[B]] =
        '{ ${ resetOwner(array) }.map(${ resetOwner(fExpr) })(${ summonImplicit[ClassTag[B]].get }) }

      def to[A: Type, C: Type](array: Expr[Array[A]])(
          factoryExpr: Expr[scala.collection.compat.Factory[A, C]]
      ): Expr[C] =
        '{ ${ resetOwner(array) }.to(${ factoryExpr }) }

      def iterator[A: Type](array: Expr[Array[A]]): Expr[Iterator[A]] = '{ ${ resetOwner(array) }.iterator }
    }

    object Option extends OptionModule {
      def apply[A: Type](a: Expr[A]): Expr[Option[A]] = '{ scala.Option(${ resetOwner(a) }) }
      def empty[A: Type]: Expr[Option[A]] = '{ scala.Option.empty[A] }
      val None: Expr[scala.None.type] = '{ scala.None }
      def map[A: Type, B: Type](opt: Expr[Option[A]])(f: Expr[A => B]): Expr[Option[B]] = '{
        ${ resetOwner(opt) }.map(${ f })
      }
      def fold[A: Type, B: Type](opt: Expr[Option[A]])(onNone: Expr[B])(onSome: Expr[A => B]): Expr[B] =
        '{ ${ resetOwner(opt) }.fold(${ onNone })(${ onSome }) }
      def orElse[A: Type](opt1: Expr[Option[A]], opt2: Expr[Option[A]]): Expr[Option[A]] =
        '{ ${ opt1 }.orElse(${ opt2 }) }
      def getOrElse[A: Type](opt: Expr[Option[A]])(orElse: Expr[A]): Expr[A] =
        '{ ${ opt }.getOrElse(${ orElse }) }
      def get[A: Type](opt: Expr[Option[A]]): Expr[A] =
        '{ ${ opt }.get }
      def isDefined[A: Type](opt: Expr[Option[A]]): Expr[Boolean] =
        '{ ${ opt }.isDefined }
    }

    object Either extends EitherModule {
      def fold[L: Type, R: Type, A: Type](either: Expr[Either[L, R]])(left: Expr[L => A])(
          right: Expr[R => A]
      ): Expr[A] =
        '{ ${ resetOwner(either) }.fold[A](${ left }, ${ right }) }

      object Left extends LeftModule {
        def apply[L: Type, R: Type](value: Expr[L]): Expr[Left[L, R]] = '{ scala.Left[L, R](${ resetOwner(value) }) }

        def value[L: Type, R: Type](left: Expr[Left[L, R]]): Expr[L] = '{ ${ resetOwner(left) }.value }
      }
      object Right extends RightModule {
        def apply[L: Type, R: Type](value: Expr[R]): Expr[Right[L, R]] = '{ scala.Right[L, R](${ resetOwner(value) }) }

        def value[L: Type, R: Type](right: Expr[Right[L, R]]): Expr[R] = '{ ${ resetOwner(right) }.value }
      }
    }

    object Iterable extends IterableModule {
      def map[A: Type, B: Type](iterable: Expr[Iterable[A]])(fExpr: Expr[A => B]): Expr[Iterable[B]] =
        '{ ${ resetOwner(iterable) }.map(${ resetOwner(fExpr) }) }

      def to[A: Type, C: Type](iterable: Expr[Iterable[A]])(
          factoryExpr: Expr[scala.collection.compat.Factory[A, C]]
      ): Expr[C] =
        '{ ${ resetOwner(iterable) }.to(${ resetOwner(factoryExpr) }) }

      def iterator[A: Type](iterable: Expr[Iterable[A]]): Expr[Iterator[A]] = '{ ${ resetOwner(iterable) }.iterator }
    }

    object Map extends MapModule {
      def iterator[K: Type, V: Type](map: Expr[Map[K, V]]): Expr[Iterator[(K, V)]] = '{ ${ resetOwner(map) }.iterator }
    }

    object Iterator extends IteratorModule {
      def map[A: Type, B: Type](iterator: Expr[Iterator[A]])(fExpr: Expr[A => B]): Expr[Iterator[B]] =
        '{ ${ iterator }.map(${ fExpr }) }

      def to[A: Type, C: Type](iterator: Expr[Iterator[A]])(
          factoryExpr: Expr[scala.collection.compat.Factory[A, C]]
      ): Expr[C] =
        '{ ${ iterator }.to(${ factoryExpr }) }

      def zipWithIndex[A: Type](it: Expr[Iterator[A]]): Expr[Iterator[(A, Int)]] = '{ ${ it }.zipWithIndex }
    }

    def ifElse[A: Type](cond: Expr[Boolean])(ifBranch: Expr[A])(elseBranch: Expr[A]): Expr[A] =
      '{
        if ${ resetOwner(cond) } then ${ ifBranch }
        else ${ elseBranch }
      }

    def block[A: Type](statements: List[Expr[Unit]], expr: Expr[A]): Expr[A] =
      Block(statements.map(_.asTerm), expr.asTerm).asExprOf[A]

    def summonImplicit[A: Type]: Option[Expr[A]] = scala.quoted.Expr.summon[A]

    def eq[A: Type, B: Type](a: Expr[A], b: Expr[B]): Expr[Boolean] = '{ ${ a } == ${ b } }

    def asInstanceOf[A: Type, B: Type](expr: Expr[A]): Expr[B] = '{ ${ resetOwner(expr) }.asInstanceOf[B] }

    def upcast[A: Type, B: Type](expr: Expr[A]): Expr[B] = {
      val wideningChecked = expr.widenExpr[B]
      if Type[A] =:= Type[B] then wideningChecked
      else asInstanceOf[A, B](expr)
    }

    def suppressUnused[A: Type](expr: Expr[A]): Expr[Unit] = '{ val _ = ${ expr } }

    def prettyPrint[A](expr: Expr[A]): String = expr.asTerm.show(using Printer.TreeAnsiCode)

    def typeOf[A](expr: Expr[A]): Type[A] = Type.platformSpecific.fromUntyped[A](expr.asTerm.tpe)
  }
}
