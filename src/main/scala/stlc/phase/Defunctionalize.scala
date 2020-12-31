package stlc.phase

import stlc.ast.{Defunctionalized => D, Sym, Typed => T}

object Defunctionalize extends (T => D):
  def apply(typed: T): D =
    val (exp1, ctx1) = visit(typed.exp)
    D(exp1, ctx1)

  private def visit(exp: T.Exp): (D.Exp, D.Ctx) =
    exp match

    // D-Var
    case T.Exp.Var(name) =>
      (D.Exp.Var(name), Seq.empty)

    // D-True
    case T.Exp.True =>
      (D.Exp.True, Seq.empty)

    // D-False
    case T.Exp.False =>
      (D.Exp.False, Seq.empty)

    // D-If
    case T.Exp.If(antecedent, consequent, alternative) =>
      val (antecedent1, ctx1) = visit(antecedent)
      val (consequent1, ctx2) = visit(consequent)
      val (alternative1, ctx3) = visit(alternative)
      (D.Exp.If(antecedent1, consequent1, alternative1), ctx1 ++ ctx2 ++ ctx3)

    // D-Clos
    case exp @ T.Exp.Abs(parameter, body) =>
      val (body1, ctx1) = visit(body)
      val data1 = fv(exp)
      (D.Exp.Clos(parameter, data1), (parameter, data1, body1) +: ctx1)

    // D-Call
    case T.Exp.App(operator, operand) =>
      val (operator1, ctx1) = visit(operator)
      val (operand1, ctx2) = visit(operand)
      (D.Exp.Call(operator1, operand1), ctx1 ++ ctx2)

  private def fv(exp: T.Exp): Set[Sym] =
    exp match
    case T.Exp.Var(name) => Set(name)
    case T.Exp.True => Set.empty
    case T.Exp.False => Set.empty
    case T.Exp.If(antecedent, consequent, alternative) => fv(antecedent) ++ fv(consequent) ++ fv(alternative)
    case T.Exp.Abs(parameter, body) => fv(body) - parameter
    case T.Exp.App(operator, operand) => fv(operator) ++ fv(operand)
