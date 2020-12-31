package stlc.phase

import stlc.CompileException
import stlc.ast.{Resolved => R, Typed => T}

object Type extends (R => T):
  def apply(resolved: R): T =
    T(synth(resolved.exp)(using Map.empty)._1)

  private def check(exp: R.Exp, typ: T.Typ)(using ctx: T.Ctx): T.Exp =
    (exp, typ) match

    // T-True⇐
    case (R.Exp.True, T.Typ.Bool) =>
      T.Exp.True

    // T-False⇐
    case (R.Exp.False, T.Typ.Bool) =>
      T.Exp.False

    // T-Abs⇐
    case (R.Exp.Abs(parameter, body), T.Typ.Fun(domain, codomain)) =>
      val body1 = check(body, codomain)(using ctx + (parameter -> domain))
      T.Exp.Abs(parameter, body1)

    // T-Sub⇐
    case _ =>
      val (exp1, typ1) = synth(exp)
      if typ1 == typ then
        exp1
      else
        throw CompileException(s"found type: ${stringify(typ1)}, required type: ${stringify(typ)}")

  private def synth(exp: R.Exp)(using ctx: T.Ctx): (T.Exp, T.Typ) =
    exp match

    // T-Var⇒
    case R.Exp.Var(name) =>
      (T.Exp.Var(name), ctx(name))

    // T-True⇒
    case R.Exp.True =>
      (T.Exp.True, T.Typ.Bool)

    // T-False⇒
    case R.Exp.False =>
      (T.Exp.False, T.Typ.Bool)

    // T-If⇒
    case R.Exp.If(antecedent, consequent, alternative) =>
      val antecedent1 = check(antecedent, T.Typ.Bool)
      val (consequent1, typ) = synth(consequent)
      val alternative1 = check(alternative, typ)
      (T.Exp.If(antecedent1, consequent1, alternative1), typ)

    // T-App⇒
    case R.Exp.App(operator, operand) =>
      val (operator1, typ) = synth(operator)
      typ match
      case T.Typ.Fun(domain, codomain) =>
        val operand1 = check(operand, domain)
        (T.Exp.App(operator1, operand1), codomain)
      case typ =>
        throw CompileException(s"found type: ${stringify(typ)}, required type: function type")

    // T-Anno⇒
    case R.Exp.Anno(target, annotation) =>
      (check(target, annotation), annotation)

    case _ =>
      throw CompileException(s"failed to synthesize type of ${stringify(exp)}")

  private def stringify(typ: R.Typ): String =
    typ match
    case R.Typ.Bool => "bool"
    case R.Typ.Fun(domain, codomain) => s"${stringify(domain)} → ${stringify(codomain)}"

  private def stringify(exp: R.Exp): String =
    exp match
    case R.Exp.Var(name) => name.name
    case R.Exp.True => "true"
    case R.Exp.False => "false"
    case R.Exp.If(antecedent, consequent, alternative) => s"if ${stringify(antecedent)} then ${stringify(consequent)} else ${stringify(alternative)}"
    case R.Exp.Abs(parameter, body) => s"${parameter.name} → ${stringify(body)}"
    case R.Exp.App(operator, operand) => s"${stringify(operator)} ${stringify(operand)}"
    case R.Exp.Anno(target, annotation) => s"${stringify(target)} : ${stringify(annotation)}"
