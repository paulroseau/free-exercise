package exercise.db

import scala.collection.mutable.{ Map => MutableMap }

import exercise.model.User

sealed trait UserRepository {

  def getUser(id: Long): Option[User]

  def createUser(user: User): Long

  def updateUser(id: Long, newUser: User): Option[Unit]

  def deleteUser(id: Long): Option[Unit]
}

class InMemoryUserRepo extends UserRepository {

  private val repo = MutableMap[Long, User]()
  private var autoInc = 0L

  def getUser(id: Long): Option[User] = 
    repo.get(id)

  def createUser(user: User): Long = {
    autoInc += 1
    repo += (autoInc -> user)
    autoInc
  }

  def updateUser(id: Long, newUser: User): Option[Unit] =
    repo.get(id).map { _ =>
      repo += ((id, newUser))
    }

  def deleteUser(id: Long): Option[Unit] =
    repo.get(id).map { _ =>
      repo -= id
    }
}

class InMemoryUserRepoSync extends UserRepository {

  private val repo = MutableMap[Long, User]()
  private var autoInc = 0L

  def getUser(id: Long): Option[User] = 
    this.synchronized {
      repo.get(id)
    }

  def createUser(user: User): Long = 
    this.synchronized {
      autoInc += 1
      repo += (autoInc -> user)
      autoInc
    }

  def updateUser(id: Long, newUser: User): Option[Unit] =
    this.synchronized {
      repo.get(id).map { _ =>
        repo += ((id, newUser))
      }
    }

  def deleteUser(id: Long): Option[Unit] =
    this.synchronized {
      repo.get(id).map { _ =>
        repo -= id
      }
    }
}
