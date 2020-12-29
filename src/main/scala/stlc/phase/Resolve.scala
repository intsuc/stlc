package stlc.phase

import stlc.CompileException
import stlc.ast.{Parsed => P, Resolved => R, Sym}

object Resolve extends (P => R):
  def apply(parsed: P): R =
    R(visit(parsed.exp)(using Map.empty))

  private def visit(exp: P.Exp)(using ctx: R.Ctx): R.Exp =
    exp match

    // R-Var
    case P.Exp.Var(name) =>
      ctx.get(name) match
      case Some(name) =>
        R.Exp.Var(name)
      case None =>
        throw CompileException(s"not found: $name")

    // R-True
    case P.Exp.True =>
      R.Exp.True

    // R-False
    case P.Exp.False =>
      R.Exp.False

    // R-If
    case P.Exp.If(antecedent, consequent, alternative) =>
      R.Exp.If(visit(antecedent), visit(consequent), visit(alternative))

    // R-Abs
    case P.Exp.Abs(parameter, body) =>
      val parameter1 = Sym.fresh()
      R.Exp.Abs(parameter1, visit(body)(using ctx + (parameter -> parameter1)))

    // R-App
    case P.Exp.App(operator, operand) =>
      R.Exp.App(visit(operator), visit(operand))

    // R-Anno
    case P.Exp.Anno(target, annotation) =>
      R.Exp.Anno(visit(target), annotation)
