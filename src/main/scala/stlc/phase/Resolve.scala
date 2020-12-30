package stlc.phase

import stlc.CompileException
import stlc.ast.{Parsed => P, Resolved => R, Sym}

object Resolve extends (P => R):
  def apply(parsed: P): R =
    R(visit(parsed.exp)(using Map.empty))

  private def visit(exp: P.Exp)(using ctx: R.Ctx): R.Exp =
    exp match

    // Var
    case P.Exp.Var(name) =>
      ctx.get(name) match
      case Some(name) =>
        R.Exp.Var(name)
      case None =>
        throw CompileException(s"not found: $name")

    // True
    case P.Exp.True =>
      R.Exp.True

    // False
    case P.Exp.False =>
      R.Exp.False

    // If
    case P.Exp.If(antecedent, consequent, alternative) =>
      val antecedent1 = visit(antecedent)
      val consequent1 = visit(consequent)
      val alternative1 = visit(alternative)
      R.Exp.If(antecedent1, consequent1, alternative1)

    // Abs
    case P.Exp.Abs(parameter, body) =>
      val parameter1 = Sym.fresh()
      val body1 = visit(body)(using ctx + (parameter -> parameter1))
      R.Exp.Abs(parameter1, body1)

    // App
    case P.Exp.App(operator, operand) =>
      val operator1 = visit(operator)
      val operand1 = visit(operand)
      R.Exp.App(operator1, operand1)

    // Anno
    case P.Exp.Anno(target, annotation) =>
      val target1 = visit(target)
      R.Exp.Anno(target1, annotation)
