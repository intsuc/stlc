package stlc.ast

final case class Sym private (id: Int)

object Sym:
  private val Id = java.util.concurrent.atomic.AtomicInteger()

  def fresh(): Sym = Sym(Id.getAndIncrement())
