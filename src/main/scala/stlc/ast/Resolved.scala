package stlc.ast

final case class Resolved(exp: Resolved.Exp)

object Resolved:
  type Ctx = Map[String, Sym]

  export Parsed.Typ

  enum Exp:
    case Var(name: Sym)
    case True
    case False
    case If(antecedent: Exp, consequent: Exp, alternative: Exp)
    case Abs(parameter: Sym, body: Exp)
    case App(operator: Exp, operand: Exp)
    case Anno(target: Exp, annotation: Typ)
