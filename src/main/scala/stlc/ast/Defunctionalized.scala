package stlc.ast

final case class Defunctionalized(exp: Defunctionalized.Exp, ctx: Defunctionalized.Ctx)

object Defunctionalized:
  type Ctx = Seq[(Sym, Set[Sym], Exp)]

  enum Exp:
    case Var(name: Sym)
    case True
    case False
    case If(antecedent: Exp, consequent: Exp, alternative: Exp)
    case Clos(tag: Sym, data: Set[Sym])
    case Call(operator: Exp, operand: Exp)
