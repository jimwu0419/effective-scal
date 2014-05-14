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

class ChildActor extends Actor {
  import context._
  val grandKid = actorOf(Props[GrandKidActor], name = "grandkid")

  var state = "normal"

  def receive: Actor.Receive = {
    case i: Int if i == -1 => {
      state = "exceptional"
      throw new RuntimeException("child failed")
    }
    case i: Int if i == -2 => {
      state = "exceptional"
      throw new NullPointerException("child null point")
    }
    case i: Int if i > 2 => println("it's ok: " + state)
    case anythingElse => println("what is this: " + anythingElse)
  }

  override def preStart(): Unit = {
    println("preStart child ... " + self.toString())
    super.preStart()
  }

  override def postStop(): Unit = {
    println("post stop child ... " + self.toString())
    super.postStop()
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("around pre restart child ... " + self.toString())
    super.preRestart(reason, message)
  }

}

class GrandKidActor extends Actor {
  def receive: Actor.Receive = {
    case i: Int if i < 0 => throw new RuntimeException("grand kid failed")
    case i: Int if i >= 0 => println("grand kid can take it")
  }

  override def preStart(): Unit = {
    println("preStart grand kid... " + self.toString())
    super.preStart()
  }

  override def postStop(): Unit = {
    println("postStop grand kid ... " + self.toString())
    super.postStop()
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("around pre restart grandkid... " + self.toString())
    super.preRestart(reason, message)
  }

}

//    val parent = system.actorOf(Props[ParentActor])
//    parent ! 1
//    parent ! -1 // child should fail
//    parent ! 3
//    parent ! -2 // child should fail
//    parent ! 3
//    parent ! 1
//
//    Thread.sleep(2000)

// preStart grand kid... Actor[akka://demo/user/$a/mychild/grandkid#102187249]
// preStart child ... Actor[akka://demo/user/$a/mychild#1431468984]
// what is this: 1
// around pre restart child ... Actor[akka://demo/user/$a/mychild#1431468984]
// post stop child ... Actor[akka://demo/user/$a/mychild#1431468984]
// [ERROR] [05/14/2014 15:39:58.014] [demo-akka.actor.default-dispatcher-4] [akka://demo/user/$a/mychild] child failed
// java.lang.RuntimeException: child failed
// 	at com.loyal3.txmessages.repository.ChildActor$$anonfun$receive$1.applyOrElse(TxMessageRepositoryTest.scala:117)
// 	at akka.actor.Actor$class.aroundReceive(Actor.scala:465)
//	at com.loyal3.txmessages.repository.ChildActor.aroundReceive(TxMessageRepositoryTest.scala:108)
//	at akka.actor.ActorCell.receiveMessage(ActorCell.scala:516)
//	at akka.actor.ActorCell.invoke(ActorCell.scala:487)
//	at akka.dispatch.Mailbox.processMailbox(Mailbox.scala:238)
//	at akka.dispatch.Mailbox.run(Mailbox.scala:220)
//	at akka.dispatch.ForkJoinExecutorConfigurator$AkkaForkJoinTask.exec(AbstractDispatcher.scala:393)
//	at scala.concurrent.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260)
//	at scala.concurrent.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1339)
//	at scala.concurrent.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979)
//	at scala.concurrent.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)

// postStop grand kid ... Actor[akka://demo/user/$a/mychild/grandkid#102187249]
// preStart child ... Actor[akka://demo/user/$a/mychild#1431468984]
// preStart grand kid... Actor[akka://demo/user/$a/mychild/grandkid#1331401504]
// it's ok: normal
// [WARN] [05/14/2014 15:39:58.020] [demo-akka.actor.default-dispatcher-2] [akka://demo/user/$a/mychild] child null point
// it's ok: exceptional
// what is this: 1
