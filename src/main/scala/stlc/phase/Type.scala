package stlc.phase

import stlc.CompileException
import stlc.ast.{Resolved => R, Typed => T}

object Type extends (R => T):
  def apply(resolved: R): T =
    T(synth(resolved.exp)(using Map.empty)._1)

  private def check(exp: R.Exp, typ: T.Typ)(using ctx: T.Ctx): T.Exp =
    (exp, typ) match

    // True⇐
    case (R.Exp.True, T.Typ.Bool) =>
      T.Exp.True

    // False⇐
    case (R.Exp.False, T.Typ.Bool) =>
      T.Exp.False

    // Abs⇐
    case (R.Exp.Abs(parameter, body), T.Typ.Fun(domain, codomain)) =>
      val body1 = check(body, codomain)(using ctx + (parameter -> domain))
      T.Exp.Abs(parameter, body1)

    // Sub⇐
    case _ =>
      val (exp1, typ1) = synth(exp)
      if typ1 == typ then
        exp1
      else
        throw CompileException(s"found type: $typ1, required type: $typ")

  private def synth(exp: R.Exp)(using ctx: T.Ctx): (T.Exp, T.Typ) =
    exp match

    // Var⇒
    case R.Exp.Var(name) =>
      (T.Exp.Var(name), ctx(name))

    // True⇒
    case R.Exp.True =>
      (T.Exp.True, T.Typ.Bool)

    // False⇒
    case R.Exp.False =>
      (T.Exp.False, T.Typ.Bool)

    // If⇒
    case R.Exp.If(antecedent, consequent, alternative) =>
      val antecedent1 = check(antecedent, T.Typ.Bool)
      val (consequent1, typ) = synth(consequent)
      val alternative1 = check(alternative, typ)
      (T.Exp.If(antecedent1, consequent1, alternative1), typ)

    // App⇒
    case R.Exp.App(operator, operand) =>
      val (operator1, typ) = synth(operator)
      typ match
      case T.Typ.Fun(domain, codomain) =>
        val operand1 = check(operand, domain)
        (T.Exp.App(operator1, operand1), codomain)
      case typ =>
        throw CompileException(s"found type: ${typ}, required type: function type")

    // Anno⇒
    case R.Exp.Anno(target, annotation) =>
      (check(target, annotation), annotation)

    case _ =>
      throw CompileException(s"failed to synthesize type of $exp")
