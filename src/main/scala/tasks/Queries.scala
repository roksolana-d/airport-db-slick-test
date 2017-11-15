package tasks

import models._
import slick.jdbc.PostgresProfile.api._

class Queries(db: Database) {


  def queryForTask63 = {
    val subquery = PassInTripTable.table.groupBy(p => (p.place, p.idPsgFk))
      .map { case ((place, id), group) => (id, group.length) }
      .filter { case (id, count) => count > 1 }
      .map { case (id, count) => id }

    val query = PassengerTable.table.filter(_.idPsg in subquery).map(_.name)

    db.run(query.result)
  }

  def queryForTask67 = {
    val query = TripTable.table.groupBy(t => (t.townFrom, t.townTo))
      .map { case ((townFrom, townTo), group) => (group.length, townFrom, townTo)}
      .sortBy{case (count, townFrom, townTo) => count}
      .take(1)

    db.run(query.length.result)
  }

  def queryForTask68 = {
    val subquery1 = TripTable.table.filter(t => t.townFrom >= t.townTo)
      .groupBy(t => (t.townFrom, t.townTo))
      .map{ case ((townFrom, townTo), group) => (group.length, townFrom, townTo)}

    val subquery2 = TripTable.table.filter(t => t.townTo > t.townFrom)
      .groupBy(t => (t.townFrom, t.townTo))
      .map{ case ((townFrom, townTo), group) => (group.length, townFrom, townTo)}

    val t = subquery1 union subquery2
    val tt = t.map { case subquery1 => (subquery1._1, subquery1._2, subquery1._3) }
      .groupBy { case (sub1, sub2, sub3) => (sub2, sub3) }
      .map { case ((sub2, sub3), group) => (group.map(_._1).sum, sub2, sub3) }
      .sortBy { case (sum, sub2, sub3) => sum }
      .take(1)
    db.run(tt.length.result)
  }

  def queryForTask72 = {
    val t = (for {
      pit <- PassInTripTable.table
      t <- TripTable.table if pit.tripNoFk === t.tripNo
    } yield (pit, t))
      .groupBy{case (pit,t) => (pit.idPsgFk, t.idCompFk)}
      .map{case ((idPsgFk, idCompFk), group) => (idPsgFk, idCompFk, group.length)}

    val tt = t.filter{case (idPsgFk, idCompFk, count) => count === 1}
      .groupBy{ case (idPsgFk, idCompFk, count) => idPsgFk}
      .map{ case (idPsgFk, group) => (idPsgFk, group.map(_._1).max)}

    val query = (for {
      p <- PassengerTable.table
      t <- tt if p.idPsg === t._1
    } yield (p, t))
      .map{ case (p,t) => (p.name, t._2)}
      .sortBy(_._2)
      .take(1)
    db.run(query.result)
  }

  def queryForTask77 = {
    val x = (for {
      pit <- PassInTripTable.table
      t <- TripTable.table if pit.tripNoFk === t.tripNo
    } yield (pit, t))
      .filter{case (pit, t) => (t.townFrom === "Rostov")}
      .groupBy{case (pit, t) => (pit.tripNoFk, pit.date)}
      .map{case ((tripNoFk, date), group) => (date, group.countDistinct)}

    val query = x.sortBy(_._1.desc).take(1)
    db.run(query.result)
  }

  def queryForTask95 = {
    val query = (for {
      c <- CompanyTable.table
      t <- TripTable.table if c.idComp === t.idCompFk
      pit <- PassInTripTable.table if t.tripNo === pit.tripNoFk
    } yield (c, t, pit))
      .groupBy { case (c, t, pit) => (c.idComp, c.name) }
      .map { case ((c, t, pit), group) => (group.map(_._1.name).countDistinct, group.map(_._2.tripNo).countDistinct,
        group.map(_._2.plane).countDistinct, group.map(_._3.idPsgFk).countDistinct, group.length)
      }

    db.run(query.result)
  }

  def queryForTask103 = {
    val query = (for {
      t <- TripTable.table
      tt <- TripTable.table  if t.tripNo < tt.tripNo
      ttt <- TripTable.table  if tt.tripNo < ttt.tripNo
    } yield (t.tripNo, tt.tripNo, ttt.tripNo))
      .groupBy{case (t, tt, ttt) => (t, tt, ttt)}
      .map{ case ((t, tt, ttt),group) => (group.map(_._1).min, group.map(_._1).max,
        group.map(_._2).min, group.map(_._2).max, group.map(_._3).min, group.map(_._3).max)}
    println(query.result.statements)
  }
}
