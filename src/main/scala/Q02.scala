package main.scala

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.functions.min

/**
 * TPC-H Query 2
 * Savvas Savvides <ssavvides@us.ibm.com>
 *
 */
class Q02 extends TpchQuery {

  import sqlContext.implicits._

  override def execute(): Unit = {

    val europe = region.filter(region("r_name") === "EUROPE")
      .join(nation, region("r_regionkey") === nation("n_regionkey"))
      .join(supplier, nation("n_nationkey") === supplier("s_nationkey"))
      .join(partsupp, supplier("s_suppkey") === partsupp("ps_suppkey"))

    val brass = part.filter(part("p_size") === 15 && part("p_type").endsWith("BRASS"))
      .join(europe, europe("ps_partkey") === $"p_partkey")

    val minCost = brass.groupBy(brass("ps_partkey")).agg(min("ps_supplycost").as("min"))

    val res = brass.join(minCost, brass("ps_partkey") === minCost("ps_partkey"))
      .filter(brass("ps_supplycost") === minCost("min"))
      .select("s_acctbal", "s_name", "n_name", "p_partkey", "p_mfgr", "s_address", "s_phone", "s_comment")
      .sort($"s_acctbal".desc, $"n_name", $"s_name", $"p_partkey")
      .limit(100)

    res.collect().foreach(println)

  }

}
