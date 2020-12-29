package stlc.ast

final case class Typed(exp: Typed.Exp)

object Typed:
  type Ctx = Map[Sym, Typ]

  export Resolved.Typ

  enum Exp:
    case Var(name: Sym)
    case True
    case False
    case If(antecedent: Exp, consequent: Exp, alternative: Exp)
    case Abs(parameter: Sym, body: Exp)
    case App(operator: Exp, operand: Exp)
