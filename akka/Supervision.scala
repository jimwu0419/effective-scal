class ParentActor extends Actor {

  import context._
  val childActor = actorOf(Props[ChildActor], name = "mychild")

  def receive: Receive = {
    case x => childActor ! x
  }


  override def supervisorStrategy: SupervisorStrategy = {
    OneForOneStrategy() {
      case _: NullPointerException     => Resume
      case _: RuntimeException         => Restart
      case _: Throwable                => Escalate
    }
  }
}
